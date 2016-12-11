package handlers;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import dao.HeatZoneStateDAO;
import org.apache.commons.io.IOUtils;
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

            int pumpDesire = 0; // TODO refactor this for every pump
            HeatZoneStateDAO zoneStates = new HeatZoneStateDAO();
            for (HeatZone zone : Building.INSTANCE.zonesByGroup(HeatZone.ValveGroup.koetshuis_kelder)) {
                if (zoneStates.get(zone)) {
                    pumpDesire++;
                }
            }
            IOUtils.closeQuietly(zoneStates);

            response.setStatus(HttpServletResponse.SC_OK);
            int furnaceDesire = HeatingControl.INSTANCE.furnaceDesire(furnace);
            boolean furnaceState = HeatingControl.INSTANCE.furnaceModulation.get(furnace).control(furnaceDesire);
            if (furnaceState) {
                response.getWriter().print("{\"furnace\"=\"ON\"");
            } else {
                response.getWriter().print("{\"furnace\"=\"OFF\"");
            }
            if (pumpDesire >= 2) {
                response.getWriter().println(",\"pump\"=\"ON\"}");
            } else {
                response.getWriter().println(",\"pump\"=\"OFF\"}");
            }
            try (FluxLogger flux = new FluxLogger()) {
                flux.message(LineProtocolUtil.protocolLine(furnace, "furnaceDesire", Integer.toString(furnaceDesire)));
                flux.message(LineProtocolUtil.protocolLine(furnace, "furnaceState", furnaceState ? "1i" : "0i"));
            }
        } else {
            System.out.println("/furnace request: no furnace found " + s);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("{\"furnace\"=\"ERROR\"}");
        }
        baseRequest.setHandled(true);
    }
}
