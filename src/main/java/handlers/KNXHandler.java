package handlers;

import building.ControllableArea;
import control.HeatingControl;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
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
public class KNXHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("KNX request " + s);

        response.setContentType("application/json");
        JSONObject knxResponse = new JSONObject();

        Pattern pattern = Pattern.compile(Pattern.quote("knx/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(s);

        knxResponse.put("command", matcher.group(0));
        knxResponse.put("group", matcher.group(1) + ", " + matcher.group(2) + ", " + matcher.group(3));
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            GroupAddress address = new GroupAddress(matcher.group(1) + ", " + matcher.group(2) + ", " + matcher.group(3));
            float value = pc.readFloat(address, false);
            knxResponse.put("knx return value", value);
        } catch (KNXException | InterruptedException e) {
            knxResponse.put("error", e.getMessage());
        }
        response.getWriter().println(knxResponse.toString(2));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }
}
