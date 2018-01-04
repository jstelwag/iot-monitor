package control;

import building.Building;
import building.ControllableArea;
import building.Room;
import dao.BookingDAO;
import dao.SetpointDAO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import util.HeatingProperties;

import java.util.Date;
import java.util.List;

/**
 * If a room is unused, turn off the heating
 * When the room is occupied to(morrow)night or it is still 2 hours before checkout, the heating should be switched on
 */
public class Setpoint implements Runnable {

    @Override
    public void run() {
        System.out.println("Updating setpoints to Redis");
        Date now = new Date();
        Date heatingOffTime = DateUtils.addHours(HeatingProperties.checkoutTime(now), -2);
        SetpointDAO setpoints = new SetpointDAO();
        BookingDAO bookings = new BookingDAO();
        for (Room room : Building.INSTANCE.allControllableRooms()) {
            boolean active = bookings.isOccupiedTonight(room)
                    || bookings.isOccupiedTomorrow(room) //todo only use tomorrow when things are cold
                    || (bookings.isOccupiedNow(room) && now.before(heatingOffTime));
            List<ControllableArea> rooms = Building.INSTANCE.findRooms(room);
            for (ControllableArea controlRoom : rooms) {
                setpoints.setActive(controlRoom, active);
            }
        }
        IOUtils.closeQuietly(setpoints);
        IOUtils.closeQuietly(bookings);
    }
}
