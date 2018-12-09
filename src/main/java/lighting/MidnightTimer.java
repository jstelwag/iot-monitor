package lighting;

import knx.KNXLink;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

public class MidnightTimer implements Runnable {

    @Override
    public void run() {
        LogstashLogger.INSTANCE.info("Switching midnight - lights off");

        try {
            Schedule schedule = new Schedule();
            for (String address : schedule.outdoorToMidnight) {
                KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                Thread.sleep(500);
            }
            for (String address : schedule.indoorToMidnight) {
                KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                Thread.sleep(500);
            }
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.error("Midnight time knx swithching problem " + e.getMessage());
        }

    }
}
