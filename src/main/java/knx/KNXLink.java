package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.exception.KNXException;
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

    public KNXLink() {
        HeatingProperties prop = new HeatingProperties();
        try {
            this.knxIP = new InetSocketAddress(InetAddress.getByName(prop.knxIp), prop.knxPort);
            this.localIp = new InetSocketAddress(InetAddress.getByName(prop.localIp), prop.localPort);
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.message("ERROR: could not initialize KNX link settings " + e.getMessage());
        }
    }

    public ProcessCommunicator pc() throws KNXException, InterruptedException {
        if (knxLink == null || !knxLink.isOpen()) {
            connect();
        }
        
        return pc;
    }

    private void connect() throws KNXException, InterruptedException {
        knxLink = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING
                , localIp
                , knxIP, false
                , KNXMediumSettings.create(KNXMediumSettings.MEDIUM_KNXIP, null));
        pc = new ProcessCommunicatorImpl(knxLink);
        addEventListener();
        addShutdownHook();
        System.out.println("Connecting to knx " + knxIP);
    }

    public void close() {
        System.out.println("Closing knx connection");
        if (pc != null) {
            pc.detach();
        }
        if (knxLink != null) {
            knxLink.close();
        }
    }

    private void addEventListener() {
        knxLink.addLinkListener(new KNXEventListener());
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }
}
