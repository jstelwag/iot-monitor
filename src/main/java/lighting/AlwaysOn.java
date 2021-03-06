package lighting;

import knx.KNXAccess;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

public class AlwaysOn {
    public AlwaysOn() {
        LogstashLogger.INSTANCE.info("Running Always on light timer");
        try {
            Schedule schedule = new Schedule();
            for (String address : schedule.alwaysOn) {
                KNXAccess.writeBoolean(new GroupAddress(address), true);
                Thread.sleep(500);
            }
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.error("Always on knx switching problem " + e.getMessage());
        }
    }
}
