package retriever;

import building.Building;
import control.HeatingControl;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import speaker.CustomerNameSpeaker;
import speaker.LogstashLogger;
import util.HeatingProperties;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Beds24BookingRetriever implements Runnable {

    final JSONObject request = new JSONObject();

    public Beds24BookingRetriever(String apiKey, String propertyKey) {
        request.put("arrivalTo", FastDateFormat.getInstance("yyyyMMdd").format(new Date()));
        request.put("arrivalFrom", FastDateFormat.getInstance("yyyyMMdd").format(DateUtils.addDays(new Date(), -10)));
        request.put("authentication", new JSONObject().put("apiKey", apiKey).put("propKey", propertyKey));
    }

    @Override
    public void run() {
        requestBeds24();
        new Thread(new CustomerNameSpeaker()).run();
        updateSetpoints();
    }

    void requestBeds24() {
        String responseBody = null;
        try {
            responseBody = Request.Post("https://www.beds24.com/api/json/getBookings")
                    .version(HttpVersion.HTTP_1_1)
                    .bodyString(request.toString(), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asString();
            JSONArray response = new JSONArray(responseBody);
            HeatingControl.INSTANCE.occupiedNow.clear();
            HeatingControl.INSTANCE.occupiedTonight.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject bedsBooking = response.getJSONObject(i);
                if (bedsBooking.getInt("status") != 0) {
                    String name = bedsBooking.getString("guestFirstName") + " " + bedsBooking.getString("guestName");
                    Building.Room room = Booking.roomById(bedsBooking.getLong("roomId"));

                    if (room != null) {
                        Booking booking = new Booking(name.trim(), DateUtils.parseDate(bedsBooking.getString("firstNight"), "yyyy-MM-dd")
                                , DateUtils.parseDate(bedsBooking.getString("lastNight"), "yyyy-MM-dd")
                                , room);

                        if (booking.isOccupied()) {
                            HeatingControl.INSTANCE.occupiedNow.put(booking.room, booking);
                        }
                        if (booking.isBookedToday()) {
                            HeatingControl.INSTANCE.occupiedTonight.put(booking.room, booking);
                        }
                    } else {
                        System.out.println("ERROR, unknown room id " + bedsBooking.toString(1));
                    }
                }
            }
            //todo remove this
            Building.Room myRoom = Building.Room.room_3;
            Booking booking = new Booking("Jaap, Ling en Anna", new Date(), new Date(), myRoom);
            HeatingControl.INSTANCE.occupiedNow.put(booking.room, booking);
            HeatingControl.INSTANCE.occupiedTonight.put(booking.room, booking);

            HeatingControl.INSTANCE.hasUpdatedBookings = true;
            System.out.println("Retrieved " + response.length() + " bookings, " +  HeatingControl.INSTANCE.occupiedNow.size()
                    + " are occupiedNow and " +  HeatingControl.INSTANCE.occupiedTonight.size() + " rooms are booked for today");
        } catch (IOException | ParseException e) {
            HeatingControl.INSTANCE.hasUpdatedBookings = false;
            System.out.println("Unexpected response from beds24: " + responseBody);
            LogstashLogger.INSTANCE.message("ERROR: Unexpected response from beds24: " + responseBody);
            e.printStackTrace();
        }
    }

    /**
     * If a room is unused, turn off the heating
     * When the room is occupied tonight or it is still 2 hours before checkout, the heating should be switched on
     */
    void updateSetpoints() {
        Date now = new Date();
        Date heatingOffTime = DateUtils.addHours(HeatingProperties.checkoutTime(now), -2);
        for (Building.Room room : Building.Room.values()) {
            List<Building.ControllableRoom> rooms = Building.INSTANCE.findRooms(room);
            for (Building.ControllableRoom controlRoom : rooms) {
                if (HeatingControl.INSTANCE.occupiedTonight.containsKey(room)) {
                    HeatingControl.INSTANCE.setRoomActive(controlRoom, true);
                } else if (HeatingControl.INSTANCE.occupiedNow.containsKey(room) && now.before(heatingOffTime)) {
                    HeatingControl.INSTANCE.setRoomActive(controlRoom, true);
                } else {
                    HeatingControl.INSTANCE.setRoomActive(controlRoom, false);
                }
            }
        }
    }
}
