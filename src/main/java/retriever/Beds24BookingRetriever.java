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

    private final JSONObject request = new JSONObject();

    private final int DAYS_AHEAD = 3;

    public Beds24BookingRetriever(String apiKey, String propertyKey) {
        request.put("arrivalTo", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), DAYS_AHEAD)));
        request.put("arrivalFrom", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), -10)));
        request.put("authentication", new JSONObject().put("apiKey", apiKey).put("propKey", propertyKey));
    }

    @Override
    public void run() {
        String responseBody = null;
        List<Room> roomsNow = new ArrayList<>();
        List<Room> roomsTonight = new ArrayList<>();
        List<Room> roomsTomorrow = new ArrayList<>();

        Map<Room, SortedSet<Booking>> bookings = new HashMap<>();

        try (BookingDAO bookingDAO = new BookingDAO()) {
            responseBody = Request.Post("https://www.beds24.com/api/json/getBookings")
                    .version(HttpVersion.HTTP_1_1)
                    .bodyString(request.toString(), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();
            JSONArray response = new JSONArray(responseBody);
            for (int i = 0; i < response.length(); i++) {
                JSONObject bedsBooking = response.getJSONObject(i);
                //Status values: https://api.beds24.com/json/getBookings
                if (bedsBooking.getInt("status") == 1 || bedsBooking.getInt("status") == 2) {
                    String name = (bedsBooking.getString("guestFirstName") + " " + bedsBooking.getString("guestName")).trim();
                    Long roomId = bedsBooking.getLong("roomId");
                    if (roomId == null) {
                        LogstashLogger.INSTANCE.error("No room id for " + name + " while retrieving beds24 bookings");
                    } else {
                        Room room = Booking.roomById(roomId);
                        if (room != null) {
                            Booking booking = new Booking(DateUtils.parseDate(bedsBooking.getString("firstNight"), "yyyy-MM-dd")
                                    , DateUtils.parseDate(bedsBooking.getString("lastNight"), "yyyy-MM-dd")
                                    , room);
                            if (booking.checkoutTime.compareTo(new Date()) > 0) {
                                if (!bookings.containsKey(room)) {
                                    bookings.put(room, new TreeSet<>());
                                }
                                bookings.get(room).add(booking);

                                if (booking.isOccupied()) {
                                    bookingDAO.setNow(room, name);
                                    roomsNow.add(room);
                                }
                                if (booking.isBookedToday()) {
                                    bookingDAO.setTonight(room, name);
                                    roomsTonight.add(room);
                                }
                                if (booking.isBookedTomorrow()) {
                                    bookingDAO.setTomorrow(room, name);
                                    roomsTomorrow.add(room);
                                }
                            }
                        } else {
                            LogstashLogger.INSTANCE.error("Unknown room id " + bedsBooking.toString(1));
                        }
                    }
                }
            }

            for (Room room : Building.INSTANCE.bookableRooms()) {
                if (!roomsNow.contains(room)) {
                    bookingDAO.setNow(room, null);
                }
                if (!roomsTonight.contains(room)) {
                    bookingDAO.setTonight(room, null);
                }
                if (!roomsTomorrow.contains(room)) {
                    bookingDAO.setTomorrow(room, null);
                }
            }

            for (Room room : bookings.keySet()) {
                for (Booking booking : bookings.get(room)) {
                    System.out.println("Room " + room + ": " + booking.checkinTime);
                }
                bookingDAO.setFirstCheckinTime(room, bookings.get(room).first().checkinTime);
            }
            LogstashLogger.INSTANCE.info("Retrieved " + response.length() + " bookings");
        } catch (IOException | ParseException e) {
            LogstashLogger.INSTANCE.error("Unexpected response from beds24: " + responseBody);
        }
    }
}
