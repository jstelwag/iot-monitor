package handlers;

import building.Building;
import building.Room;
import knx.KNXAddress;
import knx.KNXAddressList;
import knx.KNXLink;
import lighting.SwitchLights;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interfacing to KNX functions of given room. Use case:
 * /room/room-id/[all-off|toggle]/
 * /list/
 */
public class KNXRoomHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LogstashLogger.INSTANCE.info("KNX via http request " + s);
        response.setContentType("application/json");
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "room" + Pattern.quote("/")
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
        } else if (s.contains("list")) {
            JSONArray buildings = new JSONArray();
            for (Building.Construction building : Building.Construction.values()) {
                JSONObject buildingResponse = new JSONObject();
                buildings.put(buildingResponse);
                JSONArray rooms = new JSONArray();
                buildingResponse.put("rooms", rooms);
                buildingResponse.put("name", building);
                for (Room room : Room.values()) {
                    if (room.construction == building) {
                        JSONObject roomResponse = new JSONObject();
                        rooms.put(roomResponse);
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
                    }
                }
            }
            knxResponse.put("status", "OK");
            knxResponse.put("buildings", buildings);

        } else {
            knxResponse.put("error", "Syntax not recognized for " + s);
        }

        response.getWriter().println(knxResponse.toString(4));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
