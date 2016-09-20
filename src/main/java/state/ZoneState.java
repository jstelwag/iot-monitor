package state;

import building.HeatZone;

import java.util.Date;

public class ZoneState implements Comparable {

    public final Date created;
    public final HeatZone zone;
    public final boolean valve;

    public ZoneState(HeatZone zone, boolean valve) {
        created = new Date();
        this.zone = zone;
        this.valve = valve;
    }

    @Override
    public String toString() {
        return zone + " valve:" + (valve ? "on" : "off");
}

    @Override
    public int compareTo(Object o) {
        return zone.compareTo(((ZoneState)o).zone);
    }
}

