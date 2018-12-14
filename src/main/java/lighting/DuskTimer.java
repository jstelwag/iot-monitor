package lighting;

import lighting.Schedule.Location;
import redis.clients.jedis.Jedis;

public class DuskTimer implements Runnable{

    @Override
    public void run() {
        Jedis jedis = new Jedis("localhost");
        Schedule schedule = new Schedule();

        if (!"ON".equals(jedis.get(Location.indoor + ".state")) && new Sun().dusk(-8.0)) {
            SwitchLights.switchLight(schedule.indoorToMidnight, Location.indoor, true);
            SwitchLights.switchLight(schedule.indoorToDawn, Location.indoor, true);
        }

        if (!"ON".equals(jedis.get("outdoor.state")) && new Sun().dusk(0.0)) {
            SwitchLights.switchLight(schedule.outdoorToMidnight, Location.outdoor, true);
            SwitchLights.switchLight(schedule.outdoorToDawn, Location.outdoor, true);
        }
    }
}
