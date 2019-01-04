package speaker;

import building.ControllableArea;
import dao.SetpointDAO;
import util.LineProtocolUtil;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Publishes the thermostat setpoints to InfluxDB
 */
public class SetpointSpeaker implements Runnable {

    @Override
    public void run() {
        int count = 0;

        try (FluxLogger flux = new FluxLogger(); SetpointDAO dao = new SetpointDAO()) {
            for (ControllableArea controllableArea : ControllableArea.values()) {
                flux.message(LineProtocolUtil.protocolLine(controllableArea, "setpoint"
                        , Double.toString(dao.getActual(controllableArea))));
                count++;
            }
        } catch (UnknownHostException | SocketException e) {
            LogstashLogger.INSTANCE.error("Can't find InfluxDB for SetpointSpeaker " + e.getMessage());
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Can't connect to Redis " + e.getMessage());
        }
        LogstashLogger.INSTANCE.info("Posted " + count + " setpoints to InfluxDB");
    }
}
