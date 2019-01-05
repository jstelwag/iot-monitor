package lighting;

import lighting.Schedule.Location;

public class MidnightTimer implements Runnable {

    @Override
    public void run() {
        Schedule schedule = new Schedule();
        SwitchLights.switchPublicLight(schedule.outdoorToMidnight, Location.outdoor, false);
        SwitchLights.switchPublicLight(schedule.indoorToMidnight, Location.indoor, false);
    }
}
