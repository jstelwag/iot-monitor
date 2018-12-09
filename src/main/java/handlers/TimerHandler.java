package handlers;

import building.Building;
import building.Furnace;
import building.HeatZone;
import control.HeatingControl;
import dao.HeatZoneStateDAO;
import lighting.DawnTimer;
import lighting.DuskTimer;
import lighting.MidnightTimer;
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
public class TimerHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json");

        response.getWriter().print("[");
        try {
            new DawnTimer().run();
            response.getWriter().print("{\"dawntimer\"=\"OK\"}");
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Failed with DawnTimer " + e.getMessage());
            response.getWriter().print("{\"dawntimer\"=\"ERROR\"}");
        }
        response.getWriter().print(",");

        try {
            new DuskTimer().run();
            response.getWriter().print("{\"dustimer\"=\"OK\"}");
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Failed with DuskTimer " + e.getMessage());
            response.getWriter().print("{\"dawntimer\"=\"ERROR\"}");
        }
        response.getWriter().print(",");

        try {
            new MidnightTimer().run();
            response.getWriter().print("{\"midnighttimer\"=\"OK\"}");
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Failed with MidnightTimer " + e.getMessage());
            response.getWriter().print("{\"dawntimer\"=\"ERROR\"}");
        }
        response.getWriter().println("]");

        baseRequest.setHandled(true);
    }
}
