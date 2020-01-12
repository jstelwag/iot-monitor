package knx;

import speaker.LogstashLogger;

public class P1EventLogger implements EventHandler {
    private final KNXAddressList addressList = new KNXAddressList();
    @Override
    public void onEvent(String event, KNXAddress knx) {
        if (knx.type == KNXAddress.Type.P1) {
            LogstashLogger.INSTANCE.message("p1-event", event);
        }
    }
}
