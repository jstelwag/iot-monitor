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
public class LogstashTimedSpeaker implements Closeable {

    public final static LogstashTimedSpeaker INSTANCE = new LogstashTimedSpeaker();
    Timer timer;
    Queue<String> queue = new LinkedList<>();

    InetAddress host;
    final int port;
    DatagramSocket socket;

    private LogstashTimedSpeaker() {
        System.out.println("Starting timed Logstash Speaker");
        final HeatingProperties properties = new HeatingProperties();
        port = properties.logstashPort;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            host = InetAddress.getByName(properties.logstashIp);
        } catch (UnknownHostException e) {
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

    public void message(String who, String message) {
        queue.add(who + ": " + message);
    }

    public void processQueue(boolean all) {
        if (!queue.isEmpty()) {
            send(queue.poll());
            while (all && !queue.isEmpty()) {
                send(queue.poll());
            }
        }
    }

    private void send(String message) {
        byte[] data = message.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("ERROR for UDP connection " + socket.isConnected() + ", @"
                    + host.getHostAddress() + ":" + port + ", socket " + socket.isBound() + ". For " + message);
        }
    }

    @Override
    public void close() {
        System.out.println("Shutting down Timed Logstash Speaker, thank you!");
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
