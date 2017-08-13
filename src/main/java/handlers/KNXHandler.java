package handlers;

import knx.KNXLink;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
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
 * Interface to KNX. Use case
 * /knx/group address/[read|write]/[int|float|boolean]/[value/]
 */
public class KNXHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
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
                            knxResponse = readInt(address);
                            break;
                        case "float":
                            knxResponse = readFloat(address);
                            break;
                        case "boolean":
                            knxResponse = readBoolean(address);
                            break;
                        case "string":
                            knxResponse = readString(address);
                            break;
                        default:
                            knxResponse.put("error", "Unknown type " + matcher.group(5) + " @" + s);
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

        LogstashLogger.INSTANCE.message("KNX request " + s + " => " + knxResponse.toString(4));
        response.getWriter().println(knxResponse.toString(4));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }

    private JSONObject readFloat(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "read/float");
        retVal.put("group", address.toString());
        try {
            retVal.put("knx return value", KNXLink.getInstance().readFloat(address));
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }

    private JSONObject readBoolean(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "read/boolean");
        retVal.put("group", address.toString());
        try {
            retVal.put("knx return value", KNXLink.getInstance().readBoolean(address));
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
         }

        return retVal;
    }

    private JSONObject readInt(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "read/int");
        retVal.put("group", address.toString());
        try {
            retVal.put("knx return value", KNXLink.getInstance().readInt(address));
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }

    private JSONObject readString(GroupAddress address) {
        JSONObject retVal = new JSONObject();

        retVal.put("command", "read/string");
        retVal.put("group", address.toString());
        try {
            retVal.put("knx return value", KNXLink.getInstance().readString(address));
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
            KNXLink.getInstance().writeFloat(address, soll);
            retVal.put("status", "OK");
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
            KNXLink.getInstance().writeBoolean(address, soll);
            retVal.put("status", "OK");
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
            KNXLink.getInstance().writeInt(address, soll);
            retVal.put("status", "OK");
        } catch (KNXException | InterruptedException e) {
            retVal.put("error", e.getMessage());
        }

        return retVal;
    }
}
