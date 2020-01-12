package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.process.ProcessCommunicator;

import java.util.Random;

/**
 * Provides access to devices on the knx bus. Access is via GroupAddress and unrestricted in any way.
 * There are two KNX bridges, by default a random brigde is chosen. If the connection fails the other bridge is used.
 * If that also fails a KNX exception is thrown.
 */
public class KNXAccess {

    public static double readFloat(GroupAddress address) throws KNXException {
        double retVal;
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            retVal = KNXLink.getInstance(bridge).pc().readFloat(address);
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "readFloat reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                retVal = KNXLink.getInstance(bridge).pc().readFloat(address);
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException | InterruptedException e2) {
                LogstashLogger.INSTANCE.warn("Attempting readFloat on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting readFloat on both knx links failed", e2);
            }
        }
        return retVal;
    }

    public static boolean readBoolean(GroupAddress address) throws KNXException {
        boolean retVal;
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            retVal = KNXLink.getInstance(bridge).pc().readBool(address);
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "readBoolean reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                retVal = KNXLink.getInstance(bridge).pc().readBool(address);
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException | InterruptedException e2) {
                LogstashLogger.INSTANCE.warn("Attempting readBoolean on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting readBoolean on both knx links failed", e2);
            }
        }
        return retVal;
    }

    public static int readInt(GroupAddress address) throws KNXException {
        int retVal;
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            retVal = KNXLink.getInstance(bridge).pc().readUnsigned(address, ProcessCommunicator.UNSCALED);
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "readInt reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                retVal = KNXLink.getInstance(bridge).pc().readUnsigned(address, ProcessCommunicator.UNSCALED);
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException | InterruptedException e2) {
                LogstashLogger.INSTANCE.warn("Attempting readInt on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting readInt on both knx links failed", e2);
            }
        }
        return retVal;
    }

    public static String readString(GroupAddress address) throws KNXException {
        String retVal;
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            retVal = KNXLink.getInstance(bridge).pc().read(new StateDP(address, "string"));
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "readString reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                retVal = KNXLink.getInstance(bridge).pc().read(new StateDP(address, "string"));
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException | InterruptedException e2) {
                LogstashLogger.INSTANCE.warn("Attempting readString on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting readString on both knx links failed", e2);
            }
        }
        return retVal;
    }

    public static void writeFloat(GroupAddress address, float soll) throws KNXException {
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            KNXLink.getInstance(bridge).pc().write(address, soll, true);
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "writeFloat reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                KNXLink.getInstance(bridge).pc().write(address, soll, true);
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Attempting writeFloat on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting writeFloat on both knx links failed", e2);
            }
        }
    }

    public static void writeBoolean(GroupAddress address, boolean soll) throws KNXException {
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            KNXLink.getInstance(bridge).pc().write(address, soll);
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "writeBoolean reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                KNXLink.getInstance(bridge).pc().write(address, soll);
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Attempting writeBoolean on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting writeBoolean on both knx links failed", e2);
            }
        }
    }

    public static void writeInt(GroupAddress address, int soll) throws KNXException {
        int bridge = new Random().nextBoolean() ? 1 : 0;
        try {
            KNXLink.getInstance(bridge).pc().write(address, soll, ProcessCommunicator.UNSCALED);
            KNXLink.getInstance(bridge).lastCheck(true);
        } catch (KNXException e) {
            LogstashLogger.INSTANCE.warn(String.format(
                    "writeInt reported an exception at knx link #%d, retrying with other knx link: %s", bridge, e.getMessage()));
            KNXLink.getInstance(bridge).lastCheck(false);
            //Switch the knx bridge and try again
            bridge = bridge == 0 ? 1 : 0;
            try {
                KNXLink.getInstance(bridge).pc().write(address, soll, ProcessCommunicator.UNSCALED);
                KNXLink.getInstance(bridge).lastCheck(true);
            } catch (KNXException e2) {
                LogstashLogger.INSTANCE.warn("Attempting writeInt on both knx links failed: " + e2.getMessage());
                KNXLink.getInstance(bridge).lastCheck(false);
                throw new KNXException("Attempting writeInt on both knx links failed", e2);
            }
        }
    }
}
