package speaker;

import building.Building;
import control.DefaultSetpoint;
import control.HeatingControl;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessCommunicator;

/**
 * Created by Jaap on 30-1-2016.
 */
public class KNXRoomReset implements Runnable {

    public KNXRoomReset() {}

    @Override
    public void run() {

        int setpointCount = 0, allOffCount = 0;
        try {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();

            if (HeatingControl.INSTANCE.hasUpdatedBookings) {
                for (Building.ControllableRoom controllableRoom : Building.ControllableRoom.values()) {
                    if (!HeatingControl.INSTANCE.occupiedNow.containsKey(controllableRoom)) {
                        if (controllableRoom.setpoint != null) {
                            pc.write(controllableRoom.setpoint, (float) DefaultSetpoint.populate().get(controllableRoom).setpoint, false);
                            setpointCount++;
                            System.out.println("-- " + controllableRoom);
                        } else System.out.println("++ " + controllableRoom);
                    }
                    if (!HeatingControl.INSTANCE.setpoints.get(controllableRoom).isActive) {
                        if (controllableRoom.allOffButton != null) {
                            pc.write(controllableRoom.allOffButton, false);
                            allOffCount++;
                        }
                    }
                }
            }
            System.out.println("I have reset " + setpointCount + " setpoints and " + allOffCount + " rooms via knx");
        } catch (KNXException | InterruptedException e) {
            System.out.println("error " + e);
            e.printStackTrace();
            HeatingControl.INSTANCE.knxLink.close();
        }
    }
}
