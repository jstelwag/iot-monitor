package speaker;

import building.ControllableArea;
import dao.BookingDAO;
import org.apache.http.client.fluent.Request;
import util.HeatingProperties;

import java.io.IOException;

/**
 * Created by Jaap on 30-1-2016.
 */
public class KNXThermostatReset implements Runnable {

    public KNXThermostatReset() {}

    @Override
    public void run() {
        int setpointCount = 0;
        try (BookingDAO bookings = new BookingDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                if (controllableArea.offset != null) {
                    //Thermostat can be reset
                    if ("empty".equals(bookings.getTonight(controllableArea.room))
                            || !bookings.getTonight(controllableArea.room).equals(bookings.getNow(controllableArea.room))) {
                        HeatingProperties prop = new HeatingProperties();
                        String response = Request.Get("http://" + prop.localIp + ":" + prop.masterPort + "/knx/"
                                + controllableArea.modusAddress + "/write/int/" + controllableArea.defaultModus
                                + "/").execute().returnContent().asString();
                        if (!response.contains("\"status\": \"OK\"")) {
                            LogstashLogger.INSTANCE.message("ERROR: unexpected response while resetting thermostat modus "
                                    + controllableArea + ": " + response);
                        }
                        response = Request.Get("http://" + prop.localIp + ":" + prop.masterPort + "/knx/"
                                + controllableArea.offset + "/write/int/0/").execute().returnContent().asString();
                        if (!response.contains("\"status\": \"OK\"")) {
                            LogstashLogger.INSTANCE.message("ERROR: unexpected response while resetting thermostat offset "
                                    + controllableArea + ": " + response);
                        } else {
                            setpointCount++;
                        }
                    }
                }
            }
            System.out.println("I have reset " + setpointCount + " thermostats");
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: while resetting thermostats " + e.getMessage());
        }
    }
}
