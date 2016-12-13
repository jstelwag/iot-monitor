package building;

import java.util.ArrayList;
import java.util.List;

/**
 * The heat controller is created for my situation / building. To make the controller working for another building
 * remove this class and replace it with your own.
 */
public class Building {

    public final static Building INSTANCE = new Building();

    public enum Construction {
        koetshuis,
        kasteel
    }

    public final List<HeatZone> zones = new ArrayList<>();

    private Building() {
        makeZoneMap();
    }

    public List<HeatZone> zonesByRoom(ControllableArea controllableArea) {
        List<HeatZone> retVal = new ArrayList<>();
        for (HeatZone zone : zones) {
            if (zone.controllableArea == controllableArea) {
                retVal.add(zone);
            }
        }

        return retVal;
    }

    public HeatZone zoneById(HeatZone.ValveGroup group, int sequence) {
        for (HeatZone zone : zones) {
            if (zone.group == group && zone.groupSequence == sequence) {
                return zone;
            }
        }
        return null;
    }

    public List<HeatZone> zonesByGroup(HeatZone.ValveGroup group) {
        List<HeatZone> retVal = new ArrayList<>();
        for (HeatZone zone : zones) {
            if (zone.group == group) {
                retVal.add(zone);
            }
        }
        return retVal;
    }

    public List<ControllableArea> findRooms(Room room) {
        List<ControllableArea> retVal = new ArrayList<>();
        for (ControllableArea controlRoom : ControllableArea.values()) {
            if (controlRoom.room == room) {
                retVal.add(controlRoom);
            }
        }
        return retVal;
    }

    void makeZoneMap() {
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 0, true, ControllableArea.room_a, HeatZone.Position.floor, "bed- and bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 1, false, ControllableArea.apartment_I, HeatZone.Position.floor, "kitchen"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 2, false, ControllableArea.room_a, HeatZone.Position.wall, "west wall")); //todo: wrong?
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 3, false, null, null));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 4, true, ControllableArea.apartment_I, HeatZone.Position.floor, "living room"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 5, false, null, null));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 6, false, ControllableArea.room_a, HeatZone.Position.wall, "west wall")); //todo: wrong?
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 7, true, ControllableArea.apartment_I, HeatZone.Position.floor, "bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 8, false, ControllableArea.room_a, HeatZone.Position.wall, "east wall"));

        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 0, false, ControllableArea.apartment_II_bedroom, HeatZone.Position.wall, "shower"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 1, false));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 2, true, ControllableArea.room_c, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 3, true, ControllableArea.room_b, HeatZone.Position.wall)); //todo, move to unpreferred
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 4, true, ControllableArea.apartment_II, HeatZone.Position.floor, "sunroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 5, true, ControllableArea.room_d, HeatZone.Position.floor, "bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 6, false, ControllableArea.room_c, HeatZone.Position.wall)); //todo not determined, must be wrong
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 7, false, ControllableArea.room_d, HeatZone.Position.floor, "bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 8, false, ControllableArea.apartment_III_bathroom, HeatZone.Position.wall, "small bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 9, false, ControllableArea.room_c, HeatZone.Position.wall)); //todo seems OK
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 10, true, ControllableArea.apartment_III_bathroom, HeatZone.Position.wall, "master bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 11, true, ControllableArea.apartment_III, HeatZone.Position.floor, "sunroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 12, false, ControllableArea.room_d, HeatZone.Position.wall, "south"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 13, false, ControllableArea.room_d, HeatZone.Position.wall, "bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15, 14, false, ControllableArea.room_d, HeatZone.Position.wall, "east"));

        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 0, true, ControllableArea.apartment_III, HeatZone.Position.floor, "small bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 1, true, ControllableArea.apartment_III, HeatZone.Position.floor, "hall, living room"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 2, true, ControllableArea.apartment_II_bedroom, HeatZone.Position.floor, "bed- and bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 3, true, ControllableArea.apartment_II, HeatZone.Position.floor, "living room"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 4, true, ControllableArea.apartment_III_bathroom, HeatZone.Position.floor, "bed- and bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 5, false, null, null));

        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 0, true, ControllableArea.room_2_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 1, true, ControllableArea.room_2_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 2, true, ControllableArea.room_3_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 3, true, ControllableArea.room_3_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 4, false, ControllableArea.room_2_bathroom, HeatZone.Position.wall, "shower"));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 5, false, ControllableArea.room_2_bathroom, HeatZone.Position.wall, "left at entrance"));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 6, true, ControllableArea.room_3_bathroom, HeatZone.Position.wall, "wall separating the hall"));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 7, true, ControllableArea.room_3_bathroom, HeatZone.Position.radiator, "bedroom"));
    }
}
