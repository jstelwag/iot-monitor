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

    public static int allOff(Room room) {
        int retVal = 0;
        try (Jedis jedis = new Jedis("localhost")) {
            jedis.set(room + ".all.state", "OFF");
            for (KNXAddress address : addressList.addressesByRoom(room, KNXAddress.Type.button)) {
                try {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address.address), false);
                    retVal++;
                } catch (KNXException | InterruptedException e) {
                    LogstashLogger.INSTANCE.error("Failed to switch room " + address + ", " + e.getMessage());
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

            for (KNXAddress address : addressList.addressesByRoom(room, KNXAddress.Type.button)) {
                try {
                    KNXLink.getInstance().writeBoolean(new GroupAddress(address.address), desiredState);
                    retVal++;
                    Thread.sleep(50); //Wait a little to reduce LED switching peaks
                } catch (KNXException | InterruptedException e) {
                    LogstashLogger.INSTANCE.error("Failed to toggle room " + address + ", " + e.getMessage());
                }
            }
        }

        return desiredState ? retVal : -retVal;
    }

    public static void switchPublicLight(List<String> lights, Location location, boolean onOrOff) {

        LogstashLogger.INSTANCE.info("Switching " + location + " - lights " + (onOrOff ? "on" : "off"));
        try (Jedis jedis = new Jedis("localhost")) {
            jedis.set(location + ".state", onOrOff ? "ON" : "OFF");
            for (String address : lights) {
                KNXLink.getInstance().writeBoolean(new GroupAddress(address), onOrOff);
                Thread.sleep(100);
            }
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.error("KNX switching problem @" + location + ", " + e.getMessage());
        }
    }
}
