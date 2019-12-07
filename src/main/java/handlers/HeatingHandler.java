package handlers;

import building.Building;
import building.ControllableArea;
import building.Furnace;
import building.HeatZone;
import dao.FurnaceStateDAO;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
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
 * /rest/heating/temperatue/ ... temperature input from raspi / dallas sensors
 *
 * settings are stored in Redis for a limited time.
 */
public class HeatingHandler extends AbstractHandler {
    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");

        if (s != null && s.startsWith("/valve")) {
            valveOverride(s, response.getWriter());
        } else if (s != null && s.startsWith("/furnace")) {
            furnaceOverride(s, response.getWriter());
        } else if (s != null && s.startsWith("/setpoint")) {
            roomOverride(s, response.getWriter());
        } else if (s != null && s.startsWith("/temperature")) {
            temperature(s, response.getWriter());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            LogstashLogger.INSTANCE.warn("Wrong argument at rest " + s);
        }

        response.setContentType("application/json");
        baseRequest.setHandled(true);
    }

    /**
     * /setpoint/room/increase|decrease|remove/
     */
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
                    out.print("{\"setpoint\" : " + dao.getActual(room) + "}");
                } else if ("decrease".equals(matcher.group(3))) {
                    dao.setOverride(room, dao.getActual(room) - 0.5);
                    out.print("{\"setpoint\" : " + dao.getActual(room) + "}");
                } else if ("remove".equals(matcher.group(3))) {
                    dao.removeOverride(room);
                    out.print("{\"setpoint\" : " + dao.getActual(room) + "}");
                } else {
                    out.println(lineIn + "?");
                }
            } else {
                out.print(lineIn + "?");
            }
        }
    }

    /**
     * /valve/valvegroup/sequence/on|off|remove/
     */
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
            }
        } else {
            out.println(lineIn + "?");
        }
    }

    /**
     * /furnace/furnace/on|off|remove/
     */
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

    /**
     * /temperature/controllableArea/value/
     */
    void temperature(String lineIn, PrintWriter out) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(Pattern.quote("/temperature/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn);

        if (matcher.find()) {
            ControllableArea area = ControllableArea.valueOf(matcher.group(1));
            try (TemperatureDAO tempDAO = new TemperatureDAO()) {
                if (StringUtils.isNumeric(matcher.group(2).replace(".", ""))) {
                    tempDAO.set(area, Double.parseDouble(matcher.group(2)));
                    out.println("Set " + area + ": " + matcher.group(2));
                } else {
                    LogstashLogger.INSTANCE.error("Non-numeric value for " + area + ": " + matcher.group(2));
                    out.println("Non-numeric value for " + area + ": " + matcher.group(2));
                }
            }
        } else {
            out.println(lineIn + "?");
        }
    }
}
