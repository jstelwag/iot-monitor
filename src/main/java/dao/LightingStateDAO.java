package dao;

import redis.clients.jedis.Jedis;
import java.io.Closeable;
import java.io.IOException;

/**
 * Keeps an account of all lighing state (via KNX) by listing to KNX events and polling KNX devices for their state.
 */
public class LightingStateDAO implements Closeable {
    private final Jedis jedis;
    final int TTL = 60 * 60; //seconds

    public LightingStateDAO() {
        jedis = new Jedis("localhost");
    }

    public Boolean getState(String address) {
        String key = address + ".state";
        if (jedis.exists(key)) {
            return "T".equals(jedis.get(key));
        }
        return null;
    }

    public LightingStateDAO setState(String address, boolean state) {
        jedis.setex(address + ".state", TTL, state ? "T" : "F");
        return this;
    }

     public long getStateTTL(String address) {
        String key = address + ".state";
        if (jedis.exists(key)) {
            return jedis.ttl(key);
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
