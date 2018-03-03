package building;

/**
 * Created by Jaap on 13-12-2016.
 */
public enum Room {
    apartment_I(Building.Construction.koetshuis, 13237)
    , apartment_II(Building.Construction.koetshuis, 13238)
    , room_a(Building.Construction.koetshuis, 12205)
    , room_b(Building.Construction.koetshuis, 13235)
    , room_c(Building.Construction.koetshuis, 13236)
    , room_d(Building.Construction.koetshuis, 20638)
    , room_e(Building.Construction.koetshuis, 109276)
    , room_f(Building.Construction.koetshuis, 109281)
    , room_1(Building.Construction.kasteel, 23102)
    , room_2(Building.Construction.kasteel, 22164)
    , room_3(Building.Construction.kasteel, 0)
    , laundry(Building.Construction.koetshuis, 0)
    , hall_left(Building.Construction.koetshuis, 0)
    , hall_center(Building.Construction.koetshuis, 0)
    , plein(Building.Construction.koetshuis, 0)
    , hall(Building.Construction.kasteel, 0)
    , office(Building.Construction.kasteel, 0)
    , ballroom_south(Building.Construction.kasteel, 0)
    , ballroom_north(Building.Construction.kasteel, 0)
    , kitchen(Building.Construction.kasteel, 0)
    , yet_unknown(Building.Construction.kasteel, 0); //Needed sofar for P1 meter

    Room(Building.Construction construction, long beds24Id) {
        this.construction = construction;
        this.beds24Id = beds24Id;
    }

    public final Building.Construction construction;
    public final long beds24Id;
}
