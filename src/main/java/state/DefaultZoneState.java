package state;

import building.Building;
import building.HeatZone;
import util.FIFODeque;

import java.util.*;

public class DefaultZoneState {

    public final static int QUEUE_LENGTH = 30;

    public static SortedMap<HeatZone, Deque<ZoneState>> populate() {
        SortedMap<HeatZone, Deque<ZoneState>> retVal = new TreeMap<>();
        Building building = Building.INSTANCE;

        for (HeatZone zone : building.zones) {
            retVal.put(zone, new FIFODeque<ZoneState>(QUEUE_LENGTH));
        }

        HeatZone zone;
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 0);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 1);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 2);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 3);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 4);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 5);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 6);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 7);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 8);
        retVal.get(zone).add(new ZoneState(zone, false));

        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 0);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 1);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 2);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 3);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 4);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 5);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 6);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 7);
        retVal.get(zone).add(new ZoneState(zone, true));

        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 8);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 9);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 10);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 11);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 12);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 13);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 14);
        retVal.get(zone).add(new ZoneState(zone, false));

        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 0);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 1);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 2);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 3);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 4);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 5);
        retVal.get(zone).add(new ZoneState(zone, false));

        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 0);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 1);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 2);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 3);
        retVal.get(zone).add(new ZoneState(zone, true));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 4);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 5);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 6);
        retVal.get(zone).add(new ZoneState(zone, false));
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 7);
        retVal.get(zone).add(new ZoneState(zone, true));

        return retVal;
    }
}
