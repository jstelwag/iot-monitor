package knx;

import building.Building;
import building.Room;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import speaker.LogstashLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jaap on 15-12-2016.
 */
public class KNXAddressList {

    public Map<String, KNXAddress> addresses = new HashMap<>();

    public KNXAddressList() {
        ClassLoader classLoader = getClass().getClassLoader();
        Reader in = null;
        try {
            in = new InputStreamReader(classLoader.getResourceAsStream("knx-addresses.txt"));
            Iterable<CSVRecord> records = CSVFormat.TDF.parse(in);
            for (CSVRecord record : records) {
                if (addresses.put(record.get(0)
                        , new KNXAddress(record.get(0), KNXAddress.Type.valueOf(record.get(1))
                        , Building.Construction.valueOf(record.get(2))
                        , Room.valueOf(record.get(3)), record.get(4))) != null) {
                    LogstashLogger.INSTANCE.message("ERROR: duplicate address in knx-addresses.txt " + record.get(0));
                }
            }
        } catch (IOException e) {
            System.out.println("Did not open knx-addresses.txt " + e.getMessage());
            LogstashLogger.INSTANCE.message("ERROR: did not open knx-addresses.txt " + e.getMessage() );
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public List<KNXAddress> addressesByRoom(Room room, KNXAddress.Type type) {
        List<KNXAddress> retVal = new ArrayList<>();
        for (KNXAddress address : addresses.values()) {
            if (type == address.type && room == address.room) {
                retVal.add(address);
            }
        }
        return retVal;
    }
}
