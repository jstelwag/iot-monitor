package building;

import tuwien.auto.calimero.GroupAddress;

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

    public enum Furnace {
        koetshuis_kelder(Construction.koetshuis)
        , kasteel_torenkelder(Construction.kasteel)
        , kasteel_zolder(Construction.kasteel)
        , kasteel_kelder(Construction.kasteel);

        Furnace(Construction construction) {
            this.construction = construction;
        }

        public final Construction construction;
    }

    public enum Room {
        apartment_I(Construction.koetshuis, ControllableRoom.apartment_I, 13237)
        , apartment_II(Construction.koetshuis, ControllableRoom.apartment_II, 13238)
        , apartment_III(Construction.koetshuis, ControllableRoom.apartment_III, 26355)
        , room_a(Construction.koetshuis, ControllableRoom.room_a, 12205)
        , room_b(Construction.koetshuis, ControllableRoom.room_b, 13235)
        , room_c(Construction.koetshuis, ControllableRoom.room_c, 13236)
        , room_d(Construction.koetshuis, ControllableRoom.room_d, 20638)
        , room_1(Construction.kasteel, ControllableRoom.room_1, 23102)
        , room_2(Construction.kasteel, ControllableRoom.room_2_bathroom, 22164)
        , room_3(Construction.kasteel, ControllableRoom.room_3_bathroom, 0);
        Room(Construction construction, ControllableRoom mainRoom, long beds24Id) {
            this.construction = construction;
            this.mainRoom = mainRoom;
            this.beds24Id = beds24Id;
        }
        public final Construction construction;
        public final ControllableRoom mainRoom;
        public final long beds24Id;
    }

    public enum ControllableRoom {
        apartment_I(Room.apartment_I, new GroupAddress(2,5,1), null, new GroupAddress(2,7,1)),
        apartment_II(Room.apartment_II, new GroupAddress(1,5,1), new GroupAddress(1,5,6), new GroupAddress(1,7,0)),
        apartment_II_bedroom(Room.apartment_II, new GroupAddress(1,5,61), null, null),
        apartment_III(Room.apartment_III, new GroupAddress(1,5,11), new GroupAddress(1,5,16), new GroupAddress(1,7,4)),
        apartment_III_bathroom(Room.apartment_III, new GroupAddress(1,5,51), null, new GroupAddress(1,7,5)),
        room_a(Room.room_a, new GroupAddress(2,5,11), new GroupAddress(2,5,16), new GroupAddress(2,7,2)),
        room_b(Room.room_b, new GroupAddress(1,5,21), null, new GroupAddress(1,7,1)),
        room_c(Room.room_c, new GroupAddress(1,5,31), null, new GroupAddress(1,7,2)),
        room_d(Room.room_d, new GroupAddress(1,5,41), null, new GroupAddress(1,7,6)),
        room_1(Room.room_1, new GroupAddress(4,5,1), new GroupAddress(4,5,6), new GroupAddress(4,7,0)),
        room_2_bathroom(Room.room_2, new GroupAddress(6,5,1), new GroupAddress(6,5,6), new GroupAddress(6,7,0)),
        room_3_bathroom(Room.room_3, new GroupAddress(6,5,11), new GroupAddress(6,5,16), null); //new GroupAddress(6,7,2) for this room
        ControllableRoom(Room room, GroupAddress temperatureSensor, GroupAddress setpoint, GroupAddress allOffButton) {
            this.room = room;
            this.temperatureSensor = temperatureSensor;
            this.setpoint = setpoint;
            this.allOffButton = allOffButton;
        }
        public final Room room;
        public final GroupAddress temperatureSensor;
        public final GroupAddress setpoint;
        public final GroupAddress allOffButton;
    }

    public final List<HeatZone> zones = new ArrayList<>();

    private Building() {
        makeZoneMap();
    }

    public List<HeatZone> zonesByRoom(ControllableRoom controllableRoom) {
        List<HeatZone> retVal = new ArrayList<>();
        for (HeatZone zone : zones) {
            if (zone.controllableRoom == controllableRoom) {
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

    public List<ControllableRoom> findRooms(Room room) {
        List<ControllableRoom> retVal = new ArrayList<>();
        for (ControllableRoom controlRoom : ControllableRoom.values()) {
            if (controlRoom.room == room) {
                retVal.add(controlRoom);
            }
        }
        return retVal;
    }

    void makeZoneMap() {
        System.out.println("Drawing the heat zone plan");
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 0, true, ControllableRoom.room_a, HeatZone.Position.floor, "bed- and bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 1, false, ControllableRoom.apartment_I, HeatZone.Position.floor, "kitchen"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 2, false, ControllableRoom.room_a, HeatZone.Position.wall, "west wall")); //todo: wrong?
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 3, false, null, null));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 4, true, ControllableRoom.apartment_I, HeatZone.Position.floor, "living room"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 5, false, null, null));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 6, false, ControllableRoom.room_a, HeatZone.Position.wall, "west wall")); //todo: wrong?
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 7, true, ControllableRoom.apartment_I, HeatZone.Position.floor, "bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_kelder, 8, false, ControllableRoom.room_a, HeatZone.Position.wall, "east wall"));

        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 0, false, ControllableRoom.apartment_II_bedroom, HeatZone.Position.wall, "shower"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 1, false));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 2, true, ControllableRoom.room_c, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 3, true, ControllableRoom.room_b, HeatZone.Position.wall)); //todo, move to unpreferred
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 4, true, ControllableRoom.apartment_II, HeatZone.Position.floor, "sunroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 5, true, ControllableRoom.room_d, HeatZone.Position.floor, "bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 6, false, ControllableRoom.room_c, HeatZone.Position.wall)); //todo not determined, must be wrong
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15L, 7, false, ControllableRoom.room_d, HeatZone.Position.floor, "bedroom"));

        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 0, false, ControllableRoom.apartment_III_bathroom, HeatZone.Position.wall, "small bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 1, false, ControllableRoom.room_c, HeatZone.Position.wall)); //todo seems OK
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 2, true, ControllableRoom.apartment_III_bathroom, HeatZone.Position.wall, "master bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 3, true, ControllableRoom.apartment_III, HeatZone.Position.floor, "sunroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 4, false, ControllableRoom.room_d, HeatZone.Position.wall, "south"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 5, false, ControllableRoom.room_d, HeatZone.Position.wall, "bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_15R, 6, false, ControllableRoom.room_d, HeatZone.Position.wall, "east"));

        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 0, true, ControllableRoom.apartment_III, HeatZone.Position.floor, "small bedroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 1, true, ControllableRoom.apartment_III, HeatZone.Position.floor, "hall, living room"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 2, true, ControllableRoom.apartment_II_bedroom, HeatZone.Position.floor, "bed- and bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 3, true, ControllableRoom.apartment_II, HeatZone.Position.floor, "living room"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 4, true, ControllableRoom.apartment_III_bathroom, HeatZone.Position.floor, "bed- and bathroom"));
        zones.add(new HeatZone(HeatZone.ValveGroup.koetshuis_trap_6, 5, false, null, null));

        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 0, true, ControllableRoom.room_2_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 1, true, ControllableRoom.room_2_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 2, true, ControllableRoom.room_3_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 3, true, ControllableRoom.room_3_bathroom, HeatZone.Position.floor));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 4, false, ControllableRoom.room_2_bathroom, HeatZone.Position.wall, "shower"));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 5, false, ControllableRoom.room_2_bathroom, HeatZone.Position.wall, "left at entrance"));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 6, true, ControllableRoom.room_3_bathroom, HeatZone.Position.wall, "wall separating the hall"));
        zones.add(new HeatZone(HeatZone.ValveGroup.kasteel_zolder, 7, true, ControllableRoom.room_3_bathroom, HeatZone.Position.radiator, "bedroom"));
    }
}
