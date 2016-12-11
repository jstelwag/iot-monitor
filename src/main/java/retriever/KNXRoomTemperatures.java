package retriever;

import building.Building;
import control.HeatingControl;
import dao.SetpointDAO;
import org.apache.commons.io.IOUtils;
import speaker.LogstashLogger;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
import tuwien.auto.calimero.process.ProcessCommunicator;

import java.io.IOException;

public class KNXRoomTemperatures implements Runnable {

    public KNXRoomTemperatures() {}

    @Override
    public void run() {
        int countT = 0;
        int countSP = 0;

        SetpointDAO dao = new SetpointDAO();
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                try {
                    float value = pc.readFloat(controllableRoom.temperatureSensor, false);
                    HeatingControl.INSTANCE.addRoomTemperature(controllableRoom, value);
                    countT++;
                } catch (KNXTimeoutException e) {
                    System.out.println("Timeout retrieving " + controllableRoom + " temperature");
                }
                if (controllableRoom.setpoint != null) {
                    try {
                        dao.setKnx(controllableRoom, pc.readFloat(controllableRoom.setpoint, false));
                        countSP++;
                    } catch (KNXTimeoutException e) {
                        LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableRoom + " setpoint");
                        System.out.println("Timeout retrieving " + controllableRoom + " setpoint");
                    }
                }
            }
        } catch (KNXException | InterruptedException e) {
            System.out.println("error " + e);
            e.printStackTrace();
            HeatingControl.INSTANCE.knxLink.close();
        } finally {
            IOUtils.closeQuietly(dao);
        }

        System.out.println("Retrieved (knx) " + countT + " room temperatures and " + countSP + " setpoints");
    }
}
