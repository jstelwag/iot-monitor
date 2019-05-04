package handlers;

import dao.LightingStateDAO;
import knx.KNXLink;
import knx.KNXAddress;
import knx.KNXAddressList;
import speaker.LogstashLogger;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

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
                        }
                    } catch (KNXException | InterruptedException e) {
                        response.getWriter().println("Error occurred for " + address + ": " + e.toString());
                        LogstashLogger.INSTANCE.error("Failed reading " + address + " while updating the state " + e.toString());
                    }
                }
            }
        }
        baseRequest.setHandled(true);
    }
}
