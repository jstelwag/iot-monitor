package handlers;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.FluxLogger;
import util.LineProtocolUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaap on 23-9-2016.
 */
public class FurnaceHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Pattern restPattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = restPattern.matcher(s);

        response.setContentType("application/json");
        if (matcher.find()) {
            String rawFurnace = matcher.group(1);
            Building.Furnace furnace = Building.Furnace.valueOf(rawFurnace);
            System.out.println("/furnace request: " + furnace);
            int furnaceDesire = 0;
            for (HeatZone.ValveGroup group : HeatingControl.INSTANCE.valveGroupsByFurnace(furnace)) {
                for (HeatZone zone : Building.INSTANCE.zoneByGroup(group)) {
                    if (HeatingControl.INSTANCE.controlState.get(zone).getLast().valve) {
                        furnaceDesire++;
                    }
                }
            }
            FluxLogger.INSTANCE.message(LineProtocolUtil.protocolLine(furnace, "furnaceDesire", String.valueOf(furnaceDesire)));
            response.setStatus(HttpServletResponse.SC_OK);
            if (furnaceDesire > 2) {
                response.getWriter().println("{\"furnace\"=\"ON\"}");
            } else {
                response.getWriter().println("{\"furnace\"=\"OFF\"}");
            }
        } else {
            System.out.println("/furnace request: no furnace found " + s);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"furnace\"=\"ERROR\"}");
        }
        baseRequest.setHandled(true);
    }
}
