package handlers;

import lighting.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import speaker.LogstashLogger;

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

        if (s != null && s.contains("midnight")) {
            try {
                new MidnightTimer().run();
                response.getWriter().print("{\"midnighttimer\"=\"OK\"}");
            } catch (Exception e) {
                LogstashLogger.INSTANCE.error("Failed with MidnightTimer " + e.getMessage());
                response.getWriter().print("{\"dawntimer\"=\"ERROR\"}");
            }
        } else if (s != null && s.contains("manual")) {
            LogstashLogger.INSTANCE.info("Manual public light switch request: " + s);
            // /timer/manual/listname/location(indoor|outdoor)/on|off/
            Pattern manualPattern = Pattern.compile(Pattern.quote("/") + "manual" + Pattern.quote("/")
                    + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
            Matcher matcher = manualPattern.matcher(s);
            if (matcher.find()) {
                boolean onOrOff = "on".equalsIgnoreCase(matcher.group(3));
                Schedule.Location location = Schedule.Location.valueOf(matcher.group(2));
                response.getWriter().print("{\"manual\"=\"" + matcher.group(1) + "\"");
                response.getWriter().print(", \"switch\"=" + matcher.group(3) + "\"");
                response.getWriter().print(", \"location\"=\"" + location + "\"");
                Schedule schedule = new Schedule();
                switch (matcher.group(1)) {
                    case "indoorToDawn":
                        SwitchLights.switchPublicLight(schedule.indoorToDawn, location, onOrOff);
                        break;
                    case "indoorToMidnight":
                        SwitchLights.switchPublicLight(schedule.indoorToMidnight, location, onOrOff);
                        break;
                    case "outdoorToDawn":
                        SwitchLights.switchPublicLight(schedule.outdoorToDawn, location, onOrOff);
                        break;
                    case "outdoorToMidnight":
                        SwitchLights.switchPublicLight(schedule.outdoorToMidnight, location, onOrOff);
                        break;
                    default:
                        response.getWriter().print("\"status\"=\"ERROR\"");
                }
                response.getWriter().print("}");
            } else {
                LogstashLogger.INSTANCE.warn("Unrecognized manual light request: " + s);
            }
        } else {
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
                response.getWriter().print("{\"dusktimer\"=\"OK\"}");
            } catch (Exception e) {
                LogstashLogger.INSTANCE.error("Failed with DuskTimer " + e.getMessage());
                response.getWriter().print("{\"dawntimer\"=\"ERROR\"}");
            }
            response.getWriter().println("]");
        }

        baseRequest.setHandled(true);
    }
}
