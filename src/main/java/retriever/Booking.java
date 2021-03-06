package retriever;

import building.Room;
import org.apache.commons.lang3.time.DateUtils;
import util.HeatingProperties;

import java.util.Date;

public class Booking implements Comparable {

    public final Date firstNight;
    public final Date lastNight;
    public final Room room;

    public final Date checkoutTime;
    public final Date checkinTime;

    private final Date now;

    public Booking(Date firstNight, Date lastNight, Room room) {
        this.firstNight = firstNight;
        this.lastNight = lastNight;
        this.room = room;
        this.now = new Date();
        checkoutTime = HeatingProperties.checkoutTime(DateUtils.addDays(lastNight, 1));
        checkinTime = HeatingProperties.checkinTime(firstNight);
    }

    /**
     * The room is occupiedNow for tonight.
     *
     * @return lastNight should be equal or greater than today and firstNight should be less or equal to today.
     */
    public boolean isBookedToday() {
        return ((DateUtils.isSameDay(now, firstNight) || firstNight.before(now)) && (DateUtils.isSameDay(now, lastNight) || lastNight.after(now)));
    }

    /**
     * The room is occupiedNow for tomorrow.
     *
     * @return lastNight should be equal or greater than tomorrow and firstNight should be less or equal to tomorrow.
     */
    public boolean isBookedTomorrow() {
        Date tomorrow = DateUtils.addDays(now, 1);
        return ((DateUtils.isSameDay(tomorrow, firstNight) || firstNight.before(tomorrow))
                && (DateUtils.isSameDay(tomorrow, lastNight) || lastNight.after(tomorrow)));
    }

    public boolean isOccupied() {return now.after(checkinTime) && now.before(checkoutTime);}

    public static Room roomById(long id) {
        for (Room room : Room.values()) {
            if (room.beds24Id != null && room.beds24Id == id) {
                return room;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return room + " " + firstNight + "/" + lastNight;
    }

    @Override
    public int compareTo(Object o) {
        return checkinTime.compareTo(((Booking)o).checkinTime);
    }
}
