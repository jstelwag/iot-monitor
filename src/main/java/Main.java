import control.ControlCalculator;
import handlers.*;
import listener.IoTListener;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import retriever.Beds24BookingRetriever;
import speaker.*;
import util.HeatingProperties;

import java.io.IOException;

public class Main {

    /**
     * Add a properties file (monitor.conf) in the jar directory
     */
    static final HeatingProperties prop = new HeatingProperties();

    public static void main(String[] args) throws IOException {
        System.out.println("Booting");
        LogstashLogger.INSTANCE.message("start");
        //InfluxDBTimedSpeaker.INSTANCE.message("hello"); //make sure this starts in the main thread
        new Thread(new IoTListener(prop.iotPort)).start();
        try {
            //speed things up, I need booking information in order to control things
            new Beds24BookingRetriever(prop.beds24ApiKey, prop.beds24PropKey).run();
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
            LogstashLogger.INSTANCE.message("FATAL: failed to start http listeners " + e.toString());
            System.out.println(e.toString());
            System.exit(0);
        }

        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
            try {
                //hello
            } catch (RuntimeException e) {
                LogstashLogger.INSTANCE.message("ERROR: exception occurred at the regular speaker scheduling " + e.toString());
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
        ContextHandler controlContext = new ContextHandler("/control");
        controlContext.setHandler(new ControlCalculator());
        ContextHandler stateSpeakerContext = new ContextHandler("/stateSpeaker");
        stateSpeakerContext.setHandler(new StateSpeaker());
        ContextHandler roomTemperatureSpeakerContext = new ContextHandler("/roomTemperatureSpeaker");
        roomTemperatureSpeakerContext.setHandler(new RoomtemperatureSpeaker());
        ContextHandler setpointContext = new ContextHandler("/setpointSpeaker");
        setpointContext.setHandler(new SetpointSpeaker());
        ContextHandler furnaceContext = new ContextHandler("/furnace");
        furnaceContext.setHandler(new FurnaceHandler());

        ContextHandler echoContext = new ContextHandler("/echo");
        echoContext.setHandler(new EchoHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { stateContext, statusContext, restContext
                , beds24Context, roomResetContext, roomTemperatureContext, controlContext
                , stateSpeakerContext, roomTemperatureSpeakerContext, setpointContext, echoContext});
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
