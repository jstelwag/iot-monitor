package retriever;

import building.Building;
import control.HeatingControl;
import dao.SetpointDAO;
import dao.TemperatureDAO;
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

        try (SetpointDAO setpoints = new SetpointDAO(); TemperatureDAO temperatures = new TemperatureDAO()) {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();
            for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                try {
                    float value = pc.readFloat(controllableRoom.temperatureSensor, false);
                    temperatures.set(controllableRoom, value);
                    countT++;
                } catch (KNXTimeoutException e) {
                    System.out.println("Timeout retrieving " + controllableRoom + " temperature");
                }
                if (controllableRoom.setpoint != null) {
                    try {
                        setpoints.setKnx(controllableRoom, pc.readFloat(controllableRoom.setpoint, false));
                        countSP++;
                    } catch (KNXTimeoutException e) {
                        LogstashLogger.INSTANCE.message("Timeout retrieving " + controllableRoom + " setpoint");
                        System.out.println("Timeout retrieving " + controllableRoom + " setpoint");
                    }
                }
            }
        } catch (IOException | KNXException | InterruptedException e) {
            System.out.println("error " + e);
            e.printStackTrace();
            HeatingControl.INSTANCE.knxLink.close();
        }

        System.out.println("Retrieved (knx) " + countT + " room temperatures and " + countSP + " setpoints");
    }
}
