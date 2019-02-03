package control;

import building.Building;
import building.ControllableArea;
import building.Room;
import dao.BookingDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import org.apache.commons.lang3.time.DateUtils;
import util.HeatingProperties;

import java.util.Date;
import java.util.List;

/**
 * If a room is unused, turn off the heating
 * When the room is occupied to(morrow)night or it is still 2 hours before checkout, the heating should be switched on
 */
public class Setpoint implements Runnable {

    private final double LONG_PREHEAT_THRESHOLD = 16.0;

    @Override
    public void run() {
        Date now = new Date();
        Date heatingOffTime = DateUtils.addHours(HeatingProperties.checkoutTime(now), -2);

        try (SetpointDAO setpoints = new SetpointDAO();
            BookingDAO bookings = new BookingDAO();
            TemperatureDAO temperatures = new TemperatureDAO()) {
            for (Room room : Building.INSTANCE.allControllableRooms()) {
                boolean active = room.beds24Id == null
                        || bookings.isOccupiedTonight(room)
                        || (temperatures.get(Building.INSTANCE.firstControllableArea(room)) < LONG_PREHEAT_THRESHOLD && bookings.isOccupiedTomorrow(room))
                        || (bookings.isOccupiedNow(room) && now.before(heatingOffTime));
                List<ControllableArea> rooms = Building.INSTANCE.findControllableAreas(room);
                for (ControllableArea controlRoom : rooms) {
                    setpoints.setActive(controlRoom, active);
                }
            }
        }
    }
}
