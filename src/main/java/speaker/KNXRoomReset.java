package speaker;

import building.Building;
import building.ControllableArea;
import building.Room;
import dao.BookingDAO;
import org.apache.http.client.fluent.Request;
import util.HeatingProperties;

import java.io.IOException;

/**
 * Created by Jaap on 30-1-2016.
 */
public class KNXRoomReset implements Runnable {

    private final HeatingProperties prop = new HeatingProperties();

    public KNXRoomReset() {}

    @Override
    public void run() {
        int resetCount = 0;
        try (BookingDAO bookings = new BookingDAO()) {
            for (Room room : Building.INSTANCE.bookableRooms()) {
                if ("empty".equals(bookings.getTonight(room))) {
                    HeatingProperties prop = new HeatingProperties();
                    String response = Request.Get("http://" + prop.localIp + ":" + prop.masterPort + "/room/"
                                + room + "/all-off/").execute().returnContent().asString();
                    if (!response.contains("\"status\": \"OK\"")) {
                        LogstashLogger.INSTANCE.message("ERROR: unexpected response while resetting room "
                                + room + ": " + response);
                    } else {
                        resetCount++;
                        LogstashLogger.INSTANCE.message("Reset room " + room);
                    }
                }
            }
            System.out.println("I have reset " + resetCount + " rooms");
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: while resetting thermostats " + e.getMessage());
        }
    }
}
