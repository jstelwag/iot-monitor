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
        KNXAddress knx = addressList.findInString(event);
        if (knx != null) {
            switch (knx.type) {
                case P1: //ignore
                    break;
                case climate: //ignore
                    break;
                default:
                    LogstashLogger.INSTANCE.message("knx-event", addressList.replaceReceiverAddress(frameEvent.getFrame().toString()));
                    break;
            }
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.message("INFO: KNXLink closing");
    }

}
