package speaker;

import building.Building;
import control.HeatingControl;
import control.RoomSetpoint;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Publishes the thermostat setpoints to InfluxDB
 */
public class SetpointSpeaker implements Runnable {

    public SetpointSpeaker() {}

    @Override
    public void run() {
        int count = 0;
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            RoomSetpoint setpoint = HeatingControl.INSTANCE.setpoints.get(controllableRoom);
            InfluxDBTimedSpeaker.INSTANCE.message(LineProtocolUtil.protocolLine(controllableRoom, "setpoint", Double.toString(setpoint.getSetpoint())));
        }
        count++;
        System.out.println("Posted " + count + " setpoints to InfluxDB");
    }
}
