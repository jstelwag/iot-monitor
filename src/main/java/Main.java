import control.ControlCalculator;
import control.Setpoint;
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

    public static void main(String[] args) throws IOException {
        System.out.println("Monitor " + args[0]);
        HeatingProperties prop = new HeatingProperties();
        if ("http".equals(args[0])) {
            startHttp(prop.masterPort);
        } else if ("setpointspeaker".equals(args[0])) {
            new SetpointSpeaker().run();
        } else if ("setpoint".equals(args[0])) {
            new Setpoint().run();
        } else if ("fluxtemperatures".equals(args[0])) {
            new RoomtemperatureSpeaker().run();
        } else if ("statespeaker".equals(args[0])) {
            new StateSpeaker().run();
        } else if ("control".equals(args[0])) {
            new ControlCalculator().run();
        } else if ("customerspeaker".equals(args[0])) {
            new CustomerNameSpeaker().run();
        } else if ("beds24".equals(args[0])) {
            new Beds24BookingRetriever(prop.beds24ApiKey, prop.beds24PropKey).run();
        } else if ("iot".equals(args[0])) {
            LogstashLogger.INSTANCE.message("start iot");
            new IoTListener(prop.iotPort).run();
        }
    }

    private static void startHttp(int port) {
        LogstashLogger.INSTANCE.message("start http");

        Server httpServer = new Server(port);
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
            } catch (InterruptedException e) {
            }
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
        ContextHandler roomResetContext = new ContextHandler("/roomreset");
        roomResetContext.setHandler(new RoomResetHandler());
        ContextHandler furnaceContext = new ContextHandler("/furnace");
        furnaceContext.setHandler(new FurnaceHandler());
        ContextHandler valveGroupContext = new ContextHandler("/valvegroup");
        valveGroupContext.setHandler(new ValveGroupHandler());
        ContextHandler knxtemperatures = new ContextHandler("/knxtemperatures");
        knxtemperatures.setHandler(new RoomTemperatureHandler());
        ContextHandler redisContext = new ContextHandler("/redis");
        redisContext.setHandler(new RedisHandler());

        ContextHandler echoContext = new ContextHandler("/echo");
        echoContext.setHandler(new EchoHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { stateContext, statusContext, restContext, valveGroupContext
                , roomResetContext, furnaceContext, knxtemperatures, redisContext
                , echoContext});
        return contexts;
    }

    private static void removeHeaders(Server server) {
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
