package knx;

import lighting.SwitchLights;
import speaker.LogstashLogger;

public class ToggleAllListener implements EventHandler {
    private KNXAddress knx;

    @Override
    public EventHandler onEvent(String event, KNXAddress knx) {
        this.knx = knx;
        return this;
    }

    @Override
    public void run() {
        if (knx.type == KNXAddress.Type.homeserver) {
            int switchCount = SwitchLights.toggleLights(knx.room);
            LogstashLogger.INSTANCE.info(String.format("Toggled room %s, switched %d lights", knx.room, switchCount));
        }
    }
}
