package handlers;

import knx.KNXAccess;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import speaker.FluxLogger;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Pushes p1 metrics (retrieved from KNX) to Influx
 */
public class P1Handler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("application/json");
        JSONObject knxResponse = new JSONObject();

        Map<String, GroupAddress> p1List = buildList();
        try (FluxLogger flux = new FluxLogger()) {
            for (String key : p1List.keySet()) {
                String value = KNXAccess.readString(p1List.get(key));
                BigInteger decimal = new BigInteger(value.replace(" ", ""), 16);
                knxResponse.put(key, decimal);
                flux.message("P1,metric=" + key + " value=" + decimal + "i");
            }
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.error("Failed to receive p1 values, " + e.getMessage());
        }

        response.getWriter().println(knxResponse.toString(4));
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
    }

    private Map<String, GroupAddress> buildList() {
        Map<String, GroupAddress> retVal = new HashMap<>();
        retVal.put("P1_Actual_W", new GroupAddress(0, 2, 151));
        retVal.put("P1_L1_Actual_W", new GroupAddress(0, 2, 152));
        retVal.put("P1_L2_Actual_W", new GroupAddress(0, 2, 153));
        retVal.put("P1_L3_Actual_W", new GroupAddress(0, 2, 154));

        return retVal;
    }
}
