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
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(\\d+)" + Pattern.quote("/") + "(\\d+)"
                + Pattern.quote("/") + "(\\d+)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") );
        Matcher matcher = pattern.matcher(s);
        JSONObject knxResponse = new JSONObject();
        if (matcher.find()) {
            GroupAddress address = new GroupAddress(Integer.parseInt(matcher.group(1))
                    , Integer.parseInt(matcher.group(2))
                    , Integer.parseInt(matcher.group(3)));
            switch (matcher.group(4)) {
                case "read":
                    switch (matcher.group(5)) {
                        case "int":
                            knxResponse = getInt(address);
                            break;
                        case "float":
                            knxResponse = getFloat(address);
                            break;
                        case "boolean":
                            knxResponse = getBoolean(address);
                            break;
                        default:
                            knxResponse.put("error", "Unknown type " + matcher.group(4) + " @" + s);
                            break;
                    }
                    break;
                case "write":
                    Pattern writePat = Pattern.compile(Pattern.quote("/") + "(\\d+"
                            + Pattern.quote("/") + "\\d+" + Pattern.quote("/") + "\\d+" + Pattern.quote("/") + ".*?)"
                            + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
                    Matcher writeMatch = writePat.matcher(s);
                    if (writeMatch.find()) {
                        switch (writeMatch.group(2)) {
                            case "int":
                                int sollInt = Integer.parseInt(writeMatch.group(3));
                                knxResponse = writeInt(address, sollInt);
                                break;
                            case "float":
                                float sollFloat = Float.parseFloat(writeMatch.group(3));
                                knxResponse = writeFloat(address, sollFloat);
                                break;
                            case "boolean":
                                boolean sollBool = Boolean.parseBoolean(writeMatch.group(3));
                                knxResponse = writeBoolean(address, sollBool);
                                break;
                            default:
                                knxResponse.put("error", "Unknown type " + writeMatch.group(2) + " @" + s);
                                break;
                        }
                    } else {
                        knxResponse.put("error", "Unrecognized write command " + s);
                    }
                    break;
                default:
                    knxResponse.put("error", "Unknown command " + matcher.group(4) + " @" + s);
                    break;
            }
        } else {
            knxResponse.put("error", "Syntax not recognized for " + s);
        }

        response.getWriter().println(knxResponse.toString(4));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }

    private JSONObject getFloat(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "read/float");
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

        retVal.put("command", "read/boolean");
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

        retVal.put("command", "read/int");
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

        retVal.put("command", "write/float");
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

        retVal.put("command", "write/boolean");
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

    private JSONObject writeInt(GroupAddress address, int soll) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "write/int");
        retVal.put("group", address.toString());
        retVal.put("value", soll);
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            pc.write(address, soll, ProcessCommunicator.UNSCALED);
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }
}
