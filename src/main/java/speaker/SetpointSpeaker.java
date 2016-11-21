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
import java.net.UnknownHostException;

/**
 * Publishes the thermostat setpoints to InfluxDB
 */
public class SetpointSpeaker extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int count = 0;
        try (FluxLogger flux = new FluxLogger()) {
            for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                RoomSetpoint setpoint = HeatingControl.INSTANCE.setpoints.get(controllableRoom);
                flux.message(LineProtocolUtil.protocolLine(controllableRoom, "setpoint", Double.toString(setpoint.getSetpoint())));
                count++;
            }
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.message("ERROR: can't find InfluxDB for SetpointSpeaker " + e.getMessage());
            response.setContentType("application/json");
            response.getWriter().println("{\"status\"=\"ERROR\", \"message\"=\"" + e.getMessage() + "\"}");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            baseRequest.setHandled(true);
        }
        System.out.println("Posted " + count + " setpoints to InfluxDB");
        response.setContentType("application/json");
        response.getWriter().println("{\"status\"=\"OK\", \"count\"=\"" + count + "\"}");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
