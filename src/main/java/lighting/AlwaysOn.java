package lighting;

import knx.KNXLink;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

public class AlwaysOn {
    public AlwaysOn() {
        LogstashLogger.INSTANCE.info("Running Always on light timer");
        try {
            Schedule schedule = new Schedule();
            for (String address : schedule.alwaysOn) {
                KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                Thread.sleep(500);
            }
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.error("Always on knx swithching problem " + e.getMessage());
        }
    }
}
