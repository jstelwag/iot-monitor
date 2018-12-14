package lighting;

import lighting.Schedule.Location;

public class MidnightTimer implements Runnable {

    @Override
    public void run() {
        Schedule schedule = new Schedule();
        SwitchLights.switchLight(schedule.outdoorToMidnight, Location.outdoor, false);
        SwitchLights.switchLight(schedule.indoorToMidnight, Location.indoor, false);
    }
}
