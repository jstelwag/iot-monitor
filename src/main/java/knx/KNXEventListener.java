package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.link.NetworkLinkListener;

/**
 * Simply log all KNX bus messages to Elastic
 */
public class KNXEventListener implements NetworkLinkListener {

    KNXAddressList addressList = new KNXAddressList();
    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        String event = frameEvent.getFrame().toString();
        //Ignore events to the P1 gateway
        if (!event.contains("->0/2/")) {
            LogstashLogger.INSTANCE.message("knx-event", addressList.replaceReceiverAddress(frameEvent.getFrame().toString()));
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.message("INFO: KNXLink closing");
    }

}
