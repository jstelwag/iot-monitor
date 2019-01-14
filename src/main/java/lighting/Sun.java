package lighting;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;
import speaker.LogstashLogger;
import util.HeatingProperties;

import java.util.GregorianCalendar;

public class Sun {

    private final HeatingProperties prop;
    private final double DUSK_ZENITH = 85.0;
    private final double DAWN_ZENTIH = 90.0;

    private final double ZENTIH_THRESHOLD = 5.0;

    public Sun() {
        prop = new HeatingProperties();
    }

    public AzimuthZenithAngle position() {
        final GregorianCalendar dateTime = new GregorianCalendar();
        AzimuthZenithAngle position = SPA.calculateSolarPosition(
                dateTime,
                prop.latitude, prop.longitude, prop.elevation,
                DeltaT.estimate(dateTime),
                1010, // avg. air pressure (hPa)
                11); // avg. air temperature (°C)
        return position;
    }

    public boolean dusk(double bias) {
        AzimuthZenithAngle position = position();
        LogstashLogger.INSTANCE.info("Test zenith " + position.getZenithAngle() + " / "
                + (position.getZenithAngle() < (DUSK_ZENITH + bias)
                && position.getZenithAngle() > (DUSK_ZENITH - ZENTIH_THRESHOLD + bias)));
        return position.getZenithAngle() < (DUSK_ZENITH + bias)
                && position.getZenithAngle() > (DUSK_ZENITH - ZENTIH_THRESHOLD + bias);
    }

    public boolean dawn(double bias) {
        AzimuthZenithAngle position = position();
        return position.getZenithAngle() > (DAWN_ZENTIH + bias)
                && position.getZenithAngle() < (DAWN_ZENTIH + ZENTIH_THRESHOLD + bias);
    }

    public boolean down(double bias) {
        AzimuthZenithAngle position = position();
        return position.getZenithAngle() > (DAWN_ZENTIH + bias);
    }
}
