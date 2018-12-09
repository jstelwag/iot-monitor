package lighting;

import knx.KNXLink;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

public class DawnTimer extends Runnable {

    @Override
    public void run() {
        Jedis jedis = new Jedis("localhost");
        if (!"OFF".equals(jedis.get("indoor.state")) && new Sun().dawn(5.0)) {
            LogstashLogger.INSTANCE.info("Switching dawn indoor - lights off");
            jedis.set("indoor.state", "OFF");
            try {
                Schedule schedule = new Schedule();
                for (String address : schedule.indoorToDawn) {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address), true);
                    Thread.sleep(500);
                }
            } catch (KNXException | InterruptedException e) {
                LogstashLogger.INSTANCE.error("Dusk time knx swithching problem " + e.getMessage());
            }
        }

        if (!"OFF".equals(jedis.get("outdoor.state")) && new Sun().dawn(0.0)) {
            LogstashLogger.INSTANCE.info("Switching dawn outdoor - lights off");
            jedis.set("outdoor.state", "OFF");
            try {
                Schedule schedule = new Schedule();
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
