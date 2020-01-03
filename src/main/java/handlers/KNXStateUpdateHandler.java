package handlers;

import dao.LightingStateDAO;
import knx.KNXAddress;
import knx.KNXAddressList;
import knx.KNXLink;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * There is a copy of the knx state in Redis. Changes are monitored by listening to the KNX bus. However, the state in
 * Redis has a time to live, when that time is met, the current state is refreshed by asking the state from the knx device.
 */
public class KNXStateUpdateHandler extends AbstractHandler {

    KNXAddressList addressList = new KNXAddressList();

    public final int MIN_TTL = 100;

    final String ignoreErrorAddresses = "1/0/4 2/0/116 5/0/201";

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try (LightingStateDAO dao = new LightingStateDAO()) {
            for (KNXAddress address : addressList.addressesByType(KNXAddress.Type.button)) {
                if (dao.getStateTTL(address.address) < MIN_TTL) {
                    try {
                        if (ignoreErrorAddresses.contains(address.address)) {
                            response.getWriter().println("Ignoring " + address);
                        } else {
                            boolean state = KNXLink.getInstance().readBoolean(new GroupAddress(address.address.replace("/0/", "/1/")));
                            dao.setState(address.address, state);
                            response.getWriter().println("Updated state for " + address);
                            response.getWriter().flush();
                        }
                    } catch (KNXException | InterruptedException e) {
                        response.getWriter().println("Error occurred for " + address + ": " + e.toString());
                        response.getWriter().flush();
                        LogstashLogger.INSTANCE.error("Failed reading " + address + " while updating the state " + e.toString());
                    }
                }
            }
        }
        baseRequest.setHandled(true);
    }
}
