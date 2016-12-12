package dao;

import building.Building;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Jaap on 11-12-2016.
 */
public class BookingDAO implements Closeable {

    private final Jedis jedis;

    private final int TTL_BOOKINGS = 60*30;

    public BookingDAO() {
        jedis = new Jedis("localhost");
    }

    public BookingDAO setNow(Building.Room room, String name) {
        if (name == null) {
            jedis.setex(room + ".booking-now", TTL_BOOKINGS, "empty");
        } else {
            jedis.setex(room + ".booking-now", TTL_BOOKINGS, name);
        }
        return this;
    }

    public BookingDAO setTonight(Building.Room room, String name) {
        if (name == null) {
            jedis.setex(room + ".booking-tonight", TTL_BOOKINGS, "empty");
        } else {
            jedis.setex(room + ".booking-tonight", TTL_BOOKINGS, name);
        }
        return this;
    }

    public String getNow(Building.Room room) {
        return jedis.get(room + ".booking-now");
    }

    public String getTonight(Building.Room room) {
        return jedis.get(room + ".booking-tonight");
    }

    public BookingDAO setTomorrow(Building.Room room, String name) {
        if (name == null) {
            jedis.setex(room + ".booking-tomorrow", TTL_BOOKINGS, "empty");
        } else {
            jedis.setex(room + ".booking-tomorrow", TTL_BOOKINGS, name);
        }
        return this;
    }

    public boolean isOccupiedNow(Building.Room room) {
        if (!jedis.exists(room + ".booking-now")) {
            LogstashLogger.INSTANCE.message("WARNING: " + room + ".booking-now is not available in Redis");
            return true;
        }
        return !"empty".equals(getNow(room));
    }

    public boolean isOccupiedTonight(Building.Room room) {
        if (!jedis.exists(room + ".booking-tonight")) {
            LogstashLogger.INSTANCE.message("WARNING: " + room + ".booking-tonight is not available in Redis");
            return true;
        }
        return !"empty".equals(getTonight(room));
    }

    @Override
    public void close() throws IOException {
        jedis.close();
    }
}
