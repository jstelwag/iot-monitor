package knx;

import speaker.LogstashLogger;
import dao.LightingStateDAO;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;

/**
 * Listen to status messages and update state in Redis
 */
public class KNXStateListener implements NetworkLinkListener {

    KNXAddressList addressList = new KNXAddressList();

    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
       try {
            String event = frameEvent.getFrame().toString();
            KNXAddress knx = addressList.findInString(event);

            if (knx != null) {
                if (knx.type == KNXAddress.Type.button_status) {
                    try (LightingStateDAO dao = new LightingStateDAO()) {
                        boolean status = true;
                        if (event.endsWith("80")) {
                            status = false;
                        } else if (!event.endsWith("81")) {
                            LogstashLogger.INSTANCE.error("Unknown tpdu response for " + knx + " " + event);
                        }
                        dao.setState(knx.address, status);
                        LogstashLogger.INSTANCE.info("Button for " + knx + " with value " + status);
                    }
                }
            }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Caught unexpected exception, " + e.getMessage());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.info("State listener closing");
    }
}
