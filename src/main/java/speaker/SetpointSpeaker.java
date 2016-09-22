package speaker;

import building.Building;
import control.HeatingControl;
import control.RoomSetpoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import util.LineProtocolUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Publishes the thermostat setpoints to InfluxDB
 */
public class SetpointSpeaker extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int count = 0;
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            RoomSetpoint setpoint = HeatingControl.INSTANCE.setpoints.get(controllableRoom);
            InfluxDBTimedSpeaker.INSTANCE.message(LineProtocolUtil.protocolLine(controllableRoom, "setpoint", Double.toString(setpoint.getSetpoint())));
        }
        count++;
        System.out.println("Posted " + count + " setpoints to InfluxDB");
    }
}
