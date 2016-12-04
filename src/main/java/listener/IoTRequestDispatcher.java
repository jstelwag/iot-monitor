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

public class IoTRequestDispatcher {

    String lineIn;
    private HeatZone.ValveGroup group = null;

    public IoTRequestDispatcher(String lineIn) {
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

    public boolean isGroup() {
        return group != null;
    }

    public void logState() {
        List<HeatZone> zones = Building.INSTANCE.zonesByGroup(group);
        List<Boolean> clientStates = LineProtocolUtil.states(lineIn);

        if (zones.size() == clientStates.size()
                || (group == HeatZone.ValveGroup.koetshuis_trap_15 && zones.size() + 1 == clientStates.size())) {
            try (FluxLogger flux = new FluxLogger()) {
                for (int i = 0; i < zones.size(); i++) {
                    flux.message(LineProtocolUtil.protocolLine(zones.get(i), "clientState",
                            clientStates.get(i) ? "1i" : "0i"));
                }

                //TODO add logging of pump state
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
        if (device(lineIn) == HeatZone.ValveGroup.koetshuis_trap_15) {
            int pumpDesire = 0;
            for (ZoneState state : HeatingControl.INSTANCE.zoneStateByGroup(device(lineIn))) {
                if (state.valve) pumpDesire++;
            }
            int furnaceDesire = HeatingControl.INSTANCE.furnaceDesire(device(lineIn).furnace);
            boolean furnaceState = HeatingControl.INSTANCE.furnaceModulation.get(device(lineIn).furnace).control(furnaceDesire);
            response.append(furnaceState && pumpDesire >= 5 ? "1" : "0");
            //TODO shut down pump as soon as furnace is in boiler mode
        }
        response.append("E");
        return response.toString();
    }
}