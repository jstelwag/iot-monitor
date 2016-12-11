package control;

import building.Building;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import knx.KNXLink;
import org.apache.commons.io.IOUtils;

import java.net.UnknownHostException;
import java.util.*;

public class HeatingControl {

    public final static HeatingControl INSTANCE = new HeatingControl();

    public final SortedMap<HeatZone, Boolean> overrides = new TreeMap<>();
    public final Map<Building.Furnace, ControlModulation> furnaceModulation = new HashMap<>();

    public KNXLink knxLink;

    public boolean hasUpdatedBookings = false;

    private HeatingControl() {
        IOUtils.closeQuietly(new HeatZoneStateDAO().populateDefault());
        IOUtils.closeQuietly(new SetpointDAO().populateDefault());
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
        HeatZoneStateDAO zoneStates = new HeatZoneStateDAO();
        for (HeatZone.ValveGroup group : HeatingControl.INSTANCE.valveGroupsByFurnace(furnace)) {
            for (HeatZone zone : Building.INSTANCE.zonesByGroup(group)) {
                if (zoneStates.get(zone)) furnaceDesire++;
            }
        }
        IOUtils.closeQuietly(zoneStates);
        return furnaceDesire;
    }
}
