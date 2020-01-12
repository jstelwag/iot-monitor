package knx;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import util.HeatingProperties;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Link to the KNX bus. Low level access to retrieve and write data through this class - usually you will use KNXAccess
 * as a higher level access object.
 * @see KNXAddress
 */
public class KNXLink {
    final private int knxBridge;
    private static KNXLink[] INSTANCE = {null, null};
    private InetSocketAddress knxIP = null;
    private InetAddress localIp;
    private int[] localPortStart = {10000, 20000};
    /** Successful usage count, at a reconnect the counter is reset. */
    public long usageCount = 0;
    public long lastCheck = System.currentTimeMillis();

    public static final long CLOSE_TIMEOUT_MS = 60000;

    /** KNX events that come in via both knx bridges are kept in this map in order to ensure an event is handled only once */
    private final Map<String, String> eventMap = new PassiveExpiringMap<>(1000);
    private final List<EventHandler> events = new LinkedList<>();
    private final KNXAddressList addressList = new KNXAddressList();

    private KNXNetworkLink knxLink = null;
    private ProcessCommunicator pc = null;

    protected KNXLink(int knxBridge) {
        this.knxBridge = knxBridge;
        HeatingProperties prop = new HeatingProperties();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> close(0)));

        events.add(new KNXStateListener());
        events.add(new ToggleAllListener());
        events.add(new KNXEventLogger());
        events.add(new P1EventLogger());

        try {
            if (knxBridge == 0) {
                knxIP = new InetSocketAddress(InetAddress.getByName(prop.knxIp), prop.knxPort);
            } else {
                knxIP = new InetSocketAddress(InetAddress.getByName("192.168.178.120"), prop.knxPort);
            }
            localIp = InetAddress.getByName(prop.localIp);
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.error("Could not initialize KNX link settings " + e.getMessage());
        }
        LogstashLogger.INSTANCE.info("KNXLink initialized");
    }

    public synchronized static KNXLink getInstance(int knxBridge) {
        if (INSTANCE[knxBridge] == null) {
            INSTANCE[knxBridge] = new KNXLink(knxBridge);
        }
        return INSTANCE[knxBridge];
    }

    public synchronized static KNXLink getInstance() {
        return getInstance(new Random().nextBoolean() ? 1 : 0);
    }

    public synchronized void eventHandler(String event) {
        if (!eventMap.containsKey(event)) {
            eventMap.put(event, "null");
            KNXAddress knx = addressList.findInString(event);
            if (knx != null) {
                for (EventHandler handler : events) {
                    try {
                        new Thread(handler.onEvent(event, knx)).start();
                    } catch (Exception e) {
                        LogstashLogger.INSTANCE.error("Exception at EventHandler " + handler.getClass().getName()
                                + " " + e.getMessage());
                    }
                }
                usageCount++;
            } else {
                LogstashLogger.INSTANCE.warn("Unknown address for event " + event);
            }
        }
    }

    public ProcessCommunicator pc() throws KNXException {
        if (knxLink == null || !knxLink.isOpen()) {
            LogstashLogger.INSTANCE.info(String.format("KNX link #%d, is closed, creating the connection", knxBridge));
            connect();
        } else if (lastCheck + 300000 < System.currentTimeMillis()) {
            //Check the connection every five minutes
            if (!testConnection()) {
                LogstashLogger.INSTANCE.warn(String.format("Connection test failure at #%d, restarting connection", knxBridge));
                close(CLOSE_TIMEOUT_MS);
                connect();
            } else {
                lastCheck(true);
            }
        }
        return pc;
    }

    private void open() throws KNXException, InterruptedException {
        int port = new Random().nextInt(10000) + localPortStart[knxBridge];
        LogstashLogger.INSTANCE.info(String.format("Opening knx #%d from port %d", knxBridge, port));
        InetSocketAddress localAddress = new InetSocketAddress(localIp, port);
        LogstashLogger.INSTANCE.info("Connecting KNX link @" + localAddress.toString());

        knxLink = KNXNetworkLinkIP.newTunnelingLink(localAddress
                , knxIP, false
                , TPSettings.TP1);
        pc = new ProcessCommunicatorImpl(knxLink);
    }

    private void connect() throws KNXException {
        usageCount = 0;
        try {
            open();
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn(String.format("Connection to knx[%d] failed: %s", knxBridge, e.getMessage()));
            close(CLOSE_TIMEOUT_MS);
            throw new KNXException(String.format("Connection to knx[%d] failed", knxBridge), e);
        }

        if (testConnection()) {
            knxLink.addLinkListener(new KNXEventListener());
            LogstashLogger.INSTANCE.info(String.format("Connected #%d to knx %s @ %s", knxBridge, knxIP
                    , knxLink.getKNXMedium().getDeviceAddress()));
        } else {
            LogstashLogger.INSTANCE.error(String.format("knx link #%d connection failed, closing without retrying", knxBridge));
            close(CLOSE_TIMEOUT_MS);
            throw new KNXException("Failed to connect to KNX bus #" + knxBridge);
        }
    }

    public void lastCheck(boolean success) {
        if (success) {
            lastCheck = System.currentTimeMillis();
            usageCount++;
        } else {
            lastCheck = 0;
        }
    }

    /** Check the status of a device on the KNX bus, if it responds, it it OK */
    private boolean testConnection() {
        //Bathroom room 1, ventilation
        GroupAddress address = new GroupAddress(4, 1, 103);
        try {
            pc.readBool(address);
        } catch (KNXException | InterruptedException e) {
            //Bathroom room 2, ventilation
            address = new GroupAddress(6, 1, 102);
            try {
                pc.readBool(address);
            } catch (KNXException | InterruptedException e1) {
                LogstashLogger.INSTANCE.warn("KNXLink not connected, knx test requests failed, "
                        + e.getMessage() + " and " + e1.getMessage());
                return false;
            }
        }
        return true;
    }

    public void close(long sleep) {
        LogstashLogger.INSTANCE.info("Closing knx connection #" + knxBridge);
        if (pc != null) {
            pc.detach();
            pc = null;
        }
        if (knxLink != null) {
            knxLink.removeLinkListener(new KNXEventListener());
            knxLink.close();
            knxLink = null;
        }
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
