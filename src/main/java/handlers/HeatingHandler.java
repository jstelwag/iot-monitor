package handlers;

import building.Building;
import building.ControllableArea;
import building.Furnace;
import building.HeatZone;
import dao.FurnaceStateDAO;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
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
 * /rest/heating/furnace/ ... overriding furnaces
 *
 * settings are stored in Redis for a limited time.
 */
public class HeatingHandler extends AbstractHandler {
    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        if (s != null && s.startsWith("/valve")) {
            valveOverride(s, response.getWriter());
        } else if (s != null && s.startsWith("/furnace")) {
            furnaceOverride(s, response.getWriter());
        } else if (s != null && s.startsWith("/setpoint")) {
            roomOverride(s, response.getWriter());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LogstashLogger.INSTANCE.warn("Wrong argument at rest " + s);
        }

        response.setContentType("application/json");
        baseRequest.setHandled(true);
    }

    /** /setpoint/room/increase|decrease|remove/ */
    void roomOverride(String lineIn, PrintWriter out) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn);

        try (SetpointDAO dao = new SetpointDAO()) {
            if (matcher.find()) {
                ControllableArea room = ControllableArea.valueOf(matcher.group(2));
                LogstashLogger.INSTANCE.info("/rest request for " + room);
                if ("increase".equals(matcher.group(3))) {
                    dao.setOverride(room, dao.getActual(room) + 0.5);
                    out.println(dao.getActual(room));
                } else if ("decrease".equals(matcher.group(3))) {
                    dao.setOverride(room, dao.getActual(room) - 0.5);
                    out.println(dao.getActual(room));
                } else if ("remove".equals(matcher.group(3))) {
                    dao.removeOverride(room);
                    out.println(dao.getActual(room));
                } else {
                    out.println(lineIn + "?");
                }
            } else {
                out.println(lineIn + "?");
            }
        }
    }

    /** /valve/valvegroup/sequence/on|off|remove/ */
    void valveOverride(String lineIn, PrintWriter out) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(Pattern.quote("/valve/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn);

        if (matcher.find() && StringUtils.isNumeric(matcher.group(2))) {
            HeatZone.ValveGroup valve = HeatZone.ValveGroup.valueOf(matcher.group(1));
            LogstashLogger.INSTANCE.info("/rest request for valve " + valve);
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

    /** /furnace/furnace/on|off|remove/ */
    void furnaceOverride(String lineIn, PrintWriter out) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(Pattern.quote("/furnace/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn);

        if (matcher.find()) {
            Furnace furnace = Furnace.valueOf(matcher.group(1));
            LogstashLogger.INSTANCE.info("/rest request for furnace " + furnace);

            try (FurnaceStateDAO stateDAO = new FurnaceStateDAO()) {
                if ("on".equals(matcher.group(2))) {
                    stateDAO.setOverride(furnace, true);
                    out.println("Override " + furnace + " on");
                } else if ("off".equals(matcher.group(2))) {
                    stateDAO.setOverride(furnace, false);
                    out.println("Override " + furnace + " off");
                } else {
                    stateDAO.removeOverride(furnace);
                    out.println("Removed override " + furnace);
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
