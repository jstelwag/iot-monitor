package dao;

import building.Building;
import building.HeatZone;
import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Jaap on 11-12-2016.
 */
public class HeatZoneStateDAO implements Closeable {
    private final Jedis jedis;

    final int TTL = 120;

    public HeatZoneStateDAO() {
        jedis = new Jedis("localhost");
    }

    public boolean get(HeatZone zone) {
        if (jedis.exists(jedis.get(zone.group + "." + zone.groupSequence + ".state"))) {
            return "T".equals(jedis.get(zone.group + "." + zone.groupSequence + ".state"));
        }
        return getDefault(zone);
    }

    public boolean getDefault(HeatZone zone) {
        return "T".equals(jedis.get(zone.group + "." + zone.groupSequence + ".state-default"));
    }

    public HeatZoneStateDAO set(HeatZone zone, boolean state) {
        jedis.setex(zone.group + "." + zone.groupSequence + ".state", TTL, state ? "T" : "F");
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

        return this;
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
