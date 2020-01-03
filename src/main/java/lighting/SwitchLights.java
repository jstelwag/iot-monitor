package lighting;

import building.Room;
import dao.LightingStateDAO;
import knx.KNXAddress;
import knx.KNXAddressList;
import knx.KNXAccess;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static lighting.Schedule.Location;

public class SwitchLights {

    public static final double ON_THRESHOLD = 0.3;

    static KNXAddressList addressList = new KNXAddressList();

    public enum LightState {
        Dusk, Midnight, Dawn, WinterMorning
    }

    public static int allOff(Room room) {
        int retVal = 0;
        for (KNXAddress address : addressList.addressesByRoom(room, "all")) {
            try {
                KNXAccess.writeBoolean(new GroupAddress(address.address), false);
                retVal++;
            } catch (KNXException e) {
                LogstashLogger.INSTANCE.error("Failed to switch room " + room
                        + "@" + address + ", " + e.getMessage());
            }
        }

        return retVal;
    }

    public static int toggleLights(Room room) {
        int retVal = 0;

        Set<KNXAddress> roomLights = addressList.addressesByRoom(room, "toggle");
        int onCount = 0;
        try (LightingStateDAO dao = new LightingStateDAO()) {
            for (KNXAddress address : roomLights) {
                if (dao.getState(address.address)) onCount++;
            }
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Failed to connect to Redis for state count: " + e.getMessage());
        }
        boolean desiredState = onCount/roomLights.size() < ON_THRESHOLD;

        for (KNXAddress address : roomLights) {
            try {
                KNXAccess.writeBoolean(new GroupAddress(address.address), desiredState);
                retVal++;
                Thread.sleep(50); //Wait a little to reduce LED switching peaks
            } catch (KNXException | InterruptedException e) {
                LogstashLogger.INSTANCE.error("Failed to toggle room " + room + " @" + address + ", " + e.getMessage());
            }
        }

        return desiredState ? retVal : -retVal;
    }

    public static void switchLights(List<String> lights, boolean on) {
        LogstashLogger.INSTANCE.info("Switching - lights " + (on ? "on" : "off"));
        for (String address : lights) {
            try {
                KNXAccess.writeBoolean(new GroupAddress(address), on);
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
                    KNXAccess.writeBoolean(new GroupAddress(address), (state == LightState.Dusk || state == LightState.WinterMorning));
                    Thread.sleep(100);
                } catch (KNXException | InterruptedException e) {
                    LogstashLogger.INSTANCE.error("KNX switching problem @" + location + ", " + e.getMessage());
                }
            }
        }
    }
}
