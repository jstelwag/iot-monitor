package listener;

import building.HeatZone;
import control.HeatingControl;
import org.apache.commons.lang3.StringUtils;
import state.ZoneState;
import util.LineProtocolUtil;

import static util.LineProtocolUtil.device;

class IoTRequestDispatcher {

    String lineIn;
    IoTRequestDispatcher(String lineIn) {
        if (StringUtils.isEmpty(lineIn)) {
            System.out.println("Empty request");
            this.lineIn = "empty:void";
        } else {
            this.lineIn = lineIn.trim();
            if (isGroup()) {
                stateIn();
            } else {
                System.out.println("Unknown: " + lineIn);
            }
        }
    }

    boolean isGroup() {
        for (HeatZone.ValveGroup g : HeatZone.ValveGroup.values()) {
            if (g.name().equals(lineIn.split(":")[0])) {
                return true;
            }
        }
        return false;
    }

    private void stateIn() {
        int s = 0;
        for (boolean clientState : LineProtocolUtil.states(lineIn)) {
            //TODO dd Flux logging here
            s++;
        }
        System.out.println("Ingested " + s + " states from valve group " + device(lineIn));
    }

    public String actuatorsOut() {
        StringBuilder response = new StringBuilder();
        for (ZoneState state : HeatingControl.INSTANCE.zoneStateByGroup(device(lineIn))) {
            response.append(state.valve ? "1" : "0");
            System.out.print(state.valve ? "1" : "0");
        }
        System.out.println();
        response.append("E");
        return response.toString();
    }
}