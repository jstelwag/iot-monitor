package handlers;

import building.Room;
import control.HeatingControl;
import knx.KNXAddress;
import knx.KNXAddressList;
import knx.KNXLink;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.process.ProcessCommunicator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaap on 27-5-2016.
 */
public class KNXRoomHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("KNX request " + s);
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
                    for (KNXAddress address : knxList.addressesByRoom(room, KNXAddress.Type.button)) {
                        try {
                            writeBoolean(new GroupAddress(address.address), false);
                            switchCount++;
                        } catch (KNXFormatException e) {
                            e.printStackTrace();
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

    private boolean writeBoolean(GroupAddress address, boolean soll) {
        boolean retVal = false;
        try {
            ProcessCommunicator pc = KNXLink.INSTANCE.pc();
            pc.write(address, soll);
            retVal = true;
        } catch (KNXException | InterruptedException e) {
        }

        return retVal;
    }
}
