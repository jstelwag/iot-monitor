package knx;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import util.HeatingProperties;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Link to the KNX bus. Retrieve and write data through this class
 */
public class KNXLink {

    private static KNXLink INSTANCE = null;
    private InetSocketAddress[] knxIP = {null, null};
    private InetAddress localIp;
    private int[] localPortStart = {10000, 20000};

    private int robin = 0;

    public static final long CLOSE_TIMEOUT_MS = 60000;

    private long[] lastCheck = {System.currentTimeMillis(), System.currentTimeMillis()};

    /** KNX events that come in via both knx bridges are kept in this map in order to ensure an event is handled only once */
    private final Map<String, String> eventMap = new PassiveExpiringMap<>(1000);
    private final List<EventHandler> events = new LinkedList<>();
    private final KNXAddressList addressList = new KNXAddressList();

    private KNXNetworkLink[] knxLink = {null, null};
    private ProcessCommunicator[] pc = {null, null};

    protected KNXLink() {
        HeatingProperties prop = new HeatingProperties();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close(0, 0);
            close(1,0);
        }));

        events.add(new KNXStateListener());
        events.add(new ToggleAllListener());
        events.add(new KNXEventLogger());

        try {
            knxIP[0] = new InetSocketAddress(InetAddress.getByName(prop.knxIp), prop.knxPort);
            knxIP[1] = new InetSocketAddress(InetAddress.getByName("192.168.178.120"), prop.knxPort);
            localIp = InetAddress.getByName(prop.localIp);
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.error("Could not initialize KNX link settings " + e.getMessage());
        }
        LogstashLogger.INSTANCE.info("KNXLink initialized");
    }

    public synchronized static KNXLink getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KNXLink();
        }
        return INSTANCE;
    }

    public synchronized void eventHandler(String event) {
        if (!eventMap.containsKey(event)) {
            eventMap.put(event, "null");
        } else {
            KNXAddress knx = addressList.findInString(event);
            if (knx != null) {
                for (EventHandler handler : events) {
                    handler.onEvent(event, knx);
                }
            } else {
                LogstashLogger.INSTANCE.warn("Unknown address for event " + event);
            }
        }
    }

    private ProcessCommunicator pc() throws KNXException {
        robin = robin == 1 ? 0 : 1;
        if (knxLink[robin] == null || !knxLink[robin].isOpen()) {
            LogstashLogger.INSTANCE.info(String.format("KNX link #%d, is closed, creating the connection", robin));
            connect();
        } else if (lastCheck[robin] + 300000 < System.currentTimeMillis()) {
            lastCheck();
            //Check the connection every five minutes
            if(!testConnection()) {
                LogstashLogger.INSTANCE.warn(String.format("Connection test failure at #%d, restarting connection", robin));
                close(robin, CLOSE_TIMEOUT_MS);
                connect();
            }
        }

        return pc[robin];
    }

    private void open() throws KNXException, InterruptedException {
        int port = new Random().nextInt(10000) + localPortStart[robin];
        LogstashLogger.INSTANCE.info(String.format("Opening knx #%d from port %d", robin, port));
        InetSocketAddress localAddress = new InetSocketAddress(localIp, port);
        LogstashLogger.INSTANCE.info("Connecting KNX link @" + localAddress.toString());

        knxLink[robin] = KNXNetworkLinkIP.newTunnelingLink(localAddress
                , knxIP[robin], false
                , TPSettings.TP1);
        pc[robin] = new ProcessCommunicatorImpl(knxLink[robin]);
    }

    private void connect() throws KNXException {
        boolean open = false;
        try {
            open();
            open = true;
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn(String.format("Connection to knx[%d] failed, but i will retry %s", robin, e.getMessage()));
            close(robin, CLOSE_TIMEOUT_MS);
            robin = robin == 1 ? 0 : 1;
            try {
                open();
                open = true;
            } catch (KNXException | InterruptedException e1) {
                LogstashLogger.INSTANCE.warn("Second connection attempt to knx failed, i give up " + e1.getMessage());
            }
        }

        if (open && testConnection()) {
            knxLink[robin].addLinkListener(new KNXEventListener());
            LogstashLogger.INSTANCE.info(String.format("Connected #%d to knx %s @ %s", robin, knxIP[robin]
                    , knxLink[robin].getKNXMedium().getDeviceAddress()));
        } else {
            LogstashLogger.INSTANCE.error(String.format("knx link #%d connection failed, closing without retrying", robin));
            close(robin, CLOSE_TIMEOUT_MS);
            throw new KNXException("Failed to connect to KNX bus #" + robin);
        }
    }

    public void lastCheck() {
        lastCheck[robin] = System.currentTimeMillis();
    }

    /** Check the status of a device on the KNX bus, if it responds, it it OK */
    private boolean testConnection() {
        //Bathroom room 1, ventilation
        GroupAddress address = new GroupAddress(4, 1, 103);
        try {
            pc[robin].readBool(address);
        } catch (KNXException | InterruptedException e) {
            //Bathroom room 2, ventilation
            address = new GroupAddress(6, 1, 102);
            try {
                pc[robin].readBool(address);
            } catch (KNXException | InterruptedException e1) {
                LogstashLogger.INSTANCE.warn("KNXLink not connected, knx test requests failed, "
                        + e.getMessage() + " and " + e1.getMessage());
                return false;
            }
        }
        return true;
    }

    public void close(int robin, long sleep) {
        LogstashLogger.INSTANCE.info("Closing knx connection #" + robin);
        if (pc[robin] != null) {
            pc[robin].detach();
            pc[robin] = null;
        }
        if (knxLink[robin] != null) {
            knxLink[robin].removeLinkListener(new KNXEventListener());
            knxLink[robin].close();
            knxLink[robin] = null;
        }
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double readFloat(GroupAddress address) throws KNXException, InterruptedException {
        double retVal;
        try {
            retVal = pc().readFloat(address);
            lastCheck();
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readFloat reported an exception, retrying with other knx link" + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                retVal = pc().readFloat(address);
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on readFloat failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
        return retVal;
    }

    public boolean readBoolean(GroupAddress address) throws KNXException, InterruptedException {
        boolean retVal;
        try {
            retVal = pc().readBool(address);
            lastCheck();
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readBoolean reported an exception, " + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                retVal = pc().readBool(address);
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on readBool failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
        return retVal;
    }

    public int readInt(GroupAddress address) throws KNXException, InterruptedException {
        int retVal;
        try {
            retVal = pc().readUnsigned(address, ProcessCommunicator.UNSCALED);
            lastCheck();
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readInt reported an exception, " + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                retVal = pc().readUnsigned(address, ProcessCommunicator.UNSCALED);
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on readInt failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
        return retVal;
    }

    public String readString(GroupAddress address) throws KNXException, InterruptedException {
        String retVal;
        try {
            retVal = pc().read(new StateDP(address, "string"));
            lastCheck();
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readString reported an exception, " + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                retVal = pc().read(new StateDP(address, "string"));
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on readString failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
        return retVal;
    }

    public void writeFloat(GroupAddress address, float soll) throws KNXException {
        try {
            pc().write(address, soll, true);
            lastCheck();
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.warn("writeInt reported an exception, " + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                pc().write(address, soll, true);
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on writeFloat failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
    }

    public void writeBoolean(GroupAddress address, boolean soll) throws KNXException {
        try {
            pc().write(address, soll);
            lastCheck();
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.warn("writeBoolean reported an exception, " + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                pc().write(address, soll);
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on writeBoolean failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
    }

    public void writeInt(GroupAddress address, int soll) throws KNXException {
        try {
            pc().write(address, soll, ProcessCommunicator.UNSCALED);
            lastCheck();
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.warn("writeInt reported an exception, " + e.getMessage());
            lastCheck[robin] = 0;
            try {
                // pc()  will perform a round robin and with high probability chose the other knx link
                pc().write(address, soll, ProcessCommunicator.UNSCALED);
                lastCheck();
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Second attempt on writeInt failed: " + e2.getMessage());
                lastCheck[robin] = 0;
                throw e2;
            }
        }
    }
}
