package lighting;

import knx.KNXLink;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import java.util.TimerTask;

public class DuskTimer extends TimerTask {

    private final String REDIS_STATE = "dusk.state";

    @Override
    public void run() {
        Jedis jedis = new Jedis("localhost");
        LogstashLogger.INSTANCE.info("testing dusk");
        if (new Sun().dusk() && "OFF".equals(jedis.get(REDIS_STATE))) {
            LogstashLogger.INSTANCE.info("Switching dusk - lights on");
            jedis.set(REDIS_STATE, "ON");

            try {
                //3/0/106	button	garden	yet_unknown	koetshuis, buitenlamp lindeboom
                KNXLink.getInstance().writeBoolean(new GroupAddress("3/0/106"), true);
                Thread.sleep(500);
            } catch (KNXException | InterruptedException e) {
                LogstashLogger.INSTANCE.error("Dusk time knx swithching problem " + e.getMessage());
            }
        }
    }
}
