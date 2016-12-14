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

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("KNX request " + s);
        response.setContentType("application/json");
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(\\d+)"
                + Pattern.quote("/") + "(\\d+)" + Pattern.quote("/") + "(\\d+)" + Pattern.quote("/") + ".*");
        Matcher matcher = pattern.matcher(s);
        JSONObject knxResponse = null;
        if (matcher.find()) {
            GroupAddress address = new GroupAddress(Integer.parseInt(matcher.group(2))
                    , Integer.parseInt(matcher.group(3))
                    , Integer.parseInt(matcher.group(4)));
            switch (matcher.group(1)) {
                case "float":
                    knxResponse = getFloat(address);
                    break;
                case "int":
                    knxResponse = getInt(address);
                    break;
                case "boolean":
                    knxResponse = getBoolean(address);
                    break;
                case "writeFloat":
                    Pattern floatPat = Pattern.compile(Pattern.quote("/") + "(.*?" + Pattern.quote("/") + "\\d+"
                            + Pattern.quote("/") + "\\d+" + Pattern.quote("/") + "\\d+)" + Pattern.quote("/")
                            + Pattern.quote("/") + "(([0-9]*\\.?[0-9]+)" + Pattern.quote("/"));
                    Matcher floatMatch = floatPat.matcher(s);
                    if (floatMatch.find()) {
                        float soll = Float.parseFloat(floatMatch.group(2));
                        knxResponse = writeFloat(address, soll);
                    }
                    break;
                case "writeBoolean":
                    Pattern boolPat = Pattern.compile(Pattern.quote("/") + "(.*?" + Pattern.quote("/") + "\\d+"
                            + Pattern.quote("/") + "\\d+" + Pattern.quote("/") + "\\d+)" + Pattern.quote("/")
                            + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
                    Matcher boolMatch = boolPat.matcher(s);
                    if (boolMatch.find()) {
                        boolean soll = Boolean.parseBoolean(boolMatch.group(2));
                        knxResponse = writeBoolean(address, soll);
                    }
                    break;
            }
        }

        if (knxResponse != null) {
            response.getWriter().println(knxResponse.toString(4));
        }
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }

    private JSONObject getFloat(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "getFloat");
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

    private JSONObject getBoolean(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "getBoolean");
        retVal.put("group", address.toString());
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            boolean value = pc.readBool(address);
            retVal.put("knx return value", value);
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }

    private JSONObject getInt(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "getInt");
        retVal.put("group", address.toString());
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            int value = pc.readUnsigned(address, ProcessCommunicator.UNSCALED);
            retVal.put("knx return value", value);
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }

    private JSONObject writeFloat(GroupAddress address, float soll) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "writeFloat");
        retVal.put("group", address.toString());
        retVal.put("value", soll);
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            pc.write(address, soll, true);
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }

    private JSONObject writeBoolean(GroupAddress address, boolean soll) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "writeBoolean");
        retVal.put("group", address.toString());
        retVal.put("value", soll);
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            pc.write(address, soll);
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }
}
