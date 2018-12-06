package speaker;

import building.ControllableArea;
import dao.TemperatureDAO;
import util.LineProtocolUtil;

import java.io.IOException;

/**
 * Publishes the controllableArea temperature to InfluxDB
 */
public class RoomtemperatureSpeaker implements Runnable {

    @Override
    public void run() {
        int count = 0;

        try (FluxLogger flux = new FluxLogger(); TemperatureDAO temperatures = new TemperatureDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                Double temperature = temperatures.getActual(controllableArea);
                if (temperature != null) {
                    flux.message(LineProtocolUtil.protocolLine(controllableArea, "temperature"
                            , Double.toString(temperature)));
                    count++;
                }
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Failed logging temperatures " + e.getMessage());
        }
        LogstashLogger.INSTANCE.info("Posted " + count + " room temperatures to InfluxDB");
    }
}
