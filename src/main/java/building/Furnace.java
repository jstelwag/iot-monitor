package building;

/**
 * Created by Jaap on 13-12-2016.
 */
public enum Furnace {
    koetshuis_kelder(Building.Construction.koetshuis), kasteel_torenkelder(Building.Construction.kasteel), kasteel_zolder(Building.Construction.kasteel), kasteel_kelder(Building.Construction.kasteel);

    Furnace(Building.Construction construction) {
        this.construction = construction;
    }

    public final Building.Construction construction;
}
