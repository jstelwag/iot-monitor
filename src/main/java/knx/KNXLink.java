package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import util.HeatingProperties;

import java.net.*;
import java.util.Random;

/**
 * Link to the KNX bus. Retrieve and write data through this class
 */
public class KNXLink {

    private static KNXLink INSTANCE = null;

    private InetSocketAddress knxIP;
    private InetAddress localIp;
    private int localPortStart;

    public static final long CLOSE_TIMEOUT_MS = 60000;

    private long lastCheck = System.currentTimeMillis();

    private KNXNetworkLinkIP knxLink = null;
    private ProcessCommunicator pc = null;

    private final KNXEventListener listener = new KNXEventListener();
    private final ToggleAllListener toggleListener = new ToggleAllListener();

    protected KNXLink() {
        HeatingProperties prop = new HeatingProperties();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close(0);
            }
        });
        try {
            knxIP = new InetSocketAddress(InetAddress.getByName(prop.knxIp), prop.knxPort);
            localIp = InetAddress.getByName(prop.localIp);
            localPortStart = prop.localPort;
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

    private ProcessCommunicator pc() throws KNXException, InterruptedException {
        if (knxLink == null || !knxLink.isOpen()) {
            LogstashLogger.INSTANCE.info("There is no KNX link, creating the connection");
            connect();
        } else if (lastCheck + 300000 < System.currentTimeMillis()) {
            lastCheck = System.currentTimeMillis();
            //Check the connection every five minutes
            if(!testConnection()) {
                LogstashLogger.INSTANCE.warn("Connection test failure, restarting connection");
                close(CLOSE_TIMEOUT_MS);
                connect();
            }
        }

        return pc;
    }

    private void open() throws KNXException, InterruptedException, ConnectException {
        int port = new Random().nextInt(10000) + localPortStart;
        LogstashLogger.INSTANCE.info("Opening knx from port " + port);
        InetSocketAddress localAddress = new InetSocketAddress(localIp, port);
        LogstashLogger.INSTANCE.info("Connecting KNX link @" + localAddress.toString());
        knxLink = KNXNetworkLinkIP.newTunnelingLink(localAddress
                , knxIP, false
                , KNXMediumSettings.create(KNXMediumSettings.MEDIUM_KNXIP, null));
        pc = new ProcessCommunicatorImpl(knxLink);
    }

    private void connect() throws KNXException {
        boolean open = false;
        try {
            open();
            open = true;
        } catch (KNXException | InterruptedException | ConnectException e) {
            LogstashLogger.INSTANCE.warn("Connection to knx failed, but i will retry " + e.getMessage());
            close(CLOSE_TIMEOUT_MS);
            try {
                open();
                open = true;
            } catch (KNXException | InterruptedException | ConnectException e1) {
                LogstashLogger.INSTANCE.warn("Second connection attempt to knx failed, i give up " + e1.getMessage());
            }
        }

        if (open && testConnection()) {
            knxLink.addLinkListener(listener);
            knxLink.addLinkListener(toggleListener);
            LogstashLogger.INSTANCE.info("Connected to knx " + knxIP + " @" + knxLink.getKNXMedium().getDeviceAddress());
        } else {
            LogstashLogger.INSTANCE.error("knx link connection failed, closing without retrying");
            close(CLOSE_TIMEOUT_MS);
            throw new KNXException("Failed to connect to KNX bus");
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
        LogstashLogger.INSTANCE.info("Closing knx connection");
        if (pc != null) {
            pc.detach();
            pc = null;
        }
        if (knxLink != null) {
            knxLink.removeLinkListener(listener);
            knxLink.removeLinkListener(toggleListener);
            knxLink.close();
            knxLink = null;
        }
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double readFloat(GroupAddress address) throws KNXException, InterruptedException {
        try {
            return pc().readFloat(address, false);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readFloat reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }

    public boolean readBoolean(GroupAddress address) throws KNXException, InterruptedException {
        try {
            return pc().readBool(address);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readBoolean reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }

    public int readInt(GroupAddress address) throws KNXException, InterruptedException {
        try {
            return pc().readUnsigned(address, ProcessCommunicator.UNSCALED);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readInt reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }

    public String readString(GroupAddress address) throws KNXException, InterruptedException {
        try {
            StateDP dp = new StateDP(address, "string");
            return pc().read(dp);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("readString reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }

    public void writeFloat(GroupAddress address, float soll) throws KNXException, InterruptedException {
        try {
            pc().write(address, soll, true);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("writeInt reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }

    public void writeBoolean(GroupAddress address, boolean soll) throws KNXException, InterruptedException {
        try {
            pc().write(address, soll);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("writeBoolean reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }

    public void writeInt(GroupAddress address, int soll) throws KNXException, InterruptedException {
        try {
            pc().write(address, soll, ProcessCommunicator.UNSCALED);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn("writeInt reported an exception, " + e.getMessage());
            lastCheck = 0;
            throw e;
        }
    }
}
