package speaker;

import building.Building;
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
            for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                flux.message(LineProtocolUtil.protocolLine(controllableRoom, "setpoint", Double.toString(dao.get(controllableRoom))));
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
