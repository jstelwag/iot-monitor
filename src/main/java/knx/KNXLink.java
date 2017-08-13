package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
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

    public final static KNXLink INSTANCE = new KNXLink();

    private InetSocketAddress knxIP;
    private InetSocketAddress localIp;

    private KNXNetworkLinkIP knxLink = null;
    private ProcessCommunicator pc = null;

    private long lastCheck;

    private final KNXEventListener listener = new KNXEventListener();

    private KNXLink() {
        HeatingProperties prop = new HeatingProperties();
        try {
            this.knxIP = new InetSocketAddress(InetAddress.getByName(prop.knxIp), prop.knxPort);
            this.localIp = new InetSocketAddress(InetAddress.getByName(prop.localIp), prop.localPort);
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.message("ERROR: could not initialize KNX link settings " + e.getMessage());
        }
        LogstashLogger.INSTANCE.message("INFO: KNXLink initialized");
    }

    public ProcessCommunicator pc() throws KNXException, InterruptedException {
        if (knxLink == null || !knxLink.isOpen()) {
            connect();
        } else if (lastCheck + 300000 < System.currentTimeMillis()) {
            //Check the connection every five minutes
            if(!testConnection()) {
                close();
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
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    close();
                }
            });
            LogstashLogger.INSTANCE.message("INFO: connected to knx " + knxIP + " @" + knxLink.getKNXMedium().getDeviceAddress());
        } else {
            close();
            throw new KNXException("Failed to connect to KNX bus");
        }
    }

    /** Check the status of a device on the KNX bus, if it responds, it it OK */
    public boolean testConnection() {
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

    public void close() {
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
    }
}
