package dao;

import building.Room;
import org.apache.commons.lang3.time.DateFormatUtils;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;

import java.io.Closeable;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Jaap on 11-12-2016.
 */
public class BookingDAO implements Closeable {

    private final Jedis jedis;

    private final int TTL_BOOKINGS = 60*30;

    public BookingDAO() {
        jedis = new Jedis("localhost");
    }

    public BookingDAO setNow(Room room, String name) {
        if (name == null) {
            jedis.setex(room + ".booking-now", TTL_BOOKINGS, "empty");
        } else {
            jedis.setex(room + ".booking-now", TTL_BOOKINGS, name);
        }
        return this;
    }

    public BookingDAO setTonight(Room room, String name) {
        if (name == null) {
            jedis.setex(room + ".booking-tonight", TTL_BOOKINGS, "empty");
        } else {
            jedis.setex(room + ".booking-tonight", TTL_BOOKINGS, name);
        }
        return this;
    }

    public String getNow(Room room) {
        return jedis.get(room + ".booking-now");
    }

    public String getTonight(Room room) {
        return jedis.get(room + ".booking-tonight");
    }

    public String getTomorrow(Room room) {
        return jedis.get(room + ".booking-tomorrow");
    }

    /**
     * Check-in date and time of the first booking on given room. If the date is in the past, the room is already occupied.
     * If the booking is more than DAY_AHEAD days (it was 3) in the future, null is returned.
     */
    public Date getFirstCheckinTime(Room room) {
        String key = room + ".first-checkin-time";
        try {
            if (jedis.exists(key)) {
                return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.parse(jedis.get(key));
            }
        } catch (ParseException e) {
            LogstashLogger.INSTANCE.error("Could not parse date " + jedis.get(key)
                    + " for room " + room);
        }
        return null;
    }

    public void setFirstCheckinTime(Room room, Date date) {
        jedis.setex(room + ".first-checkin-time"
                , TTL_BOOKINGS, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(date));

    }

    public BookingDAO setTomorrow(Room room, String name) {
        if (name == null) {
            jedis.setex(room + ".booking-tomorrow", TTL_BOOKINGS, "empty");
        } else {
            jedis.setex(room + ".booking-tomorrow", TTL_BOOKINGS, name);
        }
        return this;
    }

    public boolean isOccupiedNow(Room room) {
        if (!jedis.exists(room + ".booking-now")) {
            LogstashLogger.INSTANCE.warn(room + ".booking-now is not available in Redis");
            return true;
        }
        return !"empty".equals(getNow(room));
    }

    public boolean isOccupiedTonight(Room room) {
        if (!jedis.exists(room + ".booking-tonight")) {
            LogstashLogger.INSTANCE.warn(room + ".booking-tonight is not available in Redis");
            return true;
        }
        return !"empty".equals(getTonight(room));
    }

    public boolean isOccupiedTomorrow(Room room) {
        if (!jedis.exists(room + ".booking-tomorrow")) {
            LogstashLogger.INSTANCE.warn(room + ".booking-tomorrow is not available in Redis");
            return true;
        }
        return !"empty".equals(getTomorrow(room));
    }

    @Override
    public void close() {
        jedis.close();
    }
}
