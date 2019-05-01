package lighting;

import building.Room;
import knx.KNXAddress;
import knx.KNXAddressList;
import knx.KNXLink;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import java.util.List;

import static lighting.Schedule.*;

public class SwitchLights {
    static KNXAddressList addressList = new KNXAddressList();

    public enum LightState {
        Dusk, Midnight, Dawn, WinterMorning
    }

    public static int allOff(Room room) {
        int retVal = 0;
        try (Jedis jedis = new Jedis("localhost")) {
            jedis.set(room + ".all.state", "OFF");
            for (KNXAddress address : addressList.addressesByRoom(room, "all")) {
                try {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address.address), false);
                    retVal++;
                } catch (KNXException | InterruptedException e) {
                    LogstashLogger.INSTANCE.error("Failed to switch room " + room
                            + "@" + address + ", " + e.getMessage());
                }
            }
        }

        return retVal;
    }

    public static int toggleLights(Room room) {
        int retVal = 0;
        boolean desiredState;

        try (Jedis jedis = new Jedis("localhost")) {
            desiredState = !"ON".equals(jedis.get(room + ".all.state"));
            jedis.set(room + ".all.state", desiredState ? "ON" : "OFF");

            for (KNXAddress address : addressList.addressesByRoom(room, "toggle")) {
                try {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address.address), desiredState);
                    retVal++;
                    Thread.sleep(50); //Wait a little to reduce LED switching peaks
                } catch (KNXException | InterruptedException e) {
                    LogstashLogger.INSTANCE.error("Failed to toggle room " + room + " @" + address + ", " + e.getMessage());
                }
            }
        }

        return desiredState ? retVal : -retVal;
    }

    public static void switchLights(List<String> lights, boolean on) {
        LogstashLogger.INSTANCE.info("Switching - lights " + (on ? "on" : "off"));
        for (String address : lights) {
            try {
                KNXLink.getInstance().writeBoolean(new GroupAddress(address), on);
                Thread.sleep(100);
            } catch (KNXException | InterruptedException e) {
                 LogstashLogger.INSTANCE.error("KNX switching problem " + e.getMessage());
            }
        }
    }

    public static void switchPublicLight(List<String> lights, Location location, LightState state) {
        LogstashLogger.INSTANCE.info("Switching " + location + " - lights " + state);
        try (Jedis jedis = new Jedis("localhost")) {
            jedis.set(location + ".state", state.name());
            for (String address : lights) {
                try {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address), (state == LightState.Dusk || state == LightState.WinterMorning));
                    Thread.sleep(100);
                } catch (KNXException | InterruptedException e) {
                    LogstashLogger.INSTANCE.error("KNX switching problem @" + location + ", " + e.getMessage());
                }
            }
        }
    }
}
