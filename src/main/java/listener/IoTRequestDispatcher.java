package listener;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import org.apache.commons.lang3.StringUtils;
import speaker.InfluxDBTimedSpeaker;
import speaker.LogstashTimedSpeaker;
import state.ZoneState;
import state.ZoneTemperatureState;
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
                if (hasSensors())
                    sensorsIn();
                else {
                    stateIn();
                }
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

    //TODO remove me
    boolean hasSensors() { return !"S".equals(lineIn.split(":")[1]); }

    private void sensorsIn() {
        if (!LineProtocolUtil.checksum(lineIn)) {
            System.out.println("Corrupt data (checksum failure) in " + lineIn);
            return;
        }

        int s = 0;
        for (double temperature : LineProtocolUtil.temperatures(lineIn)) {
            HeatZone zone = Building.INSTANCE.zoneById(device(lineIn), s++);
            ZoneTemperatureState state = new ZoneTemperatureState(zone, temperature);
            HeatingControl.INSTANCE.zoneTemperatureState.get(zone).add(state);
        }
        System.out.println("Ingested " + s + " temperatures from valve group " + device(lineIn));
    }

    private void stateIn() {
        int s = 0;
        for (double temperature : LineProtocolUtil.temperatures(lineIn)) {
            HeatZone zone = Building.INSTANCE.zoneById(device(lineIn), s++);
            ZoneTemperatureState state = new ZoneTemperatureState(zone, temperature);
            HeatingControl.INSTANCE.zoneTemperatureState.get(zone).add(state);
        }
        System.out.println("Ingested " + s + " temperatures from valve group " + device(lineIn));
    }

    public String actuatorsOut() {
        StringBuilder response = new StringBuilder();
        int checksum = 0;
        for (ZoneState state : HeatingControl.INSTANCE.zoneStateByGroup(device(lineIn))) {
            response.append(state.valve ? "1" : "0");
            System.out.print(state.valve ? "1" : "0");
            checksum += state.valve ? 1 : 0;
        }
        System.out.println();
        response.append(checksum).append("E");
        return response.toString();
    }
}