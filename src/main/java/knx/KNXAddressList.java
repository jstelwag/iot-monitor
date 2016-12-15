package knx;

import building.Building;
import building.Room;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import speaker.LogstashLogger;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaap on 15-12-2016.
 */
public class KNXAddressList {

    public List<KNXAddress> addresses = new ArrayList<>();

    public KNXAddressList() {
        ClassLoader classLoader = getClass().getClassLoader();
        Reader in = null;
        try {
            in = new FileReader(classLoader.getResource("knx-addresses.txt").getFile());
            Iterable<CSVRecord> records = CSVFormat.TDF.parse(in);
            for (CSVRecord record : records) {
                addresses.add(new KNXAddress(record.get(0), KNXAddress.Type.valueOf(record.get(1))
                        , Building.Construction.valueOf(record.get(2))
                        , Room.valueOf(record.get(3)), record.get(4)));
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
        for (KNXAddress address : addresses) {
            if (type == address.type && room == address.room) {
                retVal.add(address);
            }
        }
        return retVal;
    }
}
