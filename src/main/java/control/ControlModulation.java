package control;

import java.util.Date;

/**
 * Created by Jaap on 30-9-2016.
 */
public class ControlModulation {
    public boolean currentState = false;
    public Date lastStateChange = new Date();

    public boolean control(int level) {
        if (level < 2) {
            if (currentState) {
                currentState = false;
                lastStateChange = new Date();
            }
        } else if (level < 10) {
            // The calculation is a balanced on and off period. Low number of valves opened: short on, long off
            long expireTime;
            if (currentState) {
                expireTime = lastStateChange.getTime() + 6 * level * 60 * 1000;
            } else {
                expireTime = lastStateChange.getTime() + 6 * (10 - level) * 60 * 1000;
            }
            if (new Date().getTime() > expireTime) {
                currentState = !currentState;
                lastStateChange = new Date();
            }
        } else {
            // Only full on here
            if (!currentState) {
                currentState = true;
                lastStateChange = new Date();
            }
        }
        return currentState;
    }
}
