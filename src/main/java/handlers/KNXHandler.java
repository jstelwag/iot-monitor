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
import tuwien.auto.calimero.exception.KNXFormatException;
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

    public void run(String[] args) {
        try {
            JSONObject knxResponse = process(args[1], new GroupAddress(args[2] + ", " + args[3] + ", " + args[4]));
            System.out.println(knxResponse.toString(4));
        } catch (KNXFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("KNX request " + s);
        response.setContentType("application/json");
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(s);

        JSONObject knxResponse = new JSONObject();
        try {
            GroupAddress address = new GroupAddress(matcher.group(1) + ", " + matcher.group(2) + ", " + matcher.group(3));
            knxResponse = process(matcher.group(0), address);
        } catch (KNXException e) {
            knxResponse.put("error", e.getMessage());
        }
        response.getWriter().println(knxResponse.toString(2));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }

    private JSONObject process(String action, GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", action);
        retVal.put("group", address.toString());
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            float value = pc.readFloat(address, false);
            retVal.put("knx return value", value);
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }
}
