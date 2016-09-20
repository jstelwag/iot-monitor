package state;

import java.util.Date;

public class TemperatureState {

    public final Date created;
    public final double temperature;

    public TemperatureState(double temperature) {
        created = new Date();
        this.temperature = temperature;
    }
}
