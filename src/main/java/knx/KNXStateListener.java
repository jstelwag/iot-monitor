package knx;

import dao.LightingStateDAO;
import speaker.LogstashLogger;

import java.io.IOException;

/**
 * Listen to status messages and update state in Redis
 */
public class KNXStateListener implements EventHandler {
    private String event;
    private KNXAddress knx;

    @Override
    public EventHandler onEvent(String event, KNXAddress knx) {
        this.event = event;
        this.knx = knx;
        return this;
    }

    @Override
    public void run() {
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
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Problem connecting with Redis at handling state event " + event + " for " + knx
                    + ", " + e.getMessage());
        }
    }
}
