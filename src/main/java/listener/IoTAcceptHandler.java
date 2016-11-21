package listener;

import org.apache.commons.io.IOUtils;
import speaker.LogstashLogger;

import java.io.*;
import java.net.Socket;

public class IoTAcceptHandler implements Runnable {

    private final Socket socket; //socket is closed in run()

    public IoTAcceptHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //In the end you need to close in, out and socket -- not earlier!
        BufferedReader in;
        PrintWriter out;
        StringBuilder request = new StringBuilder();
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (in.ready()) {
                request.append(in.readLine());
                if (in.ready()) {
                    request.append("\r\n");
                }
            }
            out = new PrintWriter(socket.getOutputStream(), true);
            IoTRequestDispatcher dispatcher = new IoTRequestDispatcher(request.toString());
            if (dispatcher.isGroup()) {
                dispatcher.logState();
                out.println(dispatcher.actuatorsOut());
            } else if (request.toString().startsWith("koetshuis_trap_15")) {
                //TODO gooi weg
                out.println("01010101010101018E");
            } else {
                LogstashLogger.INSTANCE.message("IoTAccept with unrecognized content " + dispatcher.lineIn);
                System.out.println("Unrecognized IoT request: " + dispatcher.lineIn + "?");
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Error processing IoT response " + request);
            LogstashLogger.INSTANCE.message("IoTAcceptHandler ERROR " + e.toString());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }
}
