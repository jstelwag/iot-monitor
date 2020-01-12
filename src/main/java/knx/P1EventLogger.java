package knx;

import speaker.FluxLogger;
import speaker.LogstashLogger;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class P1EventLogger implements EventHandler {
    private final Map<String, String> fluxList = new HashMap<>();
    private String event;
    private KNXAddress knx;

    public P1EventLogger() {
        fluxList.put("0/2/151", "P1_Actual_W");
        fluxList.put("0/2/152", "P1_L1_Actual_W");
        fluxList.put("0/2/153", "P1_L2_Actual_W");
        fluxList.put("0/2/154", "P1_L3_Actual_W");
    }

    @Override
    public EventHandler onEvent(String event, KNXAddress knx) {
        this.event = event;
        this.knx = knx;
        return this;
    }

    @Override
    public void run() {
        if (knx.type == KNXAddress.Type.P1) {
            if (fluxList.containsKey(knx.address) && event.contains("tpdu 00 80")) {
                try (FluxLogger flux = new FluxLogger()) {
                    int p1Value = Integer.parseInt(
                            event.split("tpdu 00 80")[1].replaceAll(" ", "").trim()
                            , 16);
                    flux.message("P1,metric=" + fluxList.get(knx.address) + " value=" + p1Value + "i");
                } catch (SocketException | UnknownHostException e) {
                    LogstashLogger.INSTANCE.warn("Could not connect with InfluxDB to upload p1. " + e.getMessage());
                }
            }
        }
    }
}
