package speaker;

import util.HeatingProperties;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jaap on 26-5-2016.
 */
public class InfluxDBTimedSpeaker implements Closeable {
    public final static InfluxDBTimedSpeaker INSTANCE = new InfluxDBTimedSpeaker();
    Timer timer;

    InetAddress host;
    final int port;
    DatagramSocket socket;

    Queue<String> queue = new LinkedList<>();

    private InfluxDBTimedSpeaker() {
        System.out.println("Starting timed InfluxDB speaker");
        LogstashTimedSpeaker.INSTANCE.message("MasterController", "InfluxDB launched");
        final HeatingProperties properties = new HeatingProperties();
        try {
            host = InetAddress.getByName(properties.influxIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        port = properties.influxPort;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        timer = new Timer();
        timer.schedule(new SpeakTask(), 1000, 50);
        addShutdownHook();
    }

    private class SpeakTask extends TimerTask {
        public void run() {
            processQueue(false);
        }
    }

    public void message(String line) {
        queue.add(line);
    }

    public void processQueue(boolean all) {
        if (!queue.isEmpty()) {
            send(queue.poll());
            while (all && !queue.isEmpty()) {
                send(queue.poll());
            }
        }
    }

    private void send(String line) {
        byte[] data = line.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("ERROR for UDP connection " + socket.isConnected() + ", @"
                    + host.getHostAddress() + ":" + port + ", socket " + socket.isBound());
        }
    }

    @Override
    public void close() {
        System.out.println("Shutting down Timed InfluxDB Speaker, thanks for using me");
        processQueue(true);
        timer.cancel();
        socket.close();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }
}
