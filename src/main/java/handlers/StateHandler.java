package handlers;

import building.HeatZone;
import control.HeatingControl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import state.ZoneState;

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

        for (HeatZone.ValveGroup group : HeatZone.ValveGroup.values()) {
            JSONObject groupsResponse = new JSONObject();
            stateResponse.put(groupsResponse);
            groupsResponse.put("name", group);
            groupsResponse.put("states", new JSONArray());
            for (ZoneState state : HeatingControl.INSTANCE.zoneStateByGroup(group)) {
                groupsResponse.getJSONArray("states").put(state);
            }
        }
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("state", stateResponse).toString(2));
        baseRequest.setHandled(true);
    }
}
