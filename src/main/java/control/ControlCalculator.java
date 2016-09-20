package control;

import building.Building;
import building.HeatZone;
import state.ZoneState;

public class ControlCalculator implements Runnable {

    public ControlCalculator() {}

    @Override
    public void run() {
        System.out.println("Calculating state");
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            double setpoint = HeatingControl.INSTANCE.setpoints.get(controllableRoom).getSetpoint();
            double roomTemperature = HeatingControl.INSTANCE.getRoomTemperature(controllableRoom);
            for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableRoom)) {
                if (HeatingControl.INSTANCE.overrides.containsKey(zone)) {
                    HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, HeatingControl.INSTANCE.overrides.get(zone)));
                } else {
                    // todo add here an optimization algorithm
                    if (setpoint < roomTemperature) {
                        HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, false));
                    } else if (setpoint - roomTemperature < 1.0) {
                        HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, zone.isPreferred));
                    } else {
                        HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, true));
                    }
                }
            }
        }
    }
}
