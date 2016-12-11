package control;

import building.Building;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.apache.commons.io.IOUtils;

public class ControlCalculator implements Runnable {

    @Override
    public void run() {
        System.out.println("Calculating state");

        SetpointDAO setpoints = new SetpointDAO();
        TemperatureDAO temperatures = new TemperatureDAO();
        HeatZoneStateDAO zoneStates = new HeatZoneStateDAO();
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            double setpoint = setpoints.get(controllableRoom);
            double roomTemperature = temperatures.get(controllableRoom);

            for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableRoom)) {
                if (HeatingControl.INSTANCE.overrides.containsKey(zone)) {
                    zoneStates.set(zone, HeatingControl.INSTANCE.overrides.get(zone));
                } else {
                    // todo add here an optimization algorithm
                    if (setpoint < roomTemperature) {
                        zoneStates.set(zone, false);
                    } else if (setpoint - roomTemperature < 1.0) {
                        zoneStates.set(zone, zone.isPreferred);
                    } else {
                        zoneStates.set(zone, true);
                    }
                }
            }
        }
        IOUtils.closeQuietly(setpoints);
        IOUtils.closeQuietly(temperatures);
        IOUtils.closeQuietly(zoneStates);
    }
}
