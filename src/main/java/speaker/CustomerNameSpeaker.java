package speaker;

import building.Building;
import control.HeatingControl;
import retriever.Booking;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Created by Jaap on 30-1-2016.
 */
public class CustomerNameSpeaker implements Runnable {

    public CustomerNameSpeaker() {}


    @Override
    public void run() {
        System.out.println("Posting " +  HeatingControl.INSTANCE.occupiedNow.size() + " bookings to influx");
        try (FluxLogger flux = new FluxLogger()) {
            for (Building.Room room : Building.Room.values()) {
                Booking booking = HeatingControl.INSTANCE.occupiedNow.get(room);
                if (booking != null) {
                    flux.message(LineProtocolUtil.protocolLine(booking.room, booking.name));
                } else {
                    flux.message(LineProtocolUtil.protocolLine(room, "empty"));
                }
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: fail to post bookings to Influx " + e.getMessage());
        }
    }
}
