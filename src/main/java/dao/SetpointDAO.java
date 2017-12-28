package dao;

import building.ControllableArea;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Jaap on 10-12-2016.
 */
public class SetpointDAO implements Closeable {
    private final Jedis jedis;

    private final int TTL_KNX = 120;
    private final int TTL_BOOKINGS = 1800;

    public SetpointDAO() {
        jedis = new Jedis("localhost");
    }

    public SetpointDAO setKnx(ControllableArea room, double value) {
        jedis.setex(room.name() + ".setpoint-knx", TTL_KNX, Double.toString(value));
        return this;
    }

    public Double getUser(ControllableArea room) {
        if (!jedis.exists(room + ".setpoint-user")) {
            return null;
        }
        return Double.valueOf(jedis.get(room + ".setpoint-user"));
    }

    public Double getKnx(ControllableArea room) {
        if (!jedis.exists(room + ".setpoint-knx")) {
            return null;
        }
        return Double.valueOf(jedis.get(room + ".setpoint-knx"));
    }

    public double getDefault(ControllableArea room) {
        if (!jedis.exists(room + ".setpoint-default")) {
            return 20.5;
        }
        return Double.parseDouble(jedis.get(room + ".setpoint-default"));
    }

    public double get(ControllableArea room) {
        if (isActive(room)) {
            Double setpoint = getKnx(room);
            if (setpoint == null) {
                setpoint = getUser(room);
            }
            if (setpoint == null) {
                setpoint = getDefault(room);
            }
            return timeCorrected(setpoint);
        }

        return 14.0;
    }

    public SetpointDAO setDefault(ControllableArea room, double value) {
        jedis.set(room + ".setpoint-default", Double.toString(value));
        return this;
    }

    public boolean isActive(ControllableArea room) {
        if (!jedis.exists(room + ".heating-active")) {
            LogstashLogger.INSTANCE.message("WARNING " + room + ".heating-active not available in Redis");
            return true;
        }
        return "T".equals(jedis.get(room + ".heating-active"));
    }

    public SetpointDAO setActive(ControllableArea room, boolean active) {
        jedis.setex(room + ".heating-active", TTL_BOOKINGS, active ? "T" : "F");
        return this;
    }

    public SetpointDAO populateDefault() {
        setDefault(ControllableArea.apartment_II_bedroom, 19.5);
        setDefault(ControllableArea.apartment_III_bathroom, 19.5);
        setDefault(ControllableArea.room_1, 19.5);
        return this;
    }

    private double timeCorrected(double in) {
        switch (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            case 22:
                return in - 0.5;
            case 23:
                return in - 1.0;
            case 0:
                return in - 3.0;
            case 1:
                return in - 3.0;
            case 2:
                return in - 3.0;
            case 3:
                return in - 2.0;
            case 4:
                return in - 2.0;
            case 5:
                return in - 1.5;
            case 6:
                return in - 1.0;
            case 7:
                return in - 0.5;
            default:
                return in;
        }
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
