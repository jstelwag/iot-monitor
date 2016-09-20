package speaker;

import building.Building;
import control.HeatingControl;
import state.RoomTemperatureState;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Publishes the controllableRoom temperature to InfluxDB
 */
public class RoomtemperatureSpeaker implements Runnable {

    public RoomtemperatureSpeaker() {}

    @Override
    public void run() {
        int count = 0;

        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            RoomTemperatureState roomTemperatureState = HeatingControl.INSTANCE.roomTemperatureState.get(controllableRoom).peekLast();
            if (roomTemperatureState != null) {
                if (!roomTemperatureState.isPosted) {
                    InfluxDBTimedSpeaker.INSTANCE.message(LineProtocolUtil.protocolLine(controllableRoom, "temperature"
                            , Double.toString(roomTemperatureState.temperature)));
                    roomTemperatureState.isPosted = true;
                    count++;
                }
            }
        }
        System.out.println("Posted " + count + " room temperatures to InfluxDB");
    }
}
