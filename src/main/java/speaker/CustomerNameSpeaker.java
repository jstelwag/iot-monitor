package speaker;

import building.Building;
import building.Room;
import dao.BookingDAO;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Created by Jaap on 30-1-2016.
 */
public class CustomerNameSpeaker implements Runnable {

    public CustomerNameSpeaker() {}

    @Override
    public void run() {
        LogstashLogger.INSTANCE.info("Posting bookings to influx");
        try (FluxLogger flux = new FluxLogger(); BookingDAO bookings = new BookingDAO()) {
            for (Room room : Building.INSTANCE.allControllableRooms()) {
                if (bookings.getNow(room) != null) {
                    flux.message(LineProtocolUtil.protocolLine(room, bookings.getNow(room)));
                }
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Failed to post bookings to Influx " + e.getMessage());
        }
    }
}
