package building;

/**
 * Created by Jaap on 13-12-2016.
 */
public enum Room {
    apartment_I(Building.Construction.koetshuis, ControllableArea.apartment_I, 13237), apartment_II(Building.Construction.koetshuis, ControllableArea.apartment_II, 13238), apartment_III(Building.Construction.koetshuis, ControllableArea.apartment_III, 26355), room_a(Building.Construction.koetshuis, ControllableArea.room_a, 12205), room_b(Building.Construction.koetshuis, ControllableArea.room_b, 13235), room_c(Building.Construction.koetshuis, ControllableArea.room_c, 13236), room_d(Building.Construction.koetshuis, ControllableArea.room_d, 20638), room_1(Building.Construction.kasteel, ControllableArea.room_1, 23102), room_2(Building.Construction.kasteel, ControllableArea.room_2_bathroom, 22164), room_3(Building.Construction.kasteel, ControllableArea.room_3_bathroom, 0);

    Room(Building.Construction construction, ControllableArea mainRoom, long beds24Id) {
        this.construction = construction;
        this.mainRoom = mainRoom;
        this.beds24Id = beds24Id;
    }

    public final Building.Construction construction;
    public final ControllableArea mainRoom;
    public final long beds24Id;
}
