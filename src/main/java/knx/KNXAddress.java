package knx;

import building.Building;
import building.Room;

/**
 * Created by Jaap on 15-12-2016.
 */
public class KNXAddress {
    public final String address;
    public final Type type;
    public final Building.Construction construction;
    public final Room room;
    public final String description;

    public enum Type {
        button;
    }

    public KNXAddress(String address, Type type, Building.Construction construction, Room room, String description) {
        this.address = address;
        this.type = type;
        this.construction = construction;
        this.room = room;
        this.description = description;
    }
}
