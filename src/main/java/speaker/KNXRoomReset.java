package speaker;

import building.ControllableArea;
import control.HeatingControl;
import dao.BookingDAO;
import dao.SetpointDAO;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessCommunicator;

import java.io.IOException;

/**
 * Created by Jaap on 30-1-2016.
 */
public class KNXRoomReset implements Runnable {

    public KNXRoomReset() {}

    @Override
    public void run() {

        int setpointCount = 0, allOffCount = 0;
        try (BookingDAO bookings = new BookingDAO(); SetpointDAO dao = new SetpointDAO()) {
            ProcessCommunicator pc = HeatingControl.INSTANCE.knxLink.pc();

            if (HeatingControl.INSTANCE.hasUpdatedBookings) {
                for (ControllableArea controllableArea : ControllableArea.values()) {
                    if (!bookings.isOccupiedNow(controllableArea.room)) {
                        if (controllableArea.setpoint != null) {
                            //pc.write(controllableArea.setpoint, (float) DefaultSetpoint.populate().get(controllableArea).setpoint, false);
                            setpointCount++;
                            System.out.println("-- " + controllableArea);
                        } else System.out.println("++ " + controllableArea);
                    }
                    if (!dao.isActive(controllableArea)) {
                        if (controllableArea.allOffButton != null) {
                            pc.write(controllableArea.allOffButton, false);
                            allOffCount++;
                        }
                    }
                }
            }
            System.out.println("I have reset " + setpointCount + " setpoints and " + allOffCount + " rooms via knx");
        } catch (KNXException | InterruptedException | IOException e) {
            System.out.println("error " + e);
            e.printStackTrace();
            HeatingControl.INSTANCE.knxLink.close();
        }
    }
}
