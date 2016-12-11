package control;

import building.Building;
import building.HeatZone;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import state.ZoneState;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ControlCalculator extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("Calculating state");

        SetpointDAO setpoints = new SetpointDAO();
        TemperatureDAO temperatures = new TemperatureDAO();
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            double setpoint = setpoints.get(controllableRoom);
            double roomTemperature = temperatures.get(controllableRoom);

            for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableRoom)) {
                if (HeatingControl.INSTANCE.overrides.containsKey(zone)) {
                    HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, HeatingControl.INSTANCE.overrides.get(zone)));
                } else {
                    // todo add here an optimization algorithm
                    if (setpoint < roomTemperature) {
                        HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, false));
                    } else if (setpoint - roomTemperature < 1.0) {
                        HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, zone.isPreferred));
                    } else {
                        HeatingControl.INSTANCE.controlState.get(zone).add(new ZoneState(zone, true));
                    }
                }
            }
        }
        IOUtils.closeQuietly(setpoints);
        IOUtils.closeQuietly(temperatures);

        response.setContentType("application/json");
        response.getWriter().println("{\"status\"=\"OK\"}");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
