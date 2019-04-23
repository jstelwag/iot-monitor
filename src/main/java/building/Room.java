package building;

/**
 * Created by Jaap on 13-12-2016.
 */
public enum Room {
    apartment_I(Building.Construction.koetshuis, 13237L)
    , apartment_II(Building.Construction.koetshuis, 13238L)
    , room_a(Building.Construction.koetshuis, 12205L)
    , room_b(Building.Construction.koetshuis, 13235L)
    , room_c(Building.Construction.koetshuis, 13236L)
    , room_d(Building.Construction.koetshuis, 20638L)
    , room_e(Building.Construction.koetshuis, 109276L)
    , room_f(Building.Construction.koetshuis, 109281L)
    , room_1(Building.Construction.kasteel, null)
    , room_2(Building.Construction.kasteel, 22164L)
    , room_3(Building.Construction.kasteel, null)
    , laundry(Building.Construction.koetshuis, null)
    , hall_coachhouse(Building.Construction.koetshuis, null)
    , outside(Building.Construction.koetshuis, null)
    , hall_castle(Building.Construction.kasteel, null)
    , hall_toilet(Building.Construction.kasteel, null)
    , office(Building.Construction.kasteel, null)
    , ballroom(Building.Construction.kasteel, null)
    , kitchen(Building.Construction.kasteel, null)
    , yet_unknown(Building.Construction.kasteel, null); //Needed sofar for P1 meter

    Room(Building.Construction construction, Long beds24Id) {
        this.construction = construction;
        this.beds24Id = beds24Id;
    }

    public final Building.Construction construction;
    public final Long beds24Id;
}
