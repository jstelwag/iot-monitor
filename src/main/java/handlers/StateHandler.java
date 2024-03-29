package handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import building.Building;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import speaker.LogstashLogger;

/**
 * Created by Jaap on 27-5-2016.
 */
public class StateHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LogstashLogger.INSTANCE.info("State request");
        JSONArray stateResponse = new JSONArray();

        try (HeatZoneStateDAO zoneStateDAO = new HeatZoneStateDAO()) {
            for (HeatZone.ValveGroup group : HeatZone.ValveGroup.values()) {
                JSONObject groupsResponse = new JSONObject();
                stateResponse.put(groupsResponse);
                groupsResponse.put("name", group);
                groupsResponse.put("states", new JSONArray());
                for (HeatZone zone : Building.INSTANCE.zonesByGroup(group)) {
                    JSONObject state = new JSONObject();
                    state.put("sequence", zone.groupSequence);
                    state.put("valueDesired", zoneStateDAO.getDesired(zone));
                    state.put("valueActual", zoneStateDAO.getActual(zone));
                    groupsResponse.getJSONArray("states").put(state);
                }
            }
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("state", stateResponse).toString(2));
        baseRequest.setHandled(true);
    }
}
