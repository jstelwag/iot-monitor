package speaker;

import util.HeatingProperties;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;

/**
 * Created by Jaap on 26-5-2016.
 */
public class FluxLogger implements Closeable {

    private final InetAddress host;
    private final int port;
    private final DatagramSocket socket;

    public FluxLogger() throws SocketException, UnknownHostException {
        final HeatingProperties properties = new HeatingProperties();
        try {
            host = InetAddress.getByName(properties.influxIp);
        } catch (UnknownHostException e) {
            LogstashLogger.INSTANCE.error("Tried to set up InluxDB client for unknown host " + e.toString());
            throw e;
        }
        port = properties.influxPort;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Socket error " + e.toString());
            LogstashLogger.INSTANCE.error("Unable to open socket to connect to InfluxDB @" + host + ":" + port
                    + " " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void close() {
        if (socket != null)
            socket.close();
    }

    public void message(String line) {
        byte[] data = line.getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
            socket.send(packet);
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Faulty UDP connection, @" + host.getHostAddress() + ":" + port);
        }
    }
}
