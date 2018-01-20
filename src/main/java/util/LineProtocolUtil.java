package util;

import building.*;

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
                states.add("1".equals(piece));
            }
        }
        // Last item is the checksum
        //todo remove
        if (!states.isEmpty() && !line.startsWith("kasteel_zolder")) {
            states.remove(states.size() - 1);
        }
        return states;
    }

    public static HeatZone.ValveGroup device(String line) {
        return HeatZone.ValveGroup.valueOf(line.split(":")[0]);
    }

    public static String protocolLine(HeatZone zone, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        if (zone.controllableArea != null) {
            retVal.append(zone.controllableArea).append(",position=").append(zone.position);
            if (zone.area != null) {
                retVal.append(",area=").append(escape(zone.area));
            }
            retVal.append(",sequence=").append(zone.groupSequence);
        } else {
            retVal.append(zone.group).append(",sequence=").append(zone.groupSequence).append('i');
        }
        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(HeatZone.ValveGroup group, int sequence, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append(group).append(",sequence=").append(sequence).append('i');
        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(ControllableArea controllableArea, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append(controllableArea).append(",position=room");
        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(Room room, String customer) {
        return "booking,room=" + room + " customer=\"" + customer.replace("\"", " ") +"\"";
    }


    public static String protocolLine(Furnace furnace, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append(furnace).append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String escape(String in) {
        return in.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }
}
