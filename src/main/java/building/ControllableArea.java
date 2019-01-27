package building;

import tuwien.auto.calimero.GroupAddress;

public enum ControllableArea {
    apartment_I(Room.apartment_I, new GroupAddress(2, 5, 1)),
    apartment_II(Room.apartment_II, new GroupAddress(1, 5, 1)),
    apartment_II_bedroom(Room.apartment_II, new GroupAddress(1, 5, 61)),
    room_a(Room.room_a, new GroupAddress(2, 5, 11)),
    room_b(Room.room_b, new GroupAddress(1, 5, 21)),
    room_c(Room.room_c, new GroupAddress(1, 5, 31)),
    room_d(Room.room_d, new GroupAddress(1, 5, 41)),
    room_e(Room.room_e, new GroupAddress(1, 5, 11)),
    room_f(Room.room_f, new GroupAddress(1, 5, 70)),
    room_f_bathroom(Room.room_f, new GroupAddress(1, 5, 51)),
    //Room 1 needs to bo colder so set the default mode to Away (2)
    room_1(Room.room_1, new GroupAddress(4, 5, 1)),
    room_2(Room.room_2, new GroupAddress(6, 5, 1)),
    room_2_bathroom(Room.room_2, new GroupAddress(6, 5, 1)),
    room_3_bathroom(Room.room_3, new GroupAddress(6, 5, 11)),
    hall(Room.hall_castle, new GroupAddress(5,5,2)),
    hall_toilet(Room.hall_castle, new GroupAddress(5,5,3)),
    hall_storage(Room.hall_castle, null),
    office(Room.office, new GroupAddress(5,5,1)),
    ballroom_south(Room.ballroom_south, new GroupAddress(5,5,4)),
    ballroom_center(Room.ballroom_center, new GroupAddress(5,5,5)),
    ballroom_north(Room.ballroom_north, null),
    tower_kitchen(Room.kitchen, null);

    ControllableArea(Room room, GroupAddress temperatureSensor) {
        this.room = room;
        this.temperatureSensor = temperatureSensor;
    }

    public final Room room;
    public final GroupAddress temperatureSensor;
}
