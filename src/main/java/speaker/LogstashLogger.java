package speaker;

import util.HeatingProperties;

import java.io.IOException;
import java.net.*;

/**
 * Created by Jaap on 26-5-2016.
 */
public class LogstashLogger {

    public final static LogstashLogger INSTANCE = new LogstashLogger();
    InetAddress host;
    final int port;

    private LogstashLogger() {
        final HeatingProperties properties = new HeatingProperties();
        port = properties.logstashPort;

        try {
            host = InetAddress.getByName(properties.logstashIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void message(String line) {
        message("iot-monitor", line);
    }

    public void message(String who, String line) {
        send(who + ": " + line);
    }

    private void send(String message) {
        try (DatagramSocket socket = new DatagramSocket()){
            byte[] data = message.getBytes();
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("ERROR for UDP connection " + socket.isConnected() + ", @"
                        + host.getHostAddress() + ":" + port + ", socket " + socket.isBound() + ". For " + message);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
