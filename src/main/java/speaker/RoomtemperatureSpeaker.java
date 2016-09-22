package speaker;

import building.Building;
import control.HeatingControl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import state.RoomTemperatureState;
import util.LineProtocolUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Publishes the controllableRoom temperature to InfluxDB
 */
public class RoomtemperatureSpeaker extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int count = 0;

        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            RoomTemperatureState roomTemperatureState = HeatingControl.INSTANCE.roomTemperatureState.get(controllableRoom).peekLast();
            if (roomTemperatureState != null) {
                if (!roomTemperatureState.isPosted) {
                    FluxLogger.INSTANCE.message(LineProtocolUtil.protocolLine(controllableRoom, "temperature"
                            , Double.toString(roomTemperatureState.temperature)));
                    roomTemperatureState.isPosted = true;
                    count++;
                }
            }
        }
        System.out.println("Posted " + count + " room temperatures to InfluxDB");
    }
}
