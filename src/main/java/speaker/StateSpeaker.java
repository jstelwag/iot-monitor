package speaker;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Posts the ZoneState to Influx.
 *
 * Run me periodically.
 */
public class StateSpeaker implements Runnable {

    public StateSpeaker() {}

    @Override
    public void run() {
        int count = 0;
        for (HeatZone zone : Building.INSTANCE.zones) {
            InfluxDBTimedSpeaker.INSTANCE.message(LineProtocolUtil.protocolLine(zone, "state"
                    , HeatingControl.INSTANCE.controlState.get(zone).peekLast().valve ? "1i" : "0i"));
            count++;
        }
        System.out.println("Posted " + count + " states to InfluxDB");
    }
}
