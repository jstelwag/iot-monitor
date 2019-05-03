package knx;

import building.Building;
import building.Room;

/**
 * Created by Jaap on 15-12-2016.
 */
public class KNXAddress {
    /** Most KNX addresses have a series of addresses for the same device. This is the first defining address. */
    public final String address;
    public final Type type;
    public final Building.Construction construction;
    public final Room room;
    public final String capabilities;
    public final String description;

    public enum Type {
        button
        , button_status
        , window
        , dimmer_relative
        , dimmer_absolute
        , dimmer_status
        , climate
        , P1
        , homeserver
    }

    public KNXAddress(String address, Type type, Building.Construction construction, Room room, String capabilities
            , String description) {
        this.address = address;
        this.type = type;
        this.construction = construction;
        this.room = room;
        this.capabilities = capabilities;
        this.description = description;
    }


    @Override
    public String toString() {
        return room + " (" + type + ") " + description;
    }
}
