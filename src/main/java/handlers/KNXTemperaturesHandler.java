package handlers;

import building.ControllableArea;
import dao.TemperatureDAO;
import knx.KNXAccess;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;
import tuwien.auto.calimero.KNXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Update temperatures. When the last temperature measurement expires, retrieve a new reading via KNX
 */
public class KNXTemperaturesHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean result = true;
        StringBuilder message = new StringBuilder();
        try (TemperatureDAO temperatures = new TemperatureDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                setTemperature(temperatures, controllableArea);
            }
        } catch (KNXException e) {
            message.append("ERROR: KNX sensor or link problem, it is giving exceptions ").append(e.getMessage());
            LogstashLogger.INSTANCE.error("KNX sensor or link problem, it is giving exceptions " + e.getMessage());
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

    /**
     * Set temperature. First via the primairy sensor. If that fails, try the secondary.
     * In case there is no primary sensor (meaning, this temperature comes in via http instead of knx),
     * try the secondary if there is no temperature in the database.
     */
    public void setTemperature(TemperatureDAO temperatures, ControllableArea controllableArea) throws KNXException {
        if (controllableArea.temperatureSensor != null) {
            if (temperatures.isExpired(controllableArea)) {
                try {
                    double value = KNXAccess.readFloat(controllableArea.temperatureSensor);
                    temperatures.set(controllableArea, value);
                } catch (KNXException e) {
                    if (controllableArea.secundairySensor != null) {
                        double value = KNXAccess.readFloat(controllableArea.secundairySensor);
                        temperatures.set(controllableArea, value);
                        LogstashLogger.INSTANCE.warn("Using secondary temperature sensor (with success) for "
                                + controllableArea);
                    } else {
                        throw e;
                    }
                }
            }
        } else if (controllableArea.secundairySensor != null && !temperatures.has(controllableArea)) {
            double value = KNXAccess.readFloat(controllableArea.secundairySensor);
            temperatures.set(controllableArea, value);
            LogstashLogger.INSTANCE.warn("Using secondary temperature sensor (with success) for "
                    + controllableArea);
        }
    }
}
