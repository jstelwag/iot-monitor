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
        try {
            String event = frameEvent.getFrame().toString();
            KNXAddress knx = addressList.findInString(event);
            if (knx != null) {
                switch (knx.type) {
                    case P1: //ignore
                        break;
                    case climate: //ignore
                        break;
                    default:
                        //ignore the watchdog ping messages
                        if (!(knx.type == KNXAddress.Type.button_status && knx.address.equals("4/1/103"))) {
                            LogstashLogger.INSTANCE.message("knx-event", addressList.replaceReceiverAddress(frameEvent.getFrame().toString()));
                        }
                        break;
                }
            }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Caught unexpected exception, " + e.getMessage());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.info("Eventlistener closing");
    }

}
