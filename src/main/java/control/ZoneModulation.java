package control;

import building.Building;
import building.Furnace;
import building.HeatZone;
import dao.BookingDAO;
import dao.HeatZoneStateDAO;
import dao.SetpointDAO;
import dao.TemperatureDAO;
import speaker.LogstashLogger;

import java.io.IOException;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Organizes division of heat over the heat zones:
 * 1. sequential, time based
 * 2. with priority
 * 3. or by a manual override
 *
 * At a certain time only a selection of valves / zones are active - other are closed on purpose so the heat is divided
 * fairly.
 *
 * Occupied rooms and rooms with a high offset are treated with a higher priority.
 *
 * Division is a random process, with above mentioned weighing component and the knowledge on what zones are active
 * currently.
 */
public class ZoneModulation implements Runnable {

    public class ModulationComparator implements Comparable {

        public HeatZone zone;
        public Integer weight = 100;

        public ModulationComparator(HeatZone zone) {
            this.zone = zone;
        }

        @Override
        public int compareTo(Object o) {
            //Reverse to descending
            return ((ModulationComparator)o).weight.compareTo(weight);
        }
    }

    @Override
    public void run() {
        try (HeatZoneStateDAO zoneDao = new HeatZoneStateDAO();
             BookingDAO bookingDAO = new BookingDAO();
             TemperatureDAO temperatureDAO = new TemperatureDAO();
             SetpointDAO setpointDAO = new SetpointDAO()) {

            for (Furnace furnace : Furnace.values()) {
                SortedSet<ModulationComparator> actualCandidates = new TreeSet<>();
                for (HeatZone zone : Building.INSTANCE.zonesByFurnace(furnace)) {
                    if (zoneDao.getOverride(zone) != null) {
                        ModulationComparator item = new ModulationComparator(zone);
                        if (zoneDao.getOverride(zone)) {
                            item.weight = 1000;
                            actualCandidates.add(item);
                        }
                        // Ignore the orverride == false, they are not added to the candidate list
                    } else if (zoneDao.getDesired(zone)) {
                        ModulationComparator item = new ModulationComparator(zone);
                        if (zoneDao.getActual(zone)) {
                            // reduce point to reduce the chance the zone is active again
                            item.weight = item.weight - 50;
                        }

                        if (bookingDAO.isOccupiedNow(zone.controllableArea.room)) {
                            item.weight = item.weight + 50;
                        }

                        double offset = setpointDAO.getActual(zone.controllableArea) - temperatureDAO.get(zone.controllableArea);
                        item.weight = item.weight + (int)(offset * 20);
                        item.weight = item.weight + new Random().nextInt(100);
                        actualCandidates.add(item);
                    }
                }

                // Prepare control switch: first turn off all zones
                for (HeatZone zone : Building.INSTANCE.zonesByFurnace(furnace)) {
                    zoneDao.setActual(zone, false);
                }

                int i = 0;
                for (ModulationComparator actuallyOn : actualCandidates) {
                    zoneDao.setActual(actuallyOn.zone, true);
                    if (++i > furnace.maxActiveZones) {
                        LogstashLogger.INSTANCE.info("Doing zone modulation, " + furnace.maxActiveZones
                                + " zones of " + actualCandidates.size()
                                + " in use for furnace " + furnace);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Failed to connect with Redis " + e.getMessage());
        }
    }
}
