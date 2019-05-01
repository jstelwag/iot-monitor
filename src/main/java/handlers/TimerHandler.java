package handlers;

import lighting.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import speaker.LogstashLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
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

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("time", new Date());

        if (s != null && s.contains("midnight")) {
            try {
                new MidnightTimer().run();
                jsonResponse.put("midnight-timer", "OK");
            } catch (Exception e) {
                LogstashLogger.INSTANCE.error("Failed with MidnightTimer " + e.getMessage());
                jsonResponse.put("midnight-timer", "ERROR");
                jsonResponse.put("error", e.getMessage());
            }
        } else if (s != null && s.contains("winter-morning")) {
            new WinterMorningTimer().run();
            jsonResponse.put("winter-morning-timer", "OK");
        } else if (s != null && s.contains("manual")) {
            LogstashLogger.INSTANCE.info("Manual public light switch request: " + s);
            // /timer/manual/listname/location(indoor|outdoor)/on|off/
            Pattern manualPattern = Pattern.compile(Pattern.quote("/") + "manual" + Pattern.quote("/")
                    + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/") + "(.*?)" + Pattern.quote("/"));
            Matcher matcher = manualPattern.matcher(s);
            if (matcher.find()) {
                boolean onOrOff = "on".equalsIgnoreCase(matcher.group(3));
                Schedule.Location location = Schedule.Location.valueOf(matcher.group(2));
                jsonResponse.put("manual", matcher.group(1));
                jsonResponse.put("switch", matcher.group(3));
                jsonResponse.put("lights", onOrOff ? "on" : "off");
                jsonResponse.put("location", location);

                Schedule schedule = new Schedule();
                switch (matcher.group(1)) {
                    case "indoorToDawn":
                        SwitchLights.switchLights(schedule.indoorToDawn, onOrOff);
                        jsonResponse.put("status", "OK");
                        break;
                    case "indoorToMidnight":
                        SwitchLights.switchLights(schedule.indoorToMidnight, onOrOff);
                        jsonResponse.put("status", "OK");
                        break;
                    case "outdoorToDawn":
                        SwitchLights.switchLights(schedule.outdoorToDawn, onOrOff);
                        jsonResponse.put("status", "OK");
                        break;
                    case "outdoorToMidnight":
                        SwitchLights.switchLights(schedule.outdoorToMidnight, onOrOff);
                        jsonResponse.put("status", "OK");
                        break;
                    default:
                        jsonResponse.put("status", "ERROR");
                        jsonResponse.put("error", "Unknown action " + matcher.group(1));
                }

            } else {
                jsonResponse.put("status", "ERROR");
                jsonResponse.put("error", "Request not recognized " + s);
                LogstashLogger.INSTANCE.warn("Unrecognized manual light request: " + s);
            }
        } else {
            try {
                new DawnTimer().run();
                jsonResponse.put("dawn-timer", "OK");
            } catch (Exception e) {
                LogstashLogger.INSTANCE.error("Failed with DawnTimer " + e.getMessage());
                jsonResponse.put("dawn-timer", "ERROR");
                jsonResponse.put("error", e.getMessage());
            }

            try {
                new DuskTimer().run();
                jsonResponse.put("dusk-timer", "OK");
            } catch (Exception e) {
                LogstashLogger.INSTANCE.error("Failed with DuskTimer " + e.getMessage());
                jsonResponse.put("dusk-timer", "ERROR");
                jsonResponse.put("error", e.getMessage());
            }
        }

        response.getWriter().print(jsonResponse.toString());
        baseRequest.setHandled(true);
    }
}
