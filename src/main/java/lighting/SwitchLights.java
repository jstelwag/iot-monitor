package lighting;

import knx.KNXLink;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import java.util.List;

import static lighting.Schedule.*;

public class SwitchLights {
    public static void switchLight(List<String> lights, Location location, boolean onOrOff) {
        Jedis jedis = new Jedis("localhost");
        LogstashLogger.INSTANCE.info("Switching " + location + " - lights " + (onOrOff ? "on" : "off"));
        jedis.set(location + ".state", onOrOff ? "ON" : "OFF");
        try {
            for (String address : lights) {
                KNXLink.getInstance().writeBoolean(new GroupAddress(address), onOrOff);
                Thread.sleep(100);
            }
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.error("KNX switching problem @" + location + ", " + e.getMessage());
        }
    }
}
