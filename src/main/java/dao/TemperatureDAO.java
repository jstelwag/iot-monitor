package dao;

import building.Building;
import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Jaap on 11-12-2016.
 */
public class TemperatureDAO implements Closeable {
    private final Jedis jedis;

    final int TTL_KNX = 120;

    public TemperatureDAO() {
        jedis = new Jedis("localhost");
    }

    public double get(Building.ControllableRoom room) {
        if (!jedis.exists(room + ".temperature")) {
            return 19.5;
        }
        return Double.valueOf(jedis.get(room + ".temperature"));
    }

    public Double getActual(Building.ControllableRoom room) {
        if (!jedis.exists(room + ".temperature")) {
            return null;
        }
        return Double.valueOf(jedis.get(room + ".temperature"));
    }

    public TemperatureDAO set(Building.ControllableRoom room, double value) {
        jedis.setex(room + ".temperature", TTL_KNX, Double.toString(value));
        return this;
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}