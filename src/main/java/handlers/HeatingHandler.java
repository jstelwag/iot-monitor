package handlers;

import building.Building;
import building.ControllableArea;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rest interface for heating:
 * /rest/heating/setpoint/... setting the setpoint manually
 * /rest/heating/valve/... setting individual valves
 *
 * settings are stored in Redis for a limited time.
 */
public class HeatingHandler extends AbstractHandler {
    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        if (s != null && s.startsWith("/valve")) {
            matchValveOverride(s, response.getWriter());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LogstashLogger.INSTANCE.warn("Wrong argument at rest " + s);
        }

        response.setContentType("application/json");
        baseRequest.setHandled(true);
    }

    /** /rest/room/on|off|toggle/ */
    void matchRoom(String roomText, String lineIn, PrintWriter out) throws IllegalArgumentException {
        ControllableArea room = ControllableArea.valueOf(roomText);
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn + "/");
        SetpointDAO dao = new SetpointDAO();
        if (matcher.find()) {
            LogstashLogger.INSTANCE.info("/rest request for " + room);
            if ("toggle".equals(matcher.group(2))) {
                dao.setActive(room, !dao.isActive(room));
                out.println("Toggled room " + room + " to " + dao.isActive(room));
            } else {
                dao.setActive(room, "on".equals(matcher.group(2)));
                out.println("Switched room " + room + " " + matcher.group(2));
            }
        } else {
            out.println(lineIn + "?");
        }
        IOUtils.closeQuietly(dao);
    }

    /** /valve/valvegroup/sequence/on|off|remove/ */
    void matchValveOverride(String lineIn, PrintWriter out) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(Pattern.quote("/valve/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn);

        if (matcher.find() && StringUtils.isNumeric(matcher.group(2))) {
            HeatZone.ValveGroup valve = HeatZone.ValveGroup.valueOf(matcher.group(1));
            LogstashLogger.INSTANCE.info("/rest request for " + valve);
            int sequence = Integer.parseInt(matcher.group(2));
            HeatZone zone = Building.INSTANCE.zoneById(valve, sequence);

            try (HeatZoneStateDAO stateDAO = new HeatZoneStateDAO()) {
                if ("on".equals(matcher.group(3))) {
                    stateDAO.setOverride(zone, true);
                    out.println("Override " + zone + " on");
                } else if ("off".equals(matcher.group(3))) {
                    stateDAO.setOverride(zone, false);
                    out.println("Override " + zone + " off");
                } else {
                    stateDAO.removeOverride(zone);
                    out.println("Removed override " + zone);
                }
            } catch (IOException e) {
                LogstashLogger.INSTANCE.error("Can't connect with state dao, " + e.getMessage());
                out.println("Error: " + e.getMessage());
            }
        } else {
            out.println(lineIn + "?");
        }
    }
}
