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
                String stateDesired = zoneStates.getDesired(zone) ? "1i" : "0i";
                flux.message(LineProtocolUtil.protocolLine(zone, "stateDesired", stateDesired));
                flux.message(LineProtocolUtil.protocolLine(zone.group, zone.groupSequence, "stateDesired", stateDesired));

                String stateActual = zoneStates.getActual(zone) ? "1i" : "0i";
                flux.message(LineProtocolUtil.protocolLine(zone, "stateActual", stateActual));
                flux.message(LineProtocolUtil.protocolLine(zone.group, zone.groupSequence, "stateActual", stateActual));
                count++;
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Sending state data to influx " + e.getMessage());
        }
        LogstashLogger.INSTANCE.info("Posted " + count + " states (x4) to InfluxDB");
    }
}
