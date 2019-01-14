package lighting;

import lighting.Schedule.Location;
import redis.clients.jedis.Jedis;

/**
 * Scheduled in early morning to switch on the lights when it is still dark
 *
 */
public class WinterMorningTimer implements Runnable {

    @Override
    public void run() {
        Schedule schedule = new Schedule();
        if (new Sun().down(0.0)) {
            SwitchLights.switchPublicLight(schedule.indoorToMidnight, Location.indoor, true);
            SwitchLights.switchPublicLight(schedule.outdoorToMidnight, Location.outdoor, true);
        }
    }
}
