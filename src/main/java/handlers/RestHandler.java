package handlers;

import building.Building;
import building.ControllableArea;
import building.HeatZone;
import control.HeatingControl;
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
 * Created by Jaap on 27-5-2016.
 */
public class RestHandler extends AbstractHandler {
    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Pattern restPattern = Pattern.compile(Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = restPattern.matcher(s);

        if (matcher.find()) {
            String roomOrValve = matcher.group(1);
            try {
                matchRoom(roomOrValve, s, response.getWriter());
            } catch (IllegalArgumentException e) {
                matchValveOverride(roomOrValve, s, response.getWriter());
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        baseRequest.setHandled(true);
    }

    /** /rest/room/on|off|toggle/ */
    void matchRoom(String roomText, String lineIn, PrintWriter out) throws IllegalArgumentException {
        ControllableArea room = ControllableArea.valueOf(roomText);
        Pattern pattern = Pattern.compile(Pattern.quote("rest/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
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

    /** /rest/valvegroup/sequence/on|off|remove/ */
    void matchValveOverride(String valveText, String lineIn, PrintWriter out) throws IllegalArgumentException {
        HeatZone.ValveGroup valve = HeatZone.ValveGroup.valueOf(valveText);
        Pattern pattern = Pattern.compile(Pattern.quote("rest/") + "(.*?)" + Pattern.quote("/") + "(.*?)"
                + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
        Matcher matcher = pattern.matcher(lineIn);
        if (matcher.find() && StringUtils.isNumeric(matcher.group(2))) {
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
