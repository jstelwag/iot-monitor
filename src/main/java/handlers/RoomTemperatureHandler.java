package handlers;

import building.ControllableArea;
import control.HeatingControl;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
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
        System.out.println("RoomTemperature request");
        int countT = 0;
        int countSP = 0;

        try (SetpointDAO setpoints = new SetpointDAO(); TemperatureDAO temperatures = new TemperatureDAO()) {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            for (ControllableArea controllableArea : ControllableArea.values()) {
                try {
                    float value = pc.readFloat(controllableArea.temperatureSensor, false);
                    temperatures.set(controllableArea, value);
                    countT++;
                } catch (KNXTimeoutException e) {
                    System.out.println("Timeout retrieving " + controllableArea + " temperature");
                }
                if (controllableArea.setpoint != null) {
                    try {
                        setpoints.setKnx(controllableArea, pc.readFloat(controllableArea.setpoint, false));
                        countSP++;
                    } catch (KNXTimeoutException e) {
                        LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableArea + " setpoint");
                        System.out.println("Timeout retrieving " + controllableArea + " setpoint");
                    }
                }
            }
        } catch (IOException | KNXException | InterruptedException e) {
            System.out.println("error " + e);
            e.printStackTrace();
            HeatingControl.INSTANCE.knxLink.close();
        }

        System.out.println("Retrieved (knx) " + countT + " room temperatures and " + countSP + " setpoints");


        response.setContentType("application/json");
        response.getWriter().println("{\"status\"=\"OK\"}");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
