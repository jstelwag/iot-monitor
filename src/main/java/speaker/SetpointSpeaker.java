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
                flux.message(LineProtocolUtil.protocolLine(controllableArea, "setpoint", Double.toString(dao.get(controllableArea))));
                count++;
            }
        } catch (UnknownHostException | SocketException e) {
            LogstashLogger.INSTANCE.message("ERROR: can't find InfluxDB for SetpointSpeaker " + e.getMessage());
        } catch (IOException e) {
            LogstashLogger.INSTANCE.message("ERROR: can't connect to Redis " + e.getMessage());
        }
        System.out.println("Posted " + count + " setpoints to InfluxDB");
    }
}
