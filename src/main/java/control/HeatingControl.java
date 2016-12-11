package control;

import building.Building;
import building.HeatZone;
import dao.SetpointDAO;
import knx.KNXLink;
import org.apache.commons.io.IOUtils;
import retriever.Booking;
import state.DefaultZoneState;
import state.RoomTemperatureState;
import state.ZoneState;
import util.FIFODeque;

import java.net.UnknownHostException;
import java.util.*;

public class HeatingControl {

    public final static HeatingControl INSTANCE = new HeatingControl();

    public final SortedMap<HeatZone, Deque<ZoneState>> controlState;
    public final SortedMap<Building.ControllableRoom, Deque<RoomTemperatureState>> roomTemperatureState = new TreeMap<>();
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
        IOUtils.closeQuietly(new SetpointDAO().populateDefault());
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
        for (HeatZone zone : Building.INSTANCE.zonesByGroup(group)) {
            retVal.add(controlState.get(zone).peekLast());
        }
        return retVal;
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

    /** @return the number of open zones in reach of given furnace */
    public int furnaceDesire(Building.Furnace furnace) {
        int furnaceDesire = 0;
        for (HeatZone.ValveGroup group : HeatingControl.INSTANCE.valveGroupsByFurnace(furnace)) {
            for (HeatZone zone : Building.INSTANCE.zonesByGroup(group)) {
                if (HeatingControl.INSTANCE.controlState.get(zone).getLast().valve) {
                    furnaceDesire++;
                }
            }
        }
        return furnaceDesire;
    }
}
