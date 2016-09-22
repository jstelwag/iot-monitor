package handlers;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import control.RoomSetpoint;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import retriever.Booking;
import state.RoomTemperatureState;
import state.ZoneState;
import state.ZoneTemperatureState;

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

        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            JSONObject roomResponse = new JSONObject();
            statusResponse.getJSONArray("rooms").put(roomResponse);
            roomResponse.put("controllableRoom", controllableRoom);

            RoomSetpoint setpoint = HeatingControl.INSTANCE.setpoints.get(controllableRoom);
            if (setpoint != null) {
                roomResponse.put("setpoint", setpoint.getSetpoint());
            }

            RoomTemperatureState roomTemperatureState = HeatingControl.INSTANCE.roomTemperatureState.get(controllableRoom).peekLast();
            if (roomTemperatureState != null) {
                roomResponse.put("temperature", roomTemperatureState.temperature);
            }

            JSONArray zones = new JSONArray();
            roomResponse.put("zones", zones);
            for (HeatZone zone : Building.INSTANCE.zonesByRoom(controllableRoom)) {
                JSONObject zoneResponse = new JSONObject();
                zoneResponse.put("zone", zone);
                zones.put(zoneResponse);

                ZoneTemperatureState zoneTemperatureState = HeatingControl.INSTANCE.zoneTemperatureState.get(zone).peekLast();
                if (zoneTemperatureState != null) {
                    zoneResponse.put("temperature", zoneTemperatureState.temperature);
                }

                ZoneState zoneState = HeatingControl.INSTANCE.controlState.get(zone).peekLast();
                if (zoneState != null) {
                    zoneResponse.put("state", zoneState.valve);
                }
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

        for (Building.Room room : Building.Room.values()) {
            Booking occupied = HeatingControl.INSTANCE.occupiedNow.get(room);
            if (occupied != null) {
                JSONObject booking = new JSONObject();
                statusResponse.getJSONArray("occupiedNow").put(booking);
                booking.put("name", occupied.name);
                booking.put("room", occupied.room);
            }
            Booking booked = HeatingControl.INSTANCE.occupiedTonight.get(room);
            if (booked != null) {
                JSONObject booking = new JSONObject();
                statusResponse.getJSONArray("occupiedTonight").put(booking);
                booking.put("name", booked.name);
                booking.put("room", booked.room);
            }
        }
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(new JSONObject().put("status", statusResponse).toString(2));
        baseRequest.setHandled(true);
    }
}
