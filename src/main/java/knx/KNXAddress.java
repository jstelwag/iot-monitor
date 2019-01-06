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

    public KNXAddress(String address, Type type, Building.Construction construction, Room room, String cpabilities
            , String description) {
        this.address = address;
        this.type = type;
        this.construction = construction;
        this.room = room;
        this.capabilities = cpabilities;
        this.description = description;
    }


    @Override
    public String toString() {
        return room + " (" + type + ") " + description;
    }
}
