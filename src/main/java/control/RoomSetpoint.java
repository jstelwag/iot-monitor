package control;

import building.Building;

import java.util.Calendar;

public class RoomSetpoint implements Comparable {
    public final Building.ControllableRoom controllableRoom;
    public double setpoint;
    public boolean isActive;

    public RoomSetpoint(Building.ControllableRoom controllableRoom, double setpoint) {
        this.controllableRoom = controllableRoom;
        this.setpoint = setpoint;
        isActive = true;
    }

    public double getSetpoint() {
        if (isActive) {
            if (setpoint > 23) {
                return timeCorrected(23.0);
            }
            return timeCorrected(setpoint);
        }
        return DefaultSetpoint.ROOM_OFF_SETPOINT;
    }

    private double timeCorrected(double in) {
        switch (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            case 22:
                return in - 0.5;
            case 23:
                return in - 1.0;
            case 0:
                return in - 3.0;
            case 1:
                return in - 3.0;
            case 2:
                return in - 3.0;
            case 3:
                return in - 2.0;
            case 4:
                return in - 2.0;
            case 5:
                return in - 1.5;
            case 6:
                return in - 1.0;
            case 7:
                return in - 0.5;
            default:
                return in;
        }
    }

    @Override
    public String toString() {
        return controllableRoom + " setpoint: " + getSetpoint();
    }

    @Override
    public int compareTo(Object o) {
        return controllableRoom.compareTo(((RoomSetpoint)o).controllableRoom);
    }
}
