package control;

import building.Building;
import building.ControllableArea;
import building.Room;
import dao.RoomOccupationDAO;
import dao.SetpointDAO;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

/**
 * If a room is unused, turn off the heating.
 * Ramp up the setpoint to it's default value to allow the room to heat up in the hours before an empty room
 * gets occupied.
 */
public class SetpointControl implements Runnable {

    private final double PREHEAT_START_TEMP = 15.0;
    private final double PREHEAT_FINAL_TEMP = 20.0;

    @Override
    public void run() {
        try (SetpointDAO setpointDAO = new SetpointDAO();
            RoomOccupationDAO roomOccupationDAO = new RoomOccupationDAO()) {
            for (Room room : Building.INSTANCE.allControllableRooms()) {
                Double preheatSetpoint = preheatSetpoint(roomOccupationDAO.getFirstCheckinTime(room)
                        , Building.INSTANCE.firstControllableArea(room).preheatRampTimeHours);
                for (ControllableArea controlRoom : Building.INSTANCE.findControllableAreas(room)) {
                    if (room.beds24Id == null) {
                        // Not a bookable room
                        setpointDAO.setDefault(controlRoom, setpointDAO.getHardDefault(controlRoom));
                    } else if (isOccupied(roomOccupationDAO.getFirstCheckinTime(room))) {
                        setpointDAO.setDefault(controlRoom, setpointDAO.getHardDefault(controlRoom));
                    } else if (preheatSetpoint != null) {
                        setpointDAO.setDefault(controlRoom, preheatSetpoint);
                        setpointDAO.setPreheatSetpoint(controlRoom, preheatSetpoint);
                    } else {
                        setpointDAO.setDefault(controlRoom, SetpointDAO.DEFAULT_SETPOINT_OFF);
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
            Date preheatStart = DateUtils.addHours(checkin, -hourThreshold);
            long diffHours = (new Date().getTime() - preheatStart.getTime()) / (60*60*1000);

            if (diffHours < hourThreshold && diffHours > 0) {
                return PREHEAT_START_TEMP + (PREHEAT_FINAL_TEMP - PREHEAT_START_TEMP) * diffHours/hourThreshold;
            }
        }
        return null;
    }
}