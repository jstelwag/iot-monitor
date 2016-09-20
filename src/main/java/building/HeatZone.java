package building;

public class HeatZone implements Comparable {

    public enum ValveGroup {
        koetshuis_kelder(Building.Furnace.koetshuis_kelder),
        koetshuis_trap_15L(Building.Furnace.koetshuis_kelder),
        koetshuis_trap_15R(Building.Furnace.koetshuis_kelder),
        koetshuis_trap_6(Building.Furnace.koetshuis_kelder),
        koetshuis_electric(null),
        kasteel_zolder(Building.Furnace.kasteel_torenzolder);

        ValveGroup(Building.Furnace furnace) {this.furnace = furnace;}
        public final Building.Furnace furnace;
    }
    public enum Position {
        floor, wall, room, radiator
    }

    boolean isUsed;

    public final ValveGroup group;
    public final Integer groupSequence;

    public Building.ControllableRoom controllableRoom;
    public Position position;
    public String area;

    public final boolean isPreferred;

    public HeatZone(ValveGroup group, Integer groupSequence, boolean isPreferred) {
        this.group = group;
        this.groupSequence = groupSequence;
        this.isUsed = false;
        this.isPreferred = isPreferred;
    }

    public HeatZone(ValveGroup group, Integer groupSequence, boolean isPreferred, Building.ControllableRoom controllableRoom, Position position) {
        this.group = group;
        this.groupSequence = groupSequence;
        this.isPreferred = isPreferred;
        this.controllableRoom = controllableRoom;
        this.position = position;
    }

    public HeatZone(ValveGroup group, Integer groupSequence, boolean isPreferred, Building.ControllableRoom controllableRoom, Position position, String area) {
        this.group = group;
        this.groupSequence = groupSequence;
        this.isPreferred = isPreferred;
        this.controllableRoom = controllableRoom;
        this.position = position;
        this.area = area;
    }

    @Override
    public String toString() {
        return group + "." + groupSequence + (controllableRoom != null ? " " + controllableRoom : "") + (area != null ? " " + area : "");
    }

    @Override
    public int compareTo(Object o) {
        return 100*group.compareTo(((HeatZone)o).group) + 10*groupSequence.compareTo(((HeatZone) o).groupSequence) ;
    }
}
