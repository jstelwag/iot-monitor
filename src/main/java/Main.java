import control.ControlCalculator;
import control.SetpointControl;
import control.ZoneModulation;
import handlers.*;
import lighting.AlwaysOn;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import retriever.Beds24BookingRetriever;
import speaker.*;
import util.HeatingProperties;


public class Main {

    public static void main(String[] args) {
        LogstashLogger.INSTANCE.info("Monitor " + args[0]);
        HeatingProperties prop = new HeatingProperties();
        if ("http".equals(args[0])) {
            startHttp(prop.masterPort);
        } else if ("setpointspeaker".equals(args[0])) {
            new SetpointSpeaker().run();
        } else if ("setpoint".equals(args[0])) {
            new SetpointControl().run();
        } else if ("fluxtemperatures".equals(args[0])) {
            new RoomtemperatureSpeaker().run();
        } else if ("statespeaker".equals(args[0])) {
            new StateSpeaker().run();
        } else if ("control".equals(args[0])) {
            new ControlCalculator().run();
        } else if ("modulate".equals(args[0])) {
            new ZoneModulation().run();
        } else if ("beds24".equals(args[0])) {
            new Beds24BookingRetriever(prop.beds24ApiKey, prop.beds24PropKey).run();
        } else if ("roomreset".equals(args[0])) {
            new KNXRoomReset().run();
        }
    }

    private static void startHttp(int port) {

        new AlwaysOn();

        LogstashLogger.INSTANCE.info("Starting http");

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
            LogstashLogger.INSTANCE.fatal("Failed to start http listener " + e.toString());
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
                LogstashLogger.INSTANCE.error("Exception occurred at the regular speaker scheduling " + e.toString());
            }
        }
    }

    private static ContextHandlerCollection contexts() {
        ContextHandler stateContext = new ContextHandler("/state");
        stateContext.setHandler(new StateHandler());
        ContextHandler statusContext = new ContextHandler("/status");
        statusContext.setHandler(new StatusHandler());
        ContextHandler restContext = new ContextHandler("/rest/heating");
        restContext.setHandler(new HeatingHandler());
        ContextHandler furnaceContext = new ContextHandler("/furnace");
        furnaceContext.setHandler(new FurnaceHandler());
        ContextHandler valveGroupContext = new ContextHandler("/valvegroup");
        valveGroupContext.setHandler(new ValveGroupHandler());
        ContextHandler knxtemperatures = new ContextHandler("/knxtemperatures");
        knxtemperatures.setHandler(new KNXTemperaturesHandler());
        ContextHandler redisContext = new ContextHandler("/redis");
        redisContext.setHandler(new RedisHandler());
        ContextHandler knxContext = new ContextHandler("/knx");
        knxContext.setHandler(new KNXHandler());
        ContextHandler roomContext = new ContextHandler("/room");
        roomContext.setHandler(new KNXRoomHandler());
        ContextHandler p1Context = new ContextHandler("/p1");
        p1Context.setHandler(new P1Handler());
        ContextHandler timerContext = new ContextHandler("/timer");
        timerContext.setHandler(new TimerHandler());
        ContextHandler knxUpdateContext = new ContextHandler("/knx-update");
        knxUpdateContext.setHandler(new KNXStateUpdateHandler());

        ContextHandler echoContext = new ContextHandler("/echo");
        echoContext.setHandler(new EchoHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { stateContext, statusContext, restContext, valveGroupContext
                , furnaceContext, knxtemperatures, redisContext, knxContext, roomContext, p1Context
                , timerContext, knxUpdateContext, echoContext});
        return contexts;
    }

    private static void removeHeaders(Server server) {
        for (Connector y : server.getConnectors()) {
            for (ConnectionFactory x : y.getConnectionFactories()) {
                if (x instanceof HttpConnectionFactory) {
                    ((HttpConnectionFactory)x).getHttpConfiguration().setSendServerVersion(false);
                    ((HttpConnectionFactory)x).getHttpConfiguration().setSendDateHeader(false);
                }
            }
        }
    }
}
