package state;

import building.HeatZone;
import state.TemperatureState;


public class ZoneTemperatureState extends TemperatureState implements Comparable {
    public HeatZone zone;

    public ZoneTemperatureState(HeatZone zone, double temperature) {
        super(temperature);
        this.zone = zone;
    }

    @Override
    public String toString() {
        return zone + " t:" + temperature + "C";
    }

    @Override
    public int compareTo(Object o) {
        return zone.compareTo(((ZoneTemperatureState)o).zone);
    }
}
