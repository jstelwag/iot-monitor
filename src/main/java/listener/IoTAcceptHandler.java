package listener;

import org.apache.commons.io.IOUtils;
import speaker.LogstashLogger;
import speaker.LogstashTimedSpeaker;
import speaker.TemperatureSpeaker;
import util.LineProtocolUtil;

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
                System.out.print("IoT accept from simple bridge ");
                System.out.print(LineProtocolUtil.device(request.toString()) + ": ");
                if (request.toString().split(":").length > 2) {
                    System.out.println(request.toString().split(":")[1]);
                }
                out.println(dispatcher.actuatorsOut());
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
