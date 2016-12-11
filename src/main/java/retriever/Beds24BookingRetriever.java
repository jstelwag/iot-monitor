package retriever;

import building.Building;
import control.HeatingControl;
import dao.BookingDAO;
import dao.SetpointDAO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import speaker.LogstashLogger;
import util.HeatingProperties;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Beds24BookingRetriever implements Runnable {

    final JSONObject request = new JSONObject();

    public Beds24BookingRetriever(String apiKey, String propertyKey) {
        request.put("arrivalTo", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), 1)));
        request.put("arrivalFrom", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), -10)));
        request.put("authentication", new JSONObject().put("apiKey", apiKey).put("propKey", propertyKey));
    }

    @Override
    public void run() {
        requestBeds24();
        updateSetpoints();
    }

    void requestBeds24() {
        String responseBody = null;
        try (BookingDAO bookings = new BookingDAO()) {
            responseBody = Request.Post("https://www.beds24.com/api/json/getBookings")
                    .version(HttpVersion.HTTP_1_1)
                    .bodyString(request.toString(), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();
            JSONArray response = new JSONArray(responseBody);
            for (int i = 0; i < response.length(); i++) {
                JSONObject bedsBooking = response.getJSONObject(i);
                if (bedsBooking.getInt("status") != 0) {
                    String name = (bedsBooking.getString("guestFirstName") + " " + bedsBooking.getString("guestName")).trim();
                    Building.Room room = Booking.roomById(bedsBooking.getLong("roomId"));

                    if (room != null) {
                        Booking booking = new Booking(DateUtils.parseDate(bedsBooking.getString("firstNight"), "yyyy-MM-dd")
                                , DateUtils.parseDate(bedsBooking.getString("lastNight"), "yyyy-MM-dd")
                                , room);

                        if (booking.isOccupied()) {
                            bookings.setNow(room, name);
                        }
                        if (booking.isBookedToday()) {
                            bookings.setTonight(room, name);
                        }
                        if (booking.isBookedTomorrow()) {
                            bookings.setTomorrow(room, name);
                        }
                    } else {
                        LogstashLogger.INSTANCE.message("ERROR, unknown room id " + bedsBooking.toString(1));
                        System.out.println("ERROR, unknown room id " + bedsBooking.toString(1));
                    }
                }
            }
            //todo remove this
            bookings.setNow(Building.Room.room_3, "Lynn, Anna en Jaap");
            bookings.setTonight(Building.Room.room_3, "Lynn, Anna en Jaap");

            System.out.println("Retrieved " + response.length() + " bookings");
        } catch (IOException | ParseException e) {
            System.out.println("Unexpected response from beds24: " + responseBody);
            LogstashLogger.INSTANCE.message("ERROR: Unexpected response from beds24: " + responseBody);
        }
    }

    /**
     * If a room is unused, turn off the heating
     * When the room is occupied tonight or it is still 2 hours before checkout, the heating should be switched on
     */
    void updateSetpoints() {
        Date now = new Date();
        Date heatingOffTime = DateUtils.addHours(HeatingProperties.checkoutTime(now), -2);
        SetpointDAO setpoints = new SetpointDAO();
        BookingDAO bookings = new BookingDAO();
        for (Building.Room room : Building.Room.values()) {
            List<Building.ControllableRoom> rooms = Building.INSTANCE.findRooms(room);
            for (Building.ControllableRoom controlRoom : rooms) {
                if (bookings.isOccupiedTonight(room)) {
                    setpoints.setActive(controlRoom, true);
                } else if (bookings.isOccupiedNow(room) && now.before(heatingOffTime)) {
                    setpoints.setActive(controlRoom, true);
                } else {
                    setpoints.setActive(controlRoom, false);
                }
            }
        }
        IOUtils.closeQuietly(setpoints);
        IOUtils.closeQuietly(bookings);
    }
}
