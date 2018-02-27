package handlers;

import building.ControllableArea;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import knx.KNXLink;
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
        String message = "";
        try (SetpointDAO setpoints = new SetpointDAO(); TemperatureDAO temperatures = new TemperatureDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                try {
                    double value = KNXLink.getInstance().readFloat(controllableArea.temperatureSensor);
                    temperatures.set(controllableArea, value);
                } catch (KNXTimeoutException e) {
                    message += "Timeout retrieving " + controllableArea + " temperature. ";
                    LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableArea + " temperature");
                    result = false;
                }
                if (controllableArea.setpoint != null) {
                    try {
                        setpoints.setKnx(controllableArea, KNXLink.getInstance().readFloat(controllableArea.setpoint));
                    } catch (KNXTimeoutException e) {
                        message += "Timeout retrieving " + controllableArea + " setpoint. ";
                        LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableArea + " setpoint");
                        result = false;
                    }
                }
            }
        } catch (KNXException | InterruptedException e) {
            message += "ERROR: closing KNX link, it is giving exceptions " + e.getMessage();
            LogstashLogger.INSTANCE.message("ERROR: closing KNX link, it is giving exceptions " + e.getMessage());
            result = false;
        }

        response.setContentType("application/json");
        if (result) {
            response.getWriter().println("{\"status\"=\"OK\"}");
        } else {
            response.getWriter().println("{\"status\"=\"ERROR\", \"message\"=\"" + message + "\"}");
        }
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
