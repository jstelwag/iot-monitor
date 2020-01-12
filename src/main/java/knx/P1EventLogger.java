package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import java.util.HashMap;
import java.util.Map;

public class P1EventLogger implements EventHandler {
    private final Map<String, String> fluxList = new HashMap<>();

    public P1EventLogger() {
        fluxList.put("0/2/151", "P1_Actual_W");
        fluxList.put("0/2/152", "P1_L1_Actual_W");
        fluxList.put("0/2/153", "P1_L2_Actual_W");
        fluxList.put("0/2/154", "P1_L3_Actual_W");
    }

    @Override
    public void onEvent(String event, KNXAddress knx) {
        if (knx.type == KNXAddress.Type.P1) {
            LogstashLogger.INSTANCE.message("p1-event", knx.address + " " + event);
                if (fluxList.containsKey(knx.address) && event.contains("tpdu 00 80")) {
                    try {
                        int p1Value = Integer.parseInt(
                            event.split("tpdu 00 80")[1].replaceAll(" ", ""), 16);
                        int knxValue = KNXAccess.readInt(new GroupAddress(knx.address));
                        LogstashLogger.INSTANCE.message("p1-event", "Match " + knx.address + " " + p1Value + " - " + knxValue);
                    } catch (KNXException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
