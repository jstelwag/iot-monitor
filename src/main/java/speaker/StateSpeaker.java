package speaker;

import building.Building;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Posts the HEatZone state to Influx.
 *
 * Run me periodically.
 */
public class StateSpeaker implements Runnable {

    @Override
    public void run() {
        int count = 0;
        try (FluxLogger flux = new FluxLogger(); HeatZoneStateDAO zoneStates = new HeatZoneStateDAO()) {
            for (HeatZone zone : Building.INSTANCE.zones) {
                flux.message(LineProtocolUtil.protocolLine(zone, "state"
                        , zoneStates.get(zone) ? "1i" : "0i"));
                count++;
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: sending state data to influx " + e.getMessage());
        }
        System.out.println("Posted " + count + " states to InfluxDB");
    }
}
