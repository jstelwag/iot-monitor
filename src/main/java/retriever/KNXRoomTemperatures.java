package retriever;

import building.Building;
import control.HeatingControl;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
import tuwien.auto.calimero.process.ProcessCommunicator;

public class KNXRoomTemperatures implements Runnable {

    public KNXRoomTemperatures() {}

    @Override
    public void run() {
        int countT = 0;
        int countSP = 0;

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
                        HeatingControl.INSTANCE.setpoints.get(controllableRoom).setpoint
                                = pc.readFloat(controllableRoom.setpoint, false);
                        countSP++;
                    } catch (KNXTimeoutException e) {
                        System.out.println("Timeout retrieving " + controllableRoom + " setpoint");
                    }
                }
            }
        } catch (KNXException | InterruptedException e) {
            System.out.println("error " + e);
            e.printStackTrace();
            HeatingControl.INSTANCE.knxLink.close();
        }

        System.out.println("Retrieved (knx) " + countT + " room temperatures and " + countSP + " setpoints");
    }
}
