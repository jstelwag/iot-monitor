package knx;

import dao.LightingStateDAO;
import speaker.LogstashLogger;

/**
 * Listen to status messages and update state in Redis
 */
public class KNXStateListener implements EventHandler {

    @Override
    public void onEvent(String event, KNXAddress knx) {
        try {
            //TODO test if this is correct, not sure if the tdpu codes are in order
            //https://doc.qt.io/QtKNX/qknxtpdu.html
            if (knx != null) {
                if (knx.type == KNXAddress.Type.button_status) {
                    try (LightingStateDAO dao = new LightingStateDAO()) {
                        boolean status = true;
                        if (event.endsWith("80") || event.endsWith("40")) {
                            status = false;
                        } else if (!(event.endsWith("81") || event.endsWith("41"))) {
                            LogstashLogger.INSTANCE.error("Unknown tpdu response for " + knx + " " + event);
                        }
                        dao.setState(knx.address, status);
                        LogstashLogger.INSTANCE.info(String.format("Button for %s switched to %s", knx, status));
                    }
                }
           }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Caught unexpected exception, " + e.getMessage());
        }
    }
}
