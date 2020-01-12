package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;

import java.util.HashMap;
import java.util.Map;

public class P1EventLogger implements EventHandler {
    private final KNXAddressList addressList = new KNXAddressList();
    private final Map<GroupAddress, String> fluxList = new HashMap<>();

    public P1EventLogger() {
        fluxList.put(new GroupAddress(0, 2, 151), "P1_Actual_W");
        fluxList.put(new GroupAddress(0, 2, 152), "P1_L1_Actual_W");
        fluxList.put(new GroupAddress(0, 2, 153), "P1_L2_Actual_W");
        fluxList.put(new GroupAddress(0, 2, 154), "P1_L3_Actual_W");

        fluxList.put(new GroupAddress(0, 2, 71), "P1_Low_Usage_kWh");
        fluxList.put(new GroupAddress(0, 2, 72), "P1_High_Usage_kWh");
        fluxList.put(new GroupAddress(0, 2, 73), "P1_Total_Usage_kWh");
    }
    @Override
    public void onEvent(String event, KNXAddress knx) {
        if (knx.type == KNXAddress.Type.P1) {
            LogstashLogger.INSTANCE.message("p1-event", event);
            if (fluxList.containsKey(knx.address)) {
                LogstashLogger.INSTANCE.message("p1-event", "Match " + event);
            }
        }
    }
}
