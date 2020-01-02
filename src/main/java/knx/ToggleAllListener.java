package knx;

import lighting.SwitchLights;
import speaker.LogstashLogger;

public class ToggleAllListener implements EventHandler {

    @Override
    public void onEvent(String event, KNXAddress knx) {
        try {
            if (knx.type == KNXAddress.Type.homeserver) {
                int switchCount = SwitchLights.toggleLights(knx.room);
                LogstashLogger.INSTANCE.info(String.format("Toggled room %s, switched %d lights", knx.room, switchCount));
            }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Caught unexpected exception, " + e.getMessage());
        }
    }
}
