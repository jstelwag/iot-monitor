package util;

import building.*;

import java.util.LinkedList;
import java.util.List;

public class LineProtocolUtil {

    public static List<Boolean> states(String line) {
        List<Boolean> states = new LinkedList<>();

        boolean start = false;
        for (int i = 0; i < line.length(); i++) {
            if (!start) {
                if (line.charAt(i) == '[') {
                    start = true;
                }
            } else {
                if (line.charAt(i) == ']') {
                    break;
                } else {
                    states.add(line.charAt(i) == '1');
                }
            }
        }

        return states;
    }

    public static HeatZone.ValveGroup device(String line) {
        return HeatZone.ValveGroup.valueOf(line.split(":")[0]);
    }

    public static String protocolLine(HeatZone zone, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("valvegroup,name=").append(zone.group);
        retVal.append(",furnace=").append(zone.group.furnace);
        retVal.append(",sequence=").append(zone.groupSequence).append('i');
        if (zone.controllableArea != null) {
            retVal.append(",room=").append(zone.controllableArea).append(",position=").append(zone.position);
        }
        if (zone.area != null) {
                retVal.append(",area=").append(escape(zone.area)).append("");
        }

        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(HeatZone.ValveGroup group, int sequence, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("valvegroup,name=");
        retVal.append(group).append(",sequence=").append(sequence).append('i');
        retVal.append(",furnace=").append(group.furnace).append("");
        retVal.append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(ControllableArea controllableArea, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("room,name=").append(controllableArea).append(' ').append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String protocolLine(Room room, String customer) {
        return "booking,room=" + room + " customer=" + escape(customer).replace("\"", " ");
    }

    public static String protocolLine(Furnace furnace, String type, String value) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("furnace,name=").append(furnace).append(" ").append(type).append('=').append(value);

        return retVal.toString();
    }

    public static String escape(String in) {
        return in.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }
}
