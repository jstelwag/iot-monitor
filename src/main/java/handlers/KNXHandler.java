package handlers;

import control.HeatingControl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXException;
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
        JSONObject knxResponse = process(args[1], new GroupAddress(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])));
        System.out.println(knxResponse.toString(4));
    }

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("KNX request " + s);
        response.setContentType("application/json");
        Matcher matcher = null;
        try {
            Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "([0-9]+)"
                    + Pattern.quote("/") + "([0-9]+)" + Pattern.quote("/") + "([0-9]+)" + Pattern.quote("/"));
            matcher = pattern.matcher(s);
        } catch (IllegalStateException e) {
            response.getWriter().println("Match of " + s + " failed " + e.getMessage());
        }

        if (matcher != null) {
            GroupAddress address = new GroupAddress(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
            JSONObject knxResponse = process(matcher.group(1), address);
            response.getWriter().println(knxResponse.toString(4));
        }
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
