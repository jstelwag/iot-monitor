package speaker;

import building.Building;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Posts the HeatZone state to Influx.
 *
 * Run me periodically.
 */
public class StateSpeaker implements Runnable {

    @Override
    public void run() {
        int count = 0;
        try (FluxLogger flux = new FluxLogger(); HeatZoneStateDAO zoneStates = new HeatZoneStateDAO()) {
            for (HeatZone zone : Building.INSTANCE.zones) {
                String state = zoneStates.get(zone) ? "1i" : "0i";
                flux.message(LineProtocolUtil.protocolLine(zone, "state", state));
                flux.message(LineProtocolUtil.protocolLine(zone.group, zone.groupSequence, "state", state));
                count++;
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Sending state data to influx " + e.getMessage());
        }
        LogstashLogger.INSTANCE.info("Posted " + count + " states (x2) to InfluxDB");
    }
}
