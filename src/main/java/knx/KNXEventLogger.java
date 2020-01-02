package knx;

import speaker.LogstashLogger;

public class KNXEventLogger implements EventHandler {
    private final KNXAddressList addressList = new KNXAddressList();
    @Override
    public void onEvent(String event, KNXAddress knx) {
        switch (knx.type) {
            case P1: //ignore
                break;
            case climate: //ignore
                break;
            default:
                //ignore the watchdog ping messages
                if (!(knx.type == KNXAddress.Type.button_status && knx.address.equals("4/1/103"))) {
                    LogstashLogger.INSTANCE.message("knx-event", addressList.replaceReceiverAddress(event));
                }
                break;
        }
    }
}
