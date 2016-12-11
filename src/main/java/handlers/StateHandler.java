package handlers;

import building.Building;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 27-5-2016.
 */
public class StateHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("State request");
        JSONArray stateResponse = new JSONArray();

        HeatZoneStateDAO zoneStates = new HeatZoneStateDAO();
        for (HeatZone.ValveGroup group : HeatZone.ValveGroup.values()) {
            JSONObject groupsResponse = new JSONObject();
            stateResponse.put(groupsResponse);
            groupsResponse.put("name", group);
            groupsResponse.put("states", new JSONArray());
            for (HeatZone zone : Building.INSTANCE.zonesByGroup(group)) {
                JSONObject state = new JSONObject();
                state.put("sequence", zone.groupSequence);
                state.put("value", zoneStates.get(zone));
                groupsResponse.getJSONArray("states").put(state);
            }
        }
        IOUtils.closeQuietly(zoneStates);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("state", stateResponse).toString(2));
        baseRequest.setHandled(true);
    }
}
