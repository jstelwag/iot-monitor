package knx;

import building.Building;
import building.Room;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import speaker.LogstashLogger;
import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaap on 15-12-2016.
 */
public class KNXAddressList {
    final Map<String, KNXAddress> addresses = new HashMap<>();
    public static final Pattern knxAddressPattern = Pattern.compile("\\d{1,3}" + Pattern.quote("/") + "\\d{1,3}" + Pattern.quote("/") + "\\d{1,3}");

    public KNXAddressList() {
        ClassLoader classLoader = getClass().getClassLoader();
        int lineNumber = 0;
        try (Reader in = new InputStreamReader(
                classLoader.getResourceAsStream("knx-addresses.txt"))) {
            Iterable<CSVRecord> records = CSVFormat.TDF.parse(in);
            for (CSVRecord record : records) {
                lineNumber++;
                String address = record.get(0);

                if (addresses.put(address
                        , new KNXAddress(address, KNXAddress.Type.valueOf(record.get(1))
                                , Building.Construction.valueOf(record.get(2))
                                , Room.valueOf(record.get(3)), record.get(4), record.get(5))) != null) {
                    LogstashLogger.INSTANCE.error("Duplicate address in knx-addresses.txt " + record.get(0));
                }
                if ("0".equals(address.split("/")[1])) {
                    // button style device. expand
                    addresses.put(address.replace("/0/", "/1/")
                            , new KNXAddress(address, KNXAddress.Type.button_status
                                    , Building.Construction.valueOf(record.get(2))
                                    , Room.valueOf(record.get(3)), record.get(4), record.get(5)));
                } else if ("2".equals(address.split("/")[1])) {
                    // dimmer style device. expand
                    addresses.put(address.replace("/2/", "/3/")
                            , new KNXAddress(address, KNXAddress.Type.dimmer_absolute
                                    , Building.Construction.valueOf(record.get(2))
                                    , Room.valueOf(record.get(3)), record.get(4), record.get(5)));
                    addresses.put(address.replace("/2/", "/4/")
                            , new KNXAddress(address, KNXAddress.Type.dimmer_status
                                    , Building.Construction.valueOf(record.get(2))
                                    , Room.valueOf(record.get(3)), record.get(4), record.get(5)));
                }
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ea) {
            LogstashLogger.INSTANCE.error("Syntax error in knx-addresses.txt at line " + lineNumber + " (" + ea.getMessage() + ")");
            throw ea;
        } catch (IOException e) {
            LogstashLogger.INSTANCE.error("Did not open knx-addresses.txt " + e.getMessage());
        }
    }

    public KNXAddress findInString(String in) {
        Matcher matcher = knxAddressPattern.matcher(in);
        if (matcher.find()) {
            if (addresses.get(matcher.group()) != null) {
                return addresses.get(matcher.group());
            } else {
                LogstashLogger.INSTANCE.warn("Address " + matcher.group() + " not found in knx devices list.");
            }
        }
        return null;
    }

    public String replaceReceiverAddress(String in) {
        KNXAddress knx = findInString(in);
        if (knx != null) {
            return "receiver: " + knx.toString() + ", " + in;
        }
        LogstashLogger.INSTANCE.warn("Matcher miss, no address (d/d/d) found in knx event " + in);
        return in;
    }

    public Set<KNXAddress> addressesByRoom(Room room, KNXAddress.Type type) {
        Set<KNXAddress> retVal = new HashSet<>();
        for (KNXAddress address : addresses.values()) {
            if (type == address.type && room == address.room) {
                retVal.add(address);
            }
        }
        return retVal;
    }

    public Set<KNXAddress> addressesByRoom(Room room, String capability) {
        Set<KNXAddress> retVal = new HashSet<>();
        for (KNXAddress address : addresses.values()) {
            if (address.capabilities.contains(capability) && room == address.room) {
                retVal.add(address);
            }
        }
        return retVal;
    }

    public Set<KNXAddress> addressesByType(KNXAddress.Type type) {
        Set<KNXAddress> retVal = new HashSet<>();
        for (KNXAddress address : addresses.values()) {
            if (type == address.type) {
                retVal.add(address);
            }
        }
        return retVal;
    }
}
