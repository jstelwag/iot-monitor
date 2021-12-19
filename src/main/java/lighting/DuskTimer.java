package lighting;

import dao.RoomOccupationDAO;
import lighting.Schedule.Location;
import lighting.SwitchLights.LightState;
import redis.clients.jedis.Jedis;

/**
 * Turns on lights in the evening. Outdoor lighting is only switched on when there is occupation.
 * Indoor is turned on earlier than outdoor.
 */
public class DuskTimer implements Runnable {

    @Override
    public void run() {
        Jedis jedis = new Jedis("localhost");
        Schedule schedule = new Schedule();

        if (!LightState.Dusk.name().equals(jedis.get(Location.indoor + ".state")) && new Sun().dusk(0.0)) {
            SwitchLights.switchPublicLight(schedule.indoorToMidnight, Location.indoor, LightState.Dusk);
            SwitchLights.switchPublicLight(schedule.indoorToDawn, Location.indoor, LightState.Dusk);
        }

        if (!LightState.Dusk.name().equals(jedis.get(Location.outdoor + ".state")) && new Sun().dusk(8.0)) {
            try (RoomOccupationDAO dao = new RoomOccupationDAO()) {
                if (dao.occupationCount() > 0) {
                    SwitchLights.switchPublicLight(schedule.outdoorToMidnight, Location.outdoor, LightState.Dusk);
                    SwitchLights.switchPublicLight(schedule.outdoorToDawn, Location.outdoor, LightState.Dusk);
                }
            }
        }
        jedis.close();
    }
}
