package building;

import tuwien.auto.calimero.GroupAddress;

public enum ControllableArea {
    /** new GroupAddress(2, 5, 1) apartment_I sensor is in outer wall, too low temp */
    apartment_I(Room.apartment_I, null, new GroupAddress(2, 5, 1), 24),
    apartment_II(Room.apartment_II, new GroupAddress(1, 5, 1), new GroupAddress(1, 5, 61), 24),
    apartment_II_bedroom(Room.apartment_II, new GroupAddress(1, 5, 61), null,24),
    room_a(Room.room_a, new GroupAddress(2, 5, 11), null,36),
    room_b(Room.room_b, new GroupAddress(1, 5, 21), null,48),
    room_c(Room.room_c, new GroupAddress(1, 5, 31), null, 36),
    room_d(Room.room_d, new GroupAddress(1, 5, 41),  null,24),
    room_e(Room.room_e, new GroupAddress(1, 5, 11), null, 24),
    room_f(Room.room_f, new GroupAddress(1, 5, 70), null, 36),
    room_f_bathroom(Room.room_f, new GroupAddress(1, 5, 51), null, 36),
    //Room 1 needs to bo colder so set the default mode to Away (2)
    room_1(Room.room_1, new GroupAddress(4, 5, 1), null, 24),
    room_2(Room.room_2, null, null,24),
    room_2_bathroom(Room.room_2, new GroupAddress(6, 5, 1), null, 12),
    room_3_bathroom(Room.room_3, new GroupAddress(6, 5, 11), null,12),
    hall(Room.hall_castle, new GroupAddress(5,5,2), null,0),
    hall_toilet(Room.hall_castle, new GroupAddress(5,5,3), null,0),
    hall_storage(Room.hall_castle, null, null,0),
    office(Room.office, new GroupAddress(5,5,1), null,0),
    ballroom_south(Room.ballroom, new GroupAddress(5,5,4), null,0),
    ballroom_center(Room.ballroom, null, null,0),
//    ballroom_north(Room.ballroom, null),
    tower_kitchen(Room.kitchen, null, null,0);

    ControllableArea(Room room, GroupAddress temperatureSensor, GroupAddress secundairySensor, int preheatRampTimeHours) {
        this.room = room;
        this.temperatureSensor = temperatureSensor;
        this.secundairySensor = secundairySensor;
        this.preheatRampTimeHours = preheatRampTimeHours;
    }

    public final Room room;
    public final GroupAddress temperatureSensor;
    public final GroupAddress secundairySensor;
    public final int preheatRampTimeHours;
}
