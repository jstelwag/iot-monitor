package dao;

import building.Furnace;
import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.io.IOException;

/**
 * Stores and retrieves different Furnace states to Redis:
 * - Desired
 * - Active
 * - Override
 */
public class FurnaceStateDAO implements Closeable {
    private final Jedis jedis;

    final int TTL = 2 * 60; //seconds
    final int TTL_ACTUAL = 30 * 60; //seconds
    final int TTL_OVERRIDE = 48 * 60 * 60; //seconds

    public FurnaceStateDAO() {
        jedis = new Jedis("localhost");
    }

    public Boolean getDesire(Furnace furnace) {
        String key = furnace + ".state-desire";
        if (jedis.exists(key)) {
            return "T".equals(jedis.get(key));
        }
        return null;
    }

    public FurnaceStateDAO setDesire(Furnace furnace, boolean state) {
        jedis.setex(furnace + ".state-desire", TTL, state ? "T" : "F");
        return this;
    }

    public Boolean getOverride(Furnace furnace) {
        String key = furnace + ".state-override";
        if (jedis.exists(key)) {
            return "T".equals(jedis.get(key));
        }
        return null;
    }

    public FurnaceStateDAO setOverride(Furnace furnace, boolean state) {
        jedis.setex(furnace + ".state-override", TTL_OVERRIDE, state ? "T" : "F");
        return this;
    }

    public FurnaceStateDAO removeOverride(Furnace furnace) {
        jedis.del(furnace + ".state-override");
        return this;
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
