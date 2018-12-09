package lighting;

import knx.KNXLink;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

public class DuskTimer extends Runnable{

    @Override
    public void run() {
        Jedis jedis = new Jedis("localhost");

        if (!"ON".equals(jedis.get("indoor.state")) && new Sun().dusk(-8.0)) {
            LogstashLogger.INSTANCE.info("Switching dusk indoor - lights on");
            jedis.set("indoor.state", "ON");

            try {
                Schedule schedule = new Schedule();
                for (String address : schedule.indoorToMidnight) {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                    Thread.sleep(500);
                }
                for (String address : schedule.indoorToDawn) {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                    Thread.sleep(500);
                }
            } catch (KNXException | InterruptedException e) {
                LogstashLogger.INSTANCE.error("Dusk time knx swithching problem " + e.getMessage());
            }
        }

        if (!"ON".equals(jedis.get("outdoor.state")) && new Sun().dusk(0.0)) {
            LogstashLogger.INSTANCE.info("Switching dusk outdoor - lights on");
            jedis.set("outdoor.state", "ON");

            try {
                Schedule schedule = new Schedule();
                for (String address : schedule.outdoorToMidnight) {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                    Thread.sleep(500);
                }
                for (String address : schedule.outdoorToDawn) {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                    Thread.sleep(500);
                }
            } catch (KNXException | InterruptedException e) {
                LogstashLogger.INSTANCE.error("Dusk time knx swithching problem " + e.getMessage());
            }
        }
    }
}
