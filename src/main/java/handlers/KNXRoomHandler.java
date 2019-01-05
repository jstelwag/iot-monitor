package handlers;

import building.Room;
import knx.KNXAddress;
import knx.KNXAddressList;
import lighting.SwitchLights;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import speaker.LogstashLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interfacing to KNX functions of given room. Use case:
 * /room-id/[all-off|toggle]/
 * /list/
 */
public class KNXRoomHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LogstashLogger.INSTANCE.info("KNX via http request " + s);
        response.setContentType("application/json");
        Pattern pattern = Pattern.compile(Pattern.quote("/")
                + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(s);
        JSONObject knxResponse = new JSONObject();
        if (matcher.find()) {
            Room room = Room.valueOf(matcher.group(1));
            switch (matcher.group(2)) {
                case "all-off":
                    int switchCount = SwitchLights.allOff(room);
                    knxResponse.put("status", "OK");
                    knxResponse.put("lightCount", switchCount);
                    break;
                case "toggle":
                    switchCount = SwitchLights.toggleLights(room);
                    knxResponse.put("status", "OK");
                    knxResponse.put("lightCount", switchCount);
                    break;
                default:
                    knxResponse.put("error", "Unknown command " + matcher.group(2) + " @" + s);
                    break;
            }
        } else if (s.startsWith("/list")) {
            pattern = Pattern.compile(Pattern.quote("/list/") + "(.*?)" + Pattern.quote("/"));
            matcher = pattern.matcher(s);
            if (matcher.find()) {
                Room room = Room.valueOf(matcher.group(1));
                JSONObject roomResponse = new JSONObject();
                roomResponse.put("name", room);
                JSONArray lights = new JSONArray();
                roomResponse.put("lights", lights);
                for (KNXAddress address :new KNXAddressList().addressesByRoom(room, KNXAddress.Type.button)) {
                    JSONObject lightResponse = new JSONObject();
                    lights.put(lightResponse);
                    lightResponse.put("name", address.description);
                    lightResponse.put("address", address.address);
                    lightResponse.put("type", address.type);
                }
                knxResponse.put("status", "OK");
                knxResponse.put("knx", roomResponse);
            }
        } else {
            knxResponse.put("error", "Syntax not recognized for " + s);
        }

        response.getWriter().println(knxResponse.toString(4));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
