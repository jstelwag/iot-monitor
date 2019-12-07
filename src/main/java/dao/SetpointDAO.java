package dao;

import building.Building;
import building.ControllableArea;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;

import java.io.Closeable;
import java.util.Calendar;

/**
 * Created by Jaap on 10-12-2016.
 */
public class SetpointDAO implements Closeable {
    private final Jedis jedis;

    private final int TTL_BOOKINGS = 1800;
    private final int TTL_OVERRIDE = 24 * 60 * 60;

    private final double DEFAULT_SETPOINT = 21.0;
    private final double DEFAULT_SETPOINT_BEDROOM = 19.5;
    public final double DEFAULT_SETPOINT_OFF = 12.0;

    public SetpointDAO() {
        jedis = new Jedis("localhost");
    }

    public double getDefault(ControllableArea room) {
        if (!jedis.exists(room + ".setpoint-default")) {
            return DEFAULT_SETPOINT;
        }
        return Double.parseDouble(jedis.get(room + ".setpoint-default"));
    }

    public double getActual(ControllableArea room) {
        if (jedis.exists(room + ".setpoint-override")) {
            return timeCorrected(Double.parseDouble(jedis.get(room + ".setpoint-override")));
        }
        if (isActive(room)) {
            return timeCorrected(getDefault(room));
        }

        return DEFAULT_SETPOINT_OFF;
    }

    /**
     * Setpoint override the default setpoint. If it is a bookable room, the override
     * will be removed after the TTL_OVERRIDE value.
     * @param room
     * @param value
     */
    public void setOverride(ControllableArea room, double value) {
        if (room.room.beds24Id == null) {
            jedis.set(room + ".setpoint-override", Double.toString(value));
        } else {
            jedis.setex(room + ".setpoint-override", TTL_OVERRIDE, Double.toString(value));
        }
    }

    public void removeOverride(ControllableArea room) {
        jedis.del(room + ".setpoint-override");
    }

    public SetpointDAO setDefault(ControllableArea room, double value) {
        jedis.set(room + ".setpoint-default", Double.toString(value));
        return this;
    }

    public boolean isActive(ControllableArea room) {
        if (!jedis.exists(room + ".heating-active")) {
            LogstashLogger.INSTANCE.warn(room + ".heating-active not available in Redis");
            return true;
        }
        return "T".equals(jedis.get(room + ".heating-active"));
    }

    public SetpointDAO setActive(ControllableArea room, boolean active) {
        jedis.setex(room + ".heating-active", TTL_BOOKINGS, active ? "T" : "F");
        return this;
    }

    public SetpointDAO populateDefault() {
        for (ControllableArea area : ControllableArea.values()) {
            setDefault(area, DEFAULT_SETPOINT);
        }
        setDefault(ControllableArea.apartment_II_bedroom, DEFAULT_SETPOINT_BEDROOM);
        setDefault(ControllableArea.room_f_bathroom, DEFAULT_SETPOINT_BEDROOM);
        setDefault(ControllableArea.room_1, DEFAULT_SETPOINT_BEDROOM);
        setDefault(ControllableArea.hall, DEFAULT_SETPOINT_BEDROOM);

        return this;
    }

    private double timeCorrected(double in) {
        switch (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            case 22:
                return in - 0.5;
            case 23:
                return in - 0.8;
            case 0:
                return in - 2.5;
            case 1:
                return in - 2.5;
            case 2:
                return in - 2.5;
            case 3:
                return in - 2.0;
            case 4:
                return in - 2.0;
            case 5:
                return in - 1.0;
            case 6:
                return in - 0.8;
            case 7:
                return in - 0.5;
            default:
                return in;
        }
    }

    @Override
    public void close() {
        jedis.close();
    }
}
