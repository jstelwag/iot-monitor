package speaker;

import building.HeatZone;
import control.HeatingControl;
import state.ZoneTemperatureState;
import util.LineProtocolUtil;

import java.io.IOException;
import java.util.List;

/**
 * Pot zone temperature data to InfluxDB
 */
public class TemperatureSpeaker implements Runnable {

    final HeatZone.ValveGroup group;

    public TemperatureSpeaker(HeatZone.ValveGroup group) {this.group = group;}

    @Override
    public void run() {
        List<ZoneTemperatureState> temperatures = HeatingControl.INSTANCE.zoneTemperatureStateByGroup(group);
        if (!temperatures.isEmpty()) {
            System.out.println("UDP temperature post for " + group);
            try (FluxLogger flux = new FluxLogger()) {
                for (ZoneTemperatureState state : temperatures) {
                    flux.message(LineProtocolUtil.protocolLine(state.zone, "temperature", Double.toString(state.temperature)));
                }
            } catch (IOException e) {
                LogstashLogger.INSTANCE.message("ERROR: connection failure while logging temperatures " + e.getMessage());
            }
        }
    }
}
