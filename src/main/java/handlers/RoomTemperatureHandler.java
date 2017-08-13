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
import tuwien.auto.calimero.process.ProcessCommunicator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 27-5-2016.
 */
public class RoomTemperatureHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        boolean result = true;
        try (SetpointDAO setpoints = new SetpointDAO(); TemperatureDAO temperatures = new TemperatureDAO()) {
            ProcessCommunicator pc = KNXLink.INSTANCE.pc();
            for (ControllableArea controllableArea : ControllableArea.values()) {
                try {
                    double value = pc.readFloat(controllableArea.temperatureSensor, false);
                    temperatures.set(controllableArea, value);
                } catch (KNXTimeoutException e) {
                    LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableArea + " temperature");
                    result = false;
                }
                if (controllableArea.setpoint != null) {
                    try {
                        setpoints.setKnx(controllableArea, pc.readFloat(controllableArea.setpoint, false));
                    } catch (KNXTimeoutException e) {
                        LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableArea + " setpoint");
                        result = false;
                    }
                }
            }
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.message("ERROR: closing KNX link, it is giving exceptions " + e.getMessage());
            KNXLink.INSTANCE.close();
            result = false;
        }

        response.setContentType("application/json");
        if (result) {
            response.getWriter().println("{\"status\"=\"OK\"}");
        } else {
            response.getWriter().println("{\"status\"=\"ERROR\"}");
        }
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
