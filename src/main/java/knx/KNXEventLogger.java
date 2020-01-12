package knx;

import speaker.LogstashLogger;

public class KNXEventLogger implements EventHandler {
    private final KNXAddressList addressList = new KNXAddressList();
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
