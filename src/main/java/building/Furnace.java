package building;

/**
 * Created by Jaap on 13-12-2016.
 */
public enum Furnace {
    koetshuis_kelder(Building.Construction.koetshuis, 10)
    , kasteel_torenkelder(Building.Construction.kasteel, 8)
    , kasteel_zolder(Building.Construction.kasteel, 10)
    , kasteel_kelder(Building.Construction.kasteel, 8);

    Furnace(Building.Construction construction, int maxActiveZones) {
        this.construction = construction;
        this.maxActiveZones = maxActiveZones;
    }

    public final Building.Construction construction;
    public final int maxActiveZones;
}
