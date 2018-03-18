package building;

public class HeatZone implements Comparable {

    public enum ValveGroup {
        koetshuis_kelder(Furnace.koetshuis_kelder),
        koetshuis_trap_15(Furnace.koetshuis_kelder),
        koetshuis_trap_6(Furnace.koetshuis_kelder),
        kasteel_zolder(Furnace.kasteel_zolder),
        kasteel_torenkelder(Furnace.kasteel_torenkelder),
        kasteel_hal(Furnace.kasteel_torenkelder);

        ValveGroup(Furnace furnace) {this.furnace = furnace;}
        public final Furnace furnace;
    }

    public enum Position {
        floor, wall, room, radiator
    }

    boolean isUsed;

    public final ValveGroup group;
    public final Integer groupSequence;

    public ControllableArea controllableArea;
    public Position position;
    public String area;

    public final boolean isPreferred;

    public HeatZone(ValveGroup group, Integer groupSequence, boolean isPreferred) {
        this.group = group;
        this.groupSequence = groupSequence;
        this.isUsed = false;
        this.isPreferred = isPreferred;
    }

    public HeatZone(ValveGroup group, Integer groupSequence, boolean isPreferred, ControllableArea controllableArea, Position position) {
        this.group = group;
        this.groupSequence = groupSequence;
        this.isPreferred = isPreferred;
        this.controllableArea = controllableArea;
        this.position = position;
    }

    public HeatZone(ValveGroup group, Integer groupSequence, boolean isPreferred, ControllableArea controllableArea, Position position, String area) {
        this.group = group;
        this.groupSequence = groupSequence;
        this.isPreferred = isPreferred;
        this.controllableArea = controllableArea;
        this.position = position;
        this.area = area;
    }

    @Override
        public String toString() {
        return group + "." + groupSequence + (controllableArea != null ? " " + controllableArea : "") + (area != null ? " " + area : "");
    }

    @Override
    public int compareTo(Object o) {
        return 100*group.compareTo(((HeatZone)o).group) + 10*groupSequence.compareTo(((HeatZone) o).groupSequence) ;
    }
}
