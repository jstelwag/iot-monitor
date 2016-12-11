package speaker;

import building.Building;
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
        System.out.println("Posting bookings to influx");
        try (FluxLogger flux = new FluxLogger(); BookingDAO bookings = new BookingDAO()) {
            for (Building.Room room : Building.Room.values()) {
                flux.message(LineProtocolUtil.protocolLine(room, bookings.getNow(room)));
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: fail to post bookings to Influx " + e.getMessage());
        }
    }
}
