package listener;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import org.apache.commons.lang3.StringUtils;
import speaker.FluxLogger;
import speaker.LogstashLogger;
import state.ZoneState;
import util.LineProtocolUtil;

import java.io.IOException;
import java.util.List;

import static util.LineProtocolUtil.device;

class IoTRequestDispatcher {

    String lineIn;
    private HeatZone.ValveGroup group = null;

    IoTRequestDispatcher(String lineIn) {
        if (StringUtils.isEmpty(lineIn)) {
            System.out.println("Empty request");
            this.lineIn = "empty:void";
        } else {
            this.lineIn = lineIn.trim();
            for (HeatZone.ValveGroup g : HeatZone.ValveGroup.values()) {
                if (g.name().equals(lineIn.split(":")[0])) {
                    this.group = g;
                    break;
                }
            }
        }
    }

    boolean isGroup() {
        return group != null;
    }

    public void logState() {
        List<HeatZone> zones = Building.INSTANCE.zonesByGroup(group);
        List<Boolean> clientStates = LineProtocolUtil.states(lineIn);

        if (zones.size() == clientStates.size()) {
            try (FluxLogger flux = new FluxLogger()) {
                for (int i = 0; i < zones.size(); i++) {
                    flux.message(LineProtocolUtil.protocolLine(zones.get(i), "clientState",
                            clientStates.get(i) ? "1i" : "0i"));
                }
            } catch (IOException e) {
                LogstashLogger.INSTANCE.message("ERROR: failed to log client state " + e.getMessage());
            }
            System.out.println("Ingested " + zones.size() + " states from valve group " + device(lineIn));
        } else {
            LogstashLogger.INSTANCE.message("ERROR: size mismatch group zones and states returned by client " + device(lineIn));
            System.out.println("Unusable response, mismatch between group " + zones.size() + " and states "
                    + clientStates.size() + " from valve group " + device(lineIn));
        }
    }

    public String actuatorsOut() {
        StringBuilder response = new StringBuilder();
        for (ZoneState state : HeatingControl.INSTANCE.zoneStateByGroup(device(lineIn))) {
            response.append(state.valve ? "1" : "0");
        }
        response.append("E");
        return response.toString();
    }
}