package control;

import building.Building;
import building.ControllableArea;
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
        for (ControllableArea controllableArea : ControllableArea.values()) {
            double setpoint = setpoints.get(controllableArea);
            double roomTemperature = temperatures.get(controllableArea);

            for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableArea)) {
                if (HeatingControl.INSTANCE.overrides.containsKey(zone)) {
                    zoneStates.set(zone, HeatingControl.INSTANCE.overrides.get(zone));
                } else {
                    // todo add here an optimization algorithm
                    if (setpoint < roomTemperature) {
                        zoneStates.set(zone, false);
                    } else {
                        // heating is needed
                        if (setpoint - roomTemperature < 0.2) {
                            zoneStates.set(zone, zone.isPreferred);
                        } else {
                            zoneStates.set(zone, true);
                        }
                    }
                }
            }
        }
        IOUtils.closeQuietly(setpoints);
        IOUtils.closeQuietly(temperatures);
        IOUtils.closeQuietly(zoneStates);
    }
}
