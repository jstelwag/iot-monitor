package lighting;

import lighting.Schedule.Location;
import lighting.SwitchLights.LightState;

public class MidnightTimer implements Runnable {

    @Override
    public void run() {
        Schedule schedule = new Schedule();
        SwitchLights.switchPublicLight(schedule.outdoorToMidnight, Location.outdoor, LightState.Midnight);
        SwitchLights.switchPublicLight(schedule.indoorToMidnight, Location.indoor, LightState.Midnight);
    }
}
