import control.ControlCalculator;
import handlers.*;
import listener.IoTListener;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import retriever.Beds24BookingRetriever;
import retriever.KNXRoomTemperatures;
import speaker.*;
import util.HeatingProperties;

import java.io.IOException;

public class Main {

    /**
     * Add a properties file (heating.properties) in the jar directory
     */
    static final HeatingProperties prop = new HeatingProperties();

    public static void main(String[] args) throws IOException {
        System.out.println("Booting");
        LogstashTimedSpeaker.INSTANCE.message("MasterController", "start");
        InfluxDBTimedSpeaker.INSTANCE.message("hello"); //make sure this starts in the main thread
        new Thread(new IoTListener(prop.iotPort)).start();
        try {
            //speed things up, I need booking information in order to control things
            new Beds24BookingRetriever(prop.beds24ApiKey, prop.beds24PropKey).run();
            //also to have the set points and temperatures is nice.
            new KNXRoomTemperatures().run();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        Server httpServer = new Server(prop.masterPort);
        httpServer.setHandler(contexts());
        removeHeaders(httpServer);
        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(true);
        httpServer.addBean(errorHandler);

        try {
            httpServer.start();
            httpServer.join();
        } catch (Exception e) {
            LogstashTimedSpeaker.INSTANCE.message("MasterController", "FATAL: failed to start http listeners " + e.toString());
            System.out.println(e.toString());
            System.exit(0);
        }

        long last30seconds = System.currentTimeMillis();

        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
            try {
                if (last30seconds + 30000 < System.currentTimeMillis()) {
                    last30seconds = System.currentTimeMillis();
                    new Thread(new ControlCalculator()).start();
                    new Thread(new StateSpeaker()).start();
                    new Thread(new RoomtemperatureSpeaker()).run();
                    new Thread(new SetpointSpeaker()).run();
                }
            } catch (RuntimeException e) {
                LogstashTimedSpeaker.INSTANCE.message("MasterController", "ERROR: exception occurred at the regular speaker scheduling " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private static ContextHandlerCollection contexts() {
        ContextHandler stateContext = new ContextHandler("/state");
        stateContext.setHandler(new StateHandler());
        ContextHandler statusContext = new ContextHandler("/status");
        statusContext.setHandler(new StatusHandler());
        ContextHandler restContext = new ContextHandler("/rest");
        restContext.setHandler(new RestHandler());
        ContextHandler beds24Context = new ContextHandler("/beds24");
        beds24Context.setHandler(new Beds24Handler());
        ContextHandler roomResetContext = new ContextHandler("/roomreset");
        roomResetContext.setHandler(new RoomResetHandler());
        ContextHandler roomTemperatureContext = new ContextHandler("/knxtemperatures");
        roomTemperatureContext.setHandler(new RoomTemperatureHandler());
        ContextHandler echoContext = new ContextHandler("/echo");
        echoContext.setHandler(new EchoHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { stateContext, statusContext, restContext
                , beds24Context, roomResetContext, roomTemperatureContext, echoContext});
        return contexts;
    }


    static void removeHeaders(Server server) {
        for(Connector y : server.getConnectors()) {
            for(ConnectionFactory x : y.getConnectionFactories()) {
                if(x instanceof HttpConnectionFactory) {
                    ((HttpConnectionFactory)x).getHttpConfiguration().setSendServerVersion(false);
                    ((HttpConnectionFactory)x).getHttpConfiguration().setSendDateHeader(false);
                }
            }
        }
    }
}
