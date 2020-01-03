package handlers;

import building.ControllableArea;
import dao.TemperatureDAO;
import knx.KNXAccess;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXTimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 27-5-2016.
 */
public class KNXTemperaturesHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean result = true;
        StringBuilder message = new StringBuilder();
        try (TemperatureDAO temperatures = new TemperatureDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                if (controllableArea.temperatureSensor != null) {
                    try {
                        double value = KNXAccess.readFloat(controllableArea.temperatureSensor);
                        temperatures.set(controllableArea, value);
                    } catch (KNXTimeoutException e) {
                        message.append("Timeout retrieving ").append(controllableArea).append(" temperature. ");
                        LogstashLogger.INSTANCE.error("Timeout retrieving " + controllableArea + " temperature");
                        result = false;
                    }
                }
            }
        } catch (KNXException e) {
            message.append("ERROR: closing KNX link, it is giving exceptions ").append(e.getMessage());
            LogstashLogger.INSTANCE.error("Closing KNX link, it is giving exceptions " + e.getMessage());
            result = false;
        }

        response.setContentType("application/json");
        if (result) {
            response.getWriter().println("{\"status\"=\"OK\"}");
        } else {
            response.getWriter().println("{\"status\"=\"ERROR\", \"message\"=\"" + message.toString() + "\"}");
        }
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
