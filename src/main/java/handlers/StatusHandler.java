package handlers;

import building.Building;
import building.ControllableArea;
import building.HeatZone;
import building.Room;
import dao.BookingDAO;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import speaker.LogstashLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jaap on 27-5-2016.
 */
public class StatusHandler extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LogstashLogger.INSTANCE.info("Status request " + s);
        JSONObject statusResponse = new JSONObject();
        statusResponse.put("rooms", new JSONArray());
        statusResponse.put("occupiedNow", new JSONArray());
        statusResponse.put("occupiedTonight", new JSONArray());
        statusResponse.put("occupiedTomorrow", new JSONArray());

        try (SetpointDAO setpointDAO = new SetpointDAO();
                TemperatureDAO temperatureDAO = new TemperatureDAO();
                BookingDAO bookingDAO = new BookingDAO();
                HeatZoneStateDAO zoneStateDAO = new HeatZoneStateDAO()) {

            for (ControllableArea controllableArea : ControllableArea.values()) {
                JSONObject roomResponse = new JSONObject();
                statusResponse.getJSONArray("rooms").put(roomResponse);
                roomResponse.put("controllableArea", controllableArea);
                roomResponse.put("setpoint", setpointDAO.getActual(controllableArea));
                roomResponse.put("setpoint-default", setpointDAO.getDefault(controllableArea));

                if (temperatureDAO.getActual(controllableArea) != null) {
                    roomResponse.put("temperature", temperatureDAO.getActual(controllableArea));
                }

                JSONArray zones = new JSONArray();
                roomResponse.put("zones", zones);
                for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableArea)) {
                    JSONObject zoneResponse = new JSONObject();
                    zoneResponse.put("zone", zone);
                    zoneResponse.put("stateDesired", zoneStateDAO.getDesired(zone));
                    zoneResponse.put("stateActual", zoneStateDAO.getActual(zone));
                    if (zoneStateDAO.getOverride(zone) != null) {
                        zoneResponse.put("stateOverride", zoneStateDAO.getOverride(zone));
                    }
                    zones.put(zoneResponse);
                }
            }

            for (Room room : Building.INSTANCE.allControllableRooms()) {
                if (bookingDAO.isOccupiedNow(room)) {
                    JSONObject bookingNow = new JSONObject();
                    statusResponse.getJSONArray("occupiedNow").put(bookingNow);
                    bookingNow.put("name", bookingDAO.getNow(room));
                    bookingNow.put("room", room);
                }
                if (bookingDAO.isOccupiedTonight(room)) {
                    JSONObject bookingTonight = new JSONObject();
                    statusResponse.getJSONArray("occupiedTonight").put(bookingTonight);
                    bookingTonight.put("name", bookingDAO.getTonight(room));
                    bookingTonight.put("room", room);
                }
                if (bookingDAO.isOccupiedTomorrow(room)) {
                    JSONObject bookingTomorrow = new JSONObject();
                    statusResponse.getJSONArray("occupiedTomorrow").put(bookingTomorrow);
                    bookingTomorrow.put("name", bookingDAO.getTomorrow(room));
                    bookingTomorrow.put("room", room);
                }
            }
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("status", statusResponse).toString(2));
        baseRequest.setHandled(true);
    }
}
