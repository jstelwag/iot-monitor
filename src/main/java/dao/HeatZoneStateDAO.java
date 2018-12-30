package dao;

import building.Building;
import building.HeatZone;
import redis.clients.jedis.Jedis;

import java.io.Closeable;;

import java.io.IOException;

/**
 * Stores and retrieves different HeatZone states to Redis:
 * - Default
 * - Desired
 * - Active
 * = TODO Confirmed
 */
public class HeatZoneStateDAO implements Closeable {
    private final Jedis jedis;

    final int TTL = 120;
    final int TTL_ACTUAL = 600;

    public HeatZoneStateDAO() {
        jedis = new Jedis("localhost");
    }

    public boolean getDesired(HeatZone zone) {
        if (jedis.exists(zone.group + "." + zone.groupSequence + ".state-desired")) {
            return "T".equals(jedis.get(zone.group + "." + zone.groupSequence + ".state-desired"));
        }
        return getDefault(zone);
    }

    public boolean getActual(HeatZone zone) {
        return jedis.exists(zone.group + "." + zone.groupSequence + ".state-actual")
                && "T".equals(jedis.get(zone.group + "." + zone.groupSequence + ".state-actual"));
    }

    public boolean getDefault(HeatZone zone) {
        return "T".equals(jedis.get(zone.group + "." + zone.groupSequence + ".state-default"));
    }

    public HeatZoneStateDAO setDesired(HeatZone zone, boolean state) {
        jedis.setex(zone.group + "." + zone.groupSequence + ".state-desired", TTL, state ? "T" : "F");
        return this;
    }

    public HeatZoneStateDAO setActual(HeatZone zone, boolean state) {
        jedis.setex(zone.group + "." + zone.groupSequence + ".state-actual", TTL_ACTUAL, state ? "T" : "F");
        return this;
    }

    public HeatZoneStateDAO setDefault(HeatZone zone, boolean state) {
        jedis.set(zone.group + "." + zone.groupSequence + ".state-default", state ? "T" : "F");
        return this;
    }

    public HeatZoneStateDAO populateDefault() {
        HeatZone zone;
        Building building = Building.INSTANCE;
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 0);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 1);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 2);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 3);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 4);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 5);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 6);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 7);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_kelder, 8);
        setDefault(zone, false);

        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 0);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 1);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 2);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 3);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 4);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 5);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 6);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 7);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 8);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 9);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 10);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 11);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 12);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 13);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_15, 14);
        setDefault(zone, false);

        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 0);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 1);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 2);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 3);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 4);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.koetshuis_trap_6, 5);
        setDefault(zone, false);

        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 0);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 1);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 2);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 3);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 4);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 5);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 6);
        setDefault(zone, false);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_zolder, 7);
        setDefault(zone, true);

        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 0);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 1);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 2);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 3);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 4);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 5);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 6);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 7);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 8);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 9);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 10);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 11);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 12);
        setDefault(zone, true);
        zone = building.zoneById(HeatZone.ValveGroup.kasteel_hal, 13);
        setDefault(zone, true);

        return this;
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
