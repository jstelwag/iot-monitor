package handlers;

import building.Room;
import knx.KNXAddress;
import knx.KNXAddressList;
import knx.KNXLink;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
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
 * /room/room-id/[all-off]/
 */
public class KNXRoomHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LogstashLogger.INSTANCE.info("KNX via http request " + s);
        response.setContentType("application/json");
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
                + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(s);
        JSONObject knxResponse = new JSONObject();
        if (matcher.find()) {
            Room room = Room.valueOf(matcher.group(1));
            switch (matcher.group(2)) {
                case "all-off":
                    KNXAddressList knxList = new KNXAddressList();
                    int switchCount = 0;
                    Jedis jedis = new Jedis("localhost");
                    jedis.set(room + ".all.state", "OFF");
                    for (KNXAddress address : knxList.addressesByRoom(room, KNXAddress.Type.button)) {
                        try {
                            KNXLink.getInstance().writeBoolean(new GroupAddress(address.address), false);
                            switchCount++;
                        } catch (KNXException | InterruptedException e) {
                            LogstashLogger.INSTANCE.error("Failed to switch room " + address + ", " + e.getMessage());
                        }
                    }
                    knxResponse.put("status", "OK");
                    knxResponse.put("switches", switchCount);
                    break;
                default:
                    knxResponse.put("error", "Unknown command " + matcher.group(2) + " @" + s);
                    break;
            }
        } else {
            knxResponse.put("error", "Syntax not recognized for " + s);
        }

        response.getWriter().println(knxResponse.toString(4));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
