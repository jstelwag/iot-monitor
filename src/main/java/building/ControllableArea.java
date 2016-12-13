package building;

import tuwien.auto.calimero.GroupAddress;

public enum ControllableArea {
    apartment_I(Room.apartment_I, new GroupAddress(2, 5, 1), null, new GroupAddress(2, 7, 1)),
    apartment_II(Room.apartment_II, new GroupAddress(1, 5, 1), new GroupAddress(1, 5, 6), new GroupAddress(1, 7, 0)),
    apartment_II_bedroom(Room.apartment_II, new GroupAddress(1, 5, 61), null, null),
    apartment_III(Room.apartment_III, new GroupAddress(1, 5, 11), new GroupAddress(1, 5, 16), new GroupAddress(1, 7, 4)),
    apartment_III_bathroom(Room.apartment_III, new GroupAddress(1, 5, 51), null, new GroupAddress(1, 7, 5)),
    room_a(Room.room_a, new GroupAddress(2, 5, 11), new GroupAddress(2, 5, 16), new GroupAddress(2, 7, 2)),
    room_b(Room.room_b, new GroupAddress(1, 5, 21), null, new GroupAddress(1, 7, 1)),
    room_c(Room.room_c, new GroupAddress(1, 5, 31), null, new GroupAddress(1, 7, 2)),
    room_d(Room.room_d, new GroupAddress(1, 5, 41), null, new GroupAddress(1, 7, 6)),
    room_1(Room.room_1, new GroupAddress(4, 5, 1), new GroupAddress(4, 5, 6), new GroupAddress(4, 7, 0)),
    room_2_bathroom(Room.room_2, new GroupAddress(6, 5, 1), new GroupAddress(6, 5, 6), new GroupAddress(6, 7, 0)),
    room_3_bathroom(Room.room_3, new GroupAddress(6, 5, 11), new GroupAddress(6, 5, 16), null); //new GroupAddress(6,7,2) for this room

    ControllableArea(Room room, GroupAddress temperatureSensor, GroupAddress setpoint, GroupAddress allOffButton) {
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
