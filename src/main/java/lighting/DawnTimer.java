package lighting;

import lighting.Schedule.Location;
import redis.clients.jedis.Jedis;

public class DawnTimer implements Runnable {


    @Override
    public void run() {
        Jedis jedis = new Jedis("localhost");
        Schedule schedule = new Schedule();
        if (!"OFF".equals(jedis.get(Location.indoor + ".state")) && new Sun().dawn(5.0)) {
            SwitchLights.switchPublicLight(schedule.indoorToDawn, Location.indoor, false);
        }

        if (!"OFF".equals(jedis.get(Location.outdoor + ".state")) && new Sun().dawn(0.0)) {
            SwitchLights.switchPublicLight(schedule.outdoorToDawn, Location.outdoor, false);
        }
    }
}
