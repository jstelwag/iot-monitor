package retriever;

import building.Building;
import building.Room;
import dao.BookingDAO;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import speaker.LogstashLogger;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Beds24BookingRetriever implements Runnable {

    final JSONObject request = new JSONObject();

    public Beds24BookingRetriever(String apiKey, String propertyKey) {
        request.put("arrivalTo", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), 2)));
        request.put("arrivalFrom", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), -10)));
        request.put("authentication", new JSONObject().put("apiKey", apiKey).put("propKey", propertyKey));
    }

    @Override
    public void run() {
        String responseBody = null;
        List<Room> roomsNow = new ArrayList<>();
        List<Room> roomsTonight = new ArrayList<>();
        List<Room> roomsTomorrow = new ArrayList<>();

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
                    Room room = Booking.roomById(bedsBooking.getLong("roomId"));

                    if (room != null) {
                        Booking booking = new Booking(DateUtils.parseDate(bedsBooking.getString("firstNight"), "yyyy-MM-dd")
                                , DateUtils.parseDate(bedsBooking.getString("lastNight"), "yyyy-MM-dd")
                                , room);

                        if (booking.isOccupied()) {
                            bookings.setNow(room, name);
                            roomsNow.add(room);
                        }
                        if (booking.isBookedToday()) {
                            bookings.setTonight(room, name);
                            roomsTonight.add(room);
                        }
                        if (booking.isBookedTomorrow()) {
                            bookings.setTomorrow(room, name);
                            roomsTomorrow.add(room);
                        }
                    } else {
                        LogstashLogger.INSTANCE.message("ERROR, unknown room id " + bedsBooking.toString(1));
                        System.out.println("ERROR, unknown room id " + bedsBooking.toString(1));
                    }
                }
            }

            for (Room room : Building.INSTANCE.bookableRooms()) {
                if (!roomsNow.contains(room)) {
                    bookings.setNow(room, null);
                }
                if (!roomsTonight.contains(room)) {
                    bookings.setTonight(room, null);
                }
                if (!roomsTomorrow.contains(room)) {
                    bookings.setTomorrow(room, null);
                }
            }
            //todo remove this
            bookings.setNow(Room.room_3, "Lynn, Anna en Jaap");
            bookings.setTonight(Room.room_3, "Lynn, Anna en Jaap");

            System.out.println("Retrieved " + response.length() + " bookings");
        } catch (IOException | ParseException e) {
            System.out.println("Unexpected response from beds24: " + responseBody);
            LogstashLogger.INSTANCE.message("ERROR: Unexpected response from beds24: " + responseBody);
        }
    }
}
