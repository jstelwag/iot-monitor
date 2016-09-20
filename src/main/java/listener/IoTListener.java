package listener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import speaker.LogstashTimedSpeaker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class IoTListener implements Runnable, Closeable {
    private final int WORKERS = 4;

    final ServerSocket listener;
    private final ThreadPoolExecutor executor;

    public IoTListener(int iotPort) throws IOException {
        listener = new ServerSocket(iotPort);
        // 2 threads active, max 4 threads before
        executor = new ThreadPoolExecutor(2, WORKERS, 0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
        addShutdownHook();
        System.out.println("Started IoT listener at " + iotPort);
    }

    public void run() {
        while (true) {
            try {
                accept();
            } catch (IOException e) {
                LogstashTimedSpeaker.INSTANCE.message("MasterController", "ERROR IoTListener connection failure, trying to continue "
                        + e.toString());
            }
        }
    }

    private void accept() throws IOException {

        Socket socket = listener.accept(); // waits for a connection
        if (executor.getActiveCount() == WORKERS) {
            LogstashTimedSpeaker.INSTANCE.message("MasterController", "IoTListener rejecting connection");
            socket.close();
        }
        executor.execute(new IoTAcceptHandler(socket));
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(listener);
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
