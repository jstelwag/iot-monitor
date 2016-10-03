package util;

import building.Building;
import building.HeatZone;

import java.util.LinkedList;
import java.util.List;

public class LineProtocolUtil {

    public static List<Boolean> states(String line) {
        List<Boolean> states = new LinkedList<>();

        boolean first = true;
        for (String piece : line.split(":")) {
            if (first) {
                first = false;
            } else {
                states.add(Boolean.parseBoolean(piece));
            }
        }
        // Last item is the checksum
        if (!states.isEmpty()) {
            states.remove(states.size() - 1);
        }
        return states;
    }

    public static HeatZone.ValveGroup device(String line) {
        return HeatZone.ValveGroup.valueOf(line.split(":")[0]);
    }

    public static String protocolLine(HeatZone zone, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        if (zone.controllableRoom != null) {
            retVal.append(zone.controllableRoom).append(",position=").append(zone.position);
            if (zone.area != null) {
                retVal.append(",area=").append(escape(zone.area));
            }
        } else {
            retVal.append(zone.group).append(",sequence=").append(zone.groupSequence).append('i');
        }
        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(Building.ControllableRoom controllableRoom, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append(controllableRoom).append(",position=room");
        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(Building.Room room, String customer) {
        return "booking,room=" + room + " customer=\"" + customer.replace("\"", " ") +"\"";
    }


    public static String protocolLine(Building.Furnace furnace, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append(furnace).append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String escape(String in) {
        return in.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }
}
