package control;

import building.Building;
import building.Furnace;
import building.HeatZone;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import org.apache.commons.io.IOUtils;

import java.util.*;

public class HeatingControl {

    public final static HeatingControl INSTANCE = new HeatingControl();
    public final Map<Furnace, ControlModulation> furnaceModulation = new HashMap<>();

    private HeatingControl() {
        IOUtils.closeQuietly(new HeatZoneStateDAO().populateDefault());
        IOUtils.closeQuietly(new SetpointDAO().populateDefault());
        for (Furnace furnace : Furnace.values()) {
            furnaceModulation.put(furnace, new ControlModulation());
        }
    }

    public List<HeatZone.ValveGroup> valveGroupsByFurnace(Furnace furnace) {
        List<HeatZone.ValveGroup> retVal = new LinkedList<>();

        for (HeatZone.ValveGroup group : HeatZone.ValveGroup.values()) {
            if (furnace.equals(group.furnace)) {
                retVal.add(group);
            }
        }

        return retVal;
    }

    /** @return the number of open zones in reach of given furnace */
    public int furnaceDesire(Furnace furnace) {
        int furnaceDesire = 0;
        HeatZoneStateDAO zoneStates = new HeatZoneStateDAO();
        for (HeatZone.ValveGroup group : HeatingControl.INSTANCE.valveGroupsByFurnace(furnace)) {
            for (HeatZone zone : Building.INSTANCE.zonesByGroup(group)) {
                if (zoneStates.getActual(zone)) furnaceDesire++;
            }
        }
        IOUtils.closeQuietly(zoneStates);
        return furnaceDesire;
    }
}
