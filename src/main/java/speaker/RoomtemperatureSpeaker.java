package speaker;

import building.Building;
import dao.TemperatureDAO;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
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

        try (FluxLogger flux = new FluxLogger(); TemperatureDAO temperatures = new TemperatureDAO()) {
            for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                Double temperature = temperatures.getActual(controllableRoom);
                if (temperature != null) {
                    flux.message(LineProtocolUtil.protocolLine(controllableRoom, "temperature"
                            , Double.toString(temperature)));
                    count++;
                }
            }
        }
        System.out.println("Posted " + count + " room temperatures to InfluxDB");
        response.setContentType("application/json");
        response.getWriter().println("{\"status\"=\"OK\", \"count\"=\"" + count + "\"}");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
