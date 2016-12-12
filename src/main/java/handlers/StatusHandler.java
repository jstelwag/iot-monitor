package handlers;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import dao.BookingDAO;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

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
        System.out.println("Status request");
        JSONObject statusResponse = new JSONObject();
        statusResponse.put("rooms", new JSONArray());
        statusResponse.put("occupiedNow", new JSONArray());
        statusResponse.put("occupiedTonight", new JSONArray());

        SetpointDAO setpoints = new SetpointDAO();
        TemperatureDAO temperatures = new TemperatureDAO();
        HeatZoneStateDAO zoneStates = new HeatZoneStateDAO();
        BookingDAO bookings = new BookingDAO();
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            JSONObject roomResponse = new JSONObject();
            statusResponse.getJSONArray("rooms").put(roomResponse);
            roomResponse.put("controllableRoom", controllableRoom);
            roomResponse.put("setpoint", setpoints.get(controllableRoom));
            if (setpoints.getUser(controllableRoom) != null) {
                roomResponse.put("setpoint-user", setpoints.getUser(controllableRoom));
            }
            if (setpoints.getKnx(controllableRoom) != null) {
                roomResponse.put("setpoint-knx", setpoints.getKnx(controllableRoom));
            }
            roomResponse.put("setpoint-default", setpoints.getDefault(controllableRoom));
            roomResponse.put("active", setpoints.isActive(controllableRoom));
            roomResponse.put("booking-now", bookings.getNow(controllableRoom.room));
            roomResponse.put("booking-tonight", bookings.getTonight(controllableRoom.room));

            if (temperatures.getActual(controllableRoom) != null) {
                roomResponse.put("temperature", temperatures.getActual(controllableRoom));
            }

            JSONArray zones = new JSONArray();
            roomResponse.put("zones", zones);
            for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableRoom)) {
                JSONObject zoneResponse = new JSONObject();
                zoneResponse.put("zone", zone);
                zoneResponse.put("state", zoneStates.get(zone));
                zones.put(zoneResponse);
            }

            JSONArray overrides = new JSONArray();
            roomResponse.put("overrides", overrides);
            for (HeatZone zone : HeatingControl.INSTANCE.overridesByRoom(controllableRoom)) {
                JSONObject zoneResponse = new JSONObject();
                zoneResponse.put("zone", zone);
                overrides.put(zoneResponse);
                zoneResponse.put("override", HeatingControl.INSTANCE.overrides.get(zone));
            }
        }
        IOUtils.closeQuietly(setpoints);
        IOUtils.closeQuietly(temperatures);
        IOUtils.closeQuietly(zoneStates);

        for (Building.Room room : Building.Room.values()) {
            if (bookings.isOccupiedNow(room)) {
                JSONObject bookingNow = new JSONObject();
                statusResponse.getJSONArray("occupiedNow").put(bookingNow);
                bookingNow.put("name", bookings.getNow(room));
                bookingNow.put("room", room);
            }
            if (bookings.isOccupiedTonight(room)) {
                JSONObject bookingTonight = new JSONObject();
                statusResponse.getJSONArray("occupiedTonight").put(bookingTonight);
                bookingTonight.put("name", bookings.getTonight(room));
                bookingTonight.put("room", room);
            }
        }
        IOUtils.closeQuietly(bookings);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("status", statusResponse).toString(2));
        baseRequest.setHandled(true);
    }
}
