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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Jaap on 8-2-2016.
 */
public class KNXLink {

    private static KNXLink INSTANCE = null;

    private InetSocketAddress knxIP;
    private InetSocketAddress localIp;

    public static final long CLOSE_TIMEOUT_MS = 60000;

    private long lastCheck = System.currentTimeMillis();

    private KNXNetworkLinkIP knxLink = null;
    private ProcessCommunicator pc = null;

    private final KNXEventListener listener = new KNXEventListener();

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
            localIp = new InetSocketAddress(InetAddress.getByName(prop.localIp), prop.localPort);
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.message("ERROR: could not initialize KNX link settings " + e.getMessage());
        }
        LogstashLogger.INSTANCE.message("INFO: KNXLink initialized");
    }

    public synchronized static KNXLink getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KNXLink();
        }
        return INSTANCE;
    }

    public ProcessCommunicator pc() throws KNXException, InterruptedException {
        if (knxLink == null || !knxLink.isOpen()) {
            LogstashLogger.INSTANCE.message("INFO: there is no KNX link, creating the connection");
            connect();
        } else if (lastCheck + 300000 < System.currentTimeMillis()) {
            lastCheck = System.currentTimeMillis();
            //Check the connection every five minutes
            if(!testConnection()) {
                LogstashLogger.INSTANCE.message("WARN: connection test failure, restarting connection");
                close(60000);
                connect();
            }
        }

        return pc;
    }

    private void connect() throws KNXException, InterruptedException {
        knxLink = KNXNetworkLinkIP.newTunnelingLink(localIp
                , knxIP, false
                , KNXMediumSettings.create(KNXMediumSettings.MEDIUM_KNXIP, null));
        pc = new ProcessCommunicatorImpl(knxLink);
        if (testConnection()) {
            knxLink.addLinkListener(listener);
            LogstashLogger.INSTANCE.message("INFO: connected to knx " + knxIP + " @" + knxLink.getKNXMedium().getDeviceAddress());
        } else {
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
                LogstashLogger.INSTANCE.message("WARNING: KNXLink not connected, thw requests failed, "
                        + e.getMessage() + " and " + e1.getMessage());
                return false;
            }
        }
        return true;
    }

    public void close(long sleep) {
        LogstashLogger.INSTANCE.message("INFO: Closing knx connection");
        if (pc != null) {
            pc.detach();
            pc = null;
        }
        if (knxLink != null) {
            knxLink.removeLinkListener(listener);
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
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }

    public boolean readBoolean(GroupAddress address) throws KNXException, InterruptedException {
        try {
            return pc().readBool(address);
        } catch (KNXException | InterruptedException e) {
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }

    public int readInt(GroupAddress address) throws KNXException, InterruptedException {
        try {
            return pc().readUnsigned(address, ProcessCommunicator.UNSCALED);
        } catch (KNXException | InterruptedException e) {
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }

    public String readString(GroupAddress address) throws KNXException, InterruptedException {
        try {
            StateDP dp = new StateDP(address, "string");
            return pc().read(dp);
        } catch (KNXException | InterruptedException e) {
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }

    public void writeFloat(GroupAddress address, float soll) throws KNXException, InterruptedException {
        try {
            pc().write(address, soll, true);
        } catch (KNXException | InterruptedException e) {
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }

    public void writeBoolean(GroupAddress address, boolean soll) throws KNXException, InterruptedException {
        try {
            pc().write(address, soll);
        } catch (KNXException | InterruptedException e) {
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }

    public void writeInt(GroupAddress address, int soll) throws KNXException, InterruptedException {
        try {
            pc().write(address, soll, ProcessCommunicator.UNSCALED);
        } catch (KNXException | InterruptedException e) {
            close(CLOSE_TIMEOUT_MS);
            throw e;
        }
    }
}
