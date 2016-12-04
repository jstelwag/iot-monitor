package control;

import building.Building;

import java.util.*;

public class DefaultSetpoint {

    public final static double ROOM_OFF_SETPOINT = 15.0;
    public final static double ROOM_ON_SETPOINT = 20.0;

    public static SortedMap<Building.ControllableRoom, RoomSetpoint> populate() {
        SortedMap<Building.ControllableRoom, RoomSetpoint> retVal = new TreeMap<>();

        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            retVal.put(controllableRoom, new RoomSetpoint(controllableRoom, ROOM_ON_SETPOINT));
        }
        retVal.get(Building.ControllableRoom.apartment_II_bedroom).setpoint = 19.0;
        retVal.get(Building.ControllableRoom.apartment_III_bathroom).setpoint = 19.0;
        retVal.get(Building.ControllableRoom.room_1).setpoint = 19.0;

        return retVal;
    }
}