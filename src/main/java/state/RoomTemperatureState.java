package state;

import building.Building;

public class RoomTemperatureState extends TemperatureState implements Comparable {

    public static final double ASSUMED_TEMPERATURE = 20.0;

    public final Building.ControllableRoom controllableRoom;
    public boolean isPosted = false;

    public RoomTemperatureState(Building.ControllableRoom controllableRoom, double temperature) {
        super(temperature);
        this.controllableRoom = controllableRoom;
    }

    @Override
    public String toString() {
        return controllableRoom + " t:" + temperature + "C";
    }

    @Override
    public int compareTo(Object o) {
        return controllableRoom.compareTo(((RoomTemperatureState)o).controllableRoom);
    }
}
