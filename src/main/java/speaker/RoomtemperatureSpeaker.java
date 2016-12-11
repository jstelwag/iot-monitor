package speaker;

import building.Building;
import dao.TemperatureDAO;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Publishes the controllableRoom temperature to InfluxDB
 */
public class RoomtemperatureSpeaker implements Runnable {

    @Override
    public void run() {
        int count = 0;

        try (FluxLogger flux = new FluxLogger(); TemperatureDAO temperatures = new TemperatureDAO()) {
            for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                Double temperature = temperatures.getActual(controllableRoom);
                if (temperature != null) {
                    flux.message(LineProtocolUtil.protocolLine(controllableRoom, "temperature"
                            , Double.toString(temperature)));
                    count++;
                }
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: failed logging temperatures " + e.getMessage());
        }
        System.out.println("Posted " + count + " room temperatures to InfluxDB");
    }
}
