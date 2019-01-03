package handlers;

import building.Building;
import building.Furnace;
import building.HeatZone;
import control.HeatingControl;
import dao.FurnaceStateDAO;
import dao.HeatZoneStateDAO;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.FluxLogger;
import speaker.LogstashLogger;
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
            Furnace furnace = Furnace.valueOf(matcher.group(1));
            LogstashLogger.INSTANCE.info("/furnace request: " + furnace);

            int pumpDesire = 0; // TODO refactor this for every pump
            try (HeatZoneStateDAO zoneStates = new HeatZoneStateDAO()) {
                for (HeatZone zone : Building.INSTANCE.zonesByGroup(HeatZone.ValveGroup.koetshuis_kelder)) {
                    if (zoneStates.getActual(zone)) {
                        pumpDesire++;
                    }
                }
            }

            boolean furnaceState;
            int furnaceDesire = HeatingControl.INSTANCE.furnaceDesire(furnace);
            try (FurnaceStateDAO furnaceStateDAO = new FurnaceStateDAO()) {
                if (furnaceStateDAO.getOverride(furnace) != null) {
                    furnaceState = furnaceStateDAO.getOverride(furnace);
                } else {
                    furnaceState = HeatingControl.INSTANCE.furnaceModulation.get(furnace).control(furnaceDesire);
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
            if (furnaceState) {
                response.getWriter().print("{\"furnace\"=\"ON\"");
            } else {
                response.getWriter().print("{\"furnace\"=\"OFF\"");
            }
            if (furnaceState && pumpDesire >= 2) {
                response.getWriter().println(",\"pump\"=\"ON\"}");
            } else {
                response.getWriter().println(",\"pump\"=\"OFF\"}");
            }

            try (FluxLogger flux = new FluxLogger();
                 FurnaceStateDAO furnaceStateDAO = new FurnaceStateDAO()) {

                flux.message(LineProtocolUtil.protocolLine(furnace, "furnaceDesire", Integer.toString(furnaceDesire)));
                flux.message(LineProtocolUtil.protocolLine(furnace, "stateDesire", furnaceState ? "1i" : "0i"));
                if (furnaceStateDAO.getOverride(furnace) != null) {
                    flux.message(LineProtocolUtil.protocolLine(furnace, "stateOverride"
                            , furnaceStateDAO.getOverride(furnace) ? "1i" : "0i"));
                }
            }
        } else {
            LogstashLogger.INSTANCE.error("/furnace request: no furnace found " + s);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"furnace\"=\"ERROR\"}");
        }
        baseRequest.setHandled(true);
    }
}
