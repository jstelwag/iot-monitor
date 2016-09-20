package control;

import building.Building;

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
            return setpoint;
        }
        return DefaultSetpoint.ROOM_OFF_SETPOINT;
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
