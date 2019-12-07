package control;

import building.Building;
import building.ControllableArea;
import building.Room;
import dao.BookingDAO;
import dao.SetpointDAO;
import org.apache.commons.lang3.time.DateUtils;
import speaker.LogstashLogger;

import java.util.Date;

/**
 * If a room is unused, turn off the heating.
 * Ramp up the setpoint to it's default value to allow the room to heat up in the hours before an empty room
 * gets occupied.
 */
public class SetpointControl implements Runnable {

    private final double LONG_PREHEAT_THRESHOLD = 16.0;

    private final double PREHEAT_START_TEMP = 15.0;
    private final double PREHEAT_FINAL_TEMP = 20.0;

    @Override
    public void run() {
        try (SetpointDAO setpointDAO = new SetpointDAO();
            BookingDAO bookingDAO = new BookingDAO()) {
            for (Room room : Building.INSTANCE.allControllableRooms()) {
                boolean active = room.beds24Id == null
                        || isOccupied(bookingDAO.getFirstCheckinTime(room));
                Double preheatSetpoint = preheatSetpoint(bookingDAO.getFirstCheckinTime(room)
                        , Building.INSTANCE.firstControllableArea(room).preheatRampTimeHours);
                for (ControllableArea controlRoom : Building.INSTANCE.findControllableAreas(room)) {
                    if (preheatSetpoint != null) {
                        setpointDAO.setActive(controlRoom, true);
                        setpointDAO.setDefault(controlRoom, preheatSetpoint);
                        LogstashLogger.INSTANCE.info("Preheating " + room + ": " + preheatSetpoint);
                    } else {
                        setpointDAO.setActive(controlRoom, active);
                    }
                }
            }
        }
    }

    private boolean isOccupied(Date checkin) {
        if (checkin != null && checkin.compareTo(new Date()) < 0) {
            return true;
        }
        return false;
    }

    private Double preheatSetpoint(Date checkin, int hourThreshold) {
        if (checkin != null) {
            Date preheatStart = DateUtils.addHours(new Date(), -hourThreshold);
            long diffHours = (new Date().getTime() - preheatStart.getTime()) / (60*60*1000);

            if (diffHours < hourThreshold && diffHours > 0) {
                return PREHEAT_START_TEMP + (PREHEAT_FINAL_TEMP - PREHEAT_START_TEMP) * diffHours/hourThreshold;
            }
        }
        return null;
    }
}