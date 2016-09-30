package control;

import building.Building;
import building.HeatZone;
import knx.KNXLink;
import retriever.Booking;
import state.DefaultZoneState;
import state.RoomTemperatureState;
import state.ZoneState;
import state.ZoneTemperatureState;
import util.FIFODeque;

import java.net.UnknownHostException;
import java.util.*;

public class HeatingControl {

    public final static HeatingControl INSTANCE = new HeatingControl();

    public final SortedMap<HeatZone, Deque<ZoneState>> controlState;
    public final SortedMap<HeatZone, Deque<ZoneTemperatureState>> zoneTemperatureState = new TreeMap<>();
    public final SortedMap<Building.ControllableRoom, Deque<RoomTemperatureState>> roomTemperatureState = new TreeMap<>();
    public final SortedMap<Building.ControllableRoom, RoomSetpoint> setpoints;
    public final SortedMap<HeatZone, Boolean> overrides = new TreeMap<>();
    public final Map<Building.Room, Booking> occupiedNow = new HashMap<>();
    public final Map<Building.Room, Booking> occupiedTonight = new HashMap<>();
    public final Map<Building.Furnace, ControlModulation> furnaceModulation = new HashMap<>();

    public KNXLink knxLink;

    public boolean hasUpdatedBookings = false;

    private HeatingControl() {
        occupiedNow.clear();
        System.out.println("Initializing HeatingControl");
        controlState = DefaultZoneState.populate();
        setpoints = DefaultSetpoint.populate();
        for (HeatZone zone : Building.INSTANCE.zones) {
            zoneTemperatureState.put(zone, new FIFODeque<ZoneTemperatureState>(DefaultZoneState.QUEUE_LENGTH));
        }
        for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
            roomTemperatureState.put(controllableRoom, new FIFODeque<RoomTemperatureState>(DefaultZoneState.QUEUE_LENGTH));
        }
        for (Building.Furnace furnace : Building.Furnace.values()) {
            furnaceModulation.put(furnace, new ControlModulation());
        }

        try {
            knxLink = new KNXLink();
        } catch (UnknownHostException e) {
            System.out.println("Failed to setup KNX link");
            e.printStackTrace();
        }
    }

    public List<ZoneState> zoneStateByGroup(HeatZone.ValveGroup group) {
        List<ZoneState> retVal = new LinkedList<>();
        for (HeatZone zone : Building.INSTANCE.zoneByGroup(group)) {
            retVal.add(controlState.get(zone).peekLast());
        }
        return retVal;
    }

    public List<ZoneTemperatureState> zoneTemperatureStateByGroup(HeatZone.ValveGroup group) {
        List<ZoneTemperatureState> retVal = new LinkedList<>();
        for (HeatZone zone : Building.INSTANCE.zoneByGroup(group)) {
            // At start this map can be empty
            if (!zoneTemperatureState.get(zone).isEmpty()) {
                retVal.add(zoneTemperatureState.get(zone).peekLast());
            }
        }
        return retVal;
    }

    public void setRoomActive(Building.ControllableRoom controllableRoom, Boolean active) {
        if (active != null) {
            setpoints.get(controllableRoom).isActive = active;
        } else {
            setpoints.get(controllableRoom).isActive = !setpoints.get(controllableRoom).isActive;
        }
    }

    public double getRoomTemperature(Building.ControllableRoom controllableRoom) {
        double roomTemperature;
        if (roomTemperatureState.get(controllableRoom).peekLast() != null) {
            roomTemperature = roomTemperatureState.get(controllableRoom).peekLast().temperature;
        } else {
            roomTemperature = RoomTemperatureState.ASSUMED_TEMPERATURE;
        }
        return roomTemperature;
    }

    public void addRoomTemperature(Building.ControllableRoom controllableRoom, double temperature) {
        roomTemperatureState.get(controllableRoom).add(new RoomTemperatureState(controllableRoom, temperature));
    }

    public List<HeatZone> overridesByRoom(Building.ControllableRoom room) {
        List<HeatZone> retVal = new ArrayList<>();
        for (HeatZone zone : overrides.keySet()) {
            if (zone.controllableRoom == room) {
                retVal.add(zone);
            }
        }

        return retVal;
    }

    public List<HeatZone.ValveGroup> valveGroupsByFurnace(Building.Furnace furnace) {
        List<HeatZone.ValveGroup> retVal = new LinkedList<>();

        for (HeatZone.ValveGroup group : HeatZone.ValveGroup.values()) {
            if (furnace.equals(group.furnace)) {
                retVal.add(group);
            }
        }

        return retVal;
    }
}
