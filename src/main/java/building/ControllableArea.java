package building;

import tuwien.auto.calimero.GroupAddress;

public enum ControllableArea {
    apartment_I(Room.apartment_I, new GroupAddress(2, 5, 1), null),
    apartment_II(Room.apartment_II, new GroupAddress(1, 5, 1), "1/5/8"),
    apartment_II_bedroom(Room.apartment_II, new GroupAddress(1, 5, 61), null),
    room_a(Room.room_a, new GroupAddress(2, 5, 11), null),
    room_b(Room.room_b, new GroupAddress(1, 5, 21), null),
    room_c(Room.room_c, new GroupAddress(1, 5, 31), null),
    room_d(Room.room_d, new GroupAddress(1, 5, 41), null),
    room_e(Room.room_e, new GroupAddress(1, 5, 11), "1/5/18"),
    room_f(Room.room_f, new GroupAddress(1, 5, 70), null),
    room_f_bathroom(Room.room_f, new GroupAddress(1, 5, 51), null),
    //Room 1 needs to bo colder so set the default mode to Away (2)
    room_1(Room.room_1, new GroupAddress(4, 5, 1), "4/5/6"),
    room_2(Room.room_2, new GroupAddress(6, 5, 1), null),
    room_2_bathroom(Room.room_2, new GroupAddress(6, 5, 1), null),
    room_3_bathroom(Room.room_3, new GroupAddress(6, 5, 11), null),
    hall(Room.hall, new GroupAddress(5,5,2), null),
    hall_toilet(Room.hall, new GroupAddress(5,5,3), null),
    hall_storage(Room.hall, null, null),
    office(Room.office, new GroupAddress(5,5,1), null),
    ballroom_south(Room.ballroom_south, new GroupAddress(5,5,4), null),
    ballroom_center(Room.ballroom_center, new GroupAddress(5,5,5), null),
    ballroom_north(Room.ballroom_north, null, null),
    tower_kitchen(Room.kitchen, null, null);

    ControllableArea(Room room, GroupAddress temperatureSensor, String offset) {
        this.room = room;
        this.temperatureSensor = temperatureSensor;
        this.offset = offset;
    }

    public final Room room;
    public final GroupAddress temperatureSensor;
    public final String offset;
}
