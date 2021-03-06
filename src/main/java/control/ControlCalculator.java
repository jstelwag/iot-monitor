package control;

import building.Building;
import building.ControllableArea;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import speaker.LogstashLogger;

import java.util.Calendar;

public class ControlCalculator implements Runnable {

    @Override
    public void run() {
        LogstashLogger.INSTANCE.info("Calculating state");

        try (SetpointDAO setpoints = new SetpointDAO();
             TemperatureDAO temperatures = new TemperatureDAO();
             HeatZoneStateDAO zoneStates = new HeatZoneStateDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                double setpoint = setpoints.getActual(controllableArea);
                double roomTemperature = temperatures.get(controllableArea);

                for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableArea)) {
                    // todo add here an optimization algorithm
                    if (!temperatures.has(controllableArea)){
                        LogstashLogger.INSTANCE.info("Guessing the temperature for " + controllableArea);
                        if (isWinter()) {
                            zoneStates.setDesired(zone, zone.isPreferred);
                        } else {
                            zoneStates.setDesired(zone, false);
                        }
                    } else if (setpoint < roomTemperature) {
                        zoneStates.setDesired(zone, false);
                    } else {
                        // heating is needed
                        if (setpoint - roomTemperature < 0.1) {
                            zoneStates.setDesired(zone, zone.isPreferred);
                        } else {
                            zoneStates.setDesired(zone, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Error at accessing Redis by controlling the temperature: " + e.getMessage());
        }
    }

    private boolean isWinter() {
        Calendar now = Calendar.getInstance();
        return (now.get(Calendar.MONTH) == Calendar.NOVEMBER
                || now.get(Calendar.MONTH) == Calendar.DECEMBER
                || now.get(Calendar.MONTH) == Calendar.JANUARY
                || now.get(Calendar.MONTH) == Calendar.FEBRUARY
                || now.get(Calendar.MONTH) == Calendar.MARCH)
                && now.get(Calendar.HOUR_OF_DAY) > 7
                && now.get(Calendar.HOUR_OF_DAY) < 22;
    }
}
