package speaker;

import building.Building;
import building.HeatZone;
import control.HeatingControl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import util.LineProtocolUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Posts the ZoneState to Influx.
 *
 * Run me periodically.
 */
public class StateSpeaker extends AbstractHandler {

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int count = 0;
        try (FluxLogger flux = new FluxLogger()) {
            for (HeatZone zone : Building.INSTANCE.zones) {
                flux.message(LineProtocolUtil.protocolLine(zone, "state"
                        , HeatingControl.INSTANCE.controlState.get(zone).peekLast().valve ? "1i" : "0i"));
                count++;
            }
        }
        System.out.println("Posted " + count + " states to InfluxDB");
    }
}
