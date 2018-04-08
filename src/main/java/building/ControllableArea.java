package building;

import tuwien.auto.calimero.GroupAddress;

public enum ControllableArea {
    apartment_I(Room.apartment_I, new GroupAddress(2, 5, 1), null, null, new GroupAddress(2, 7, 1), null, 1),
    //oldappii new GroupAddress(1, 5, 1), new GroupAddress(1, 5, 6)
    apartment_II(Room.apartment_II, null, null, "1/5/8", new GroupAddress(1, 7, 0), "1/5/3", 1),
    apartment_II_bedroom(Room.apartment_II, new GroupAddress(1, 5, 61), null, null, null, null, 1),
    room_a(Room.room_a, new GroupAddress(2, 5, 11), new GroupAddress(2, 5, 16), null, new GroupAddress(2, 7, 2), null, 1),
    room_b(Room.room_b, new GroupAddress(1, 5, 21), null, null, new GroupAddress(1, 7, 1), null, 1),
    room_c(Room.room_c, new GroupAddress(1, 5, 31), null, null, new GroupAddress(1, 7, 2), null, 1),
    room_d(Room.room_d, new GroupAddress(1, 5, 41), null, null, new GroupAddress(1, 7, 6), null, 1),
    room_e(Room.room_e, new GroupAddress(1, 5, 11), new GroupAddress(1, 5, 16), "1/5/18", new GroupAddress(1, 7, 4), "1/5/13", 1),
    room_f(Room.room_f, new GroupAddress(1, 5, 70), null, null, new GroupAddress(1, 7, 7), null, 1),
    room_f_bathroom(Room.room_f, new GroupAddress(1, 5, 51), null, null, new GroupAddress(1, 7, 5), null, 1),
    //Room 1 needs to bo colder so set the default mode to Away (2)
    room_1(Room.room_1, new GroupAddress(4, 5, 1), new GroupAddress(4, 5, 6), "4/5/6", new GroupAddress(4, 7, 0), "4/5/3", 2),
    room_2(Room.room_2, new GroupAddress(6, 5, 1), null, null, null, null, 0),
    room_2_bathroom(Room.room_2, new GroupAddress(6, 5, 1), new GroupAddress(6, 5, 6), "6/5/8", new GroupAddress(6, 7, 0), "6/5/3", 1),
    room_3_bathroom(Room.room_3, new GroupAddress(6, 5, 11), new GroupAddress(6, 5, 16), "6/5/18", null, "6/5/13", 1),
    hall(Room.hall, new GroupAddress(5,5,2), null, null, null, null, 0),
    hall_toilet(Room.hall, null, null, null, null, null, 0),
    hall_storage(Room.hall, null, null, null, null, null, 0),
    office(Room.office, new GroupAddress(5,5,1), null, null, null, null, 0),
    ballroom_south(Room.ballroom_south, null, null, null, null, null, 0),
    ballroom_north(Room.ballroom_north, null, null, null, null, null, 0),
    tower_kitchen(Room.kitchen, null, null, null, null, null, 0);

    ControllableArea(Room room, GroupAddress temperatureSensor, GroupAddress setpoint, String offset, GroupAddress allOffButton, String modusAddress, int defaultModus) {
        this.room = room;
        this.temperatureSensor = temperatureSensor;
        this.setpoint = setpoint;
        this.offset = offset;
        this.allOffButton = allOffButton;
        this.modusAddress = modusAddress;
        this.defaultModus = defaultModus;
    }

    public final Room room;
    public final GroupAddress temperatureSensor;
    public final GroupAddress setpoint;
    public final String offset;
    public final GroupAddress allOffButton;
    public final String modusAddress;
    public final int defaultModus;
}
