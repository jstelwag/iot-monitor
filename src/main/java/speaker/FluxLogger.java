package speaker;

import util.HeatingProperties;

import java.io.IOException;
import java.net.*;

/**
 * Created by Jaap on 26-5-2016.
 */
public class FluxLogger {
    public final static FluxLogger INSTANCE = new FluxLogger();

    InetAddress host;
    final int port;

    private FluxLogger() {
        final HeatingProperties properties = new HeatingProperties();
        try {
            host = InetAddress.getByName(properties.influxIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        port = properties.influxPort;
    }

    public void message(String line) {
        byte[] data = line.getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("ERROR for UDP connection, @" + host.getHostAddress() + ":" + port);
        }
    }
}
