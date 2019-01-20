package lighting;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;
import speaker.LogstashLogger;
import util.HeatingProperties;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Sun {

    private final HeatingProperties prop;
    private final double DUSK_ZENITH = 85.0;
    private final double DAWN_ZENITH = 92.0;

    private final double ZENITH_THRESHOLD = 5.0;

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
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 12) {
            AzimuthZenithAngle position = position();
            boolean retVal = position.getZenithAngle() > (DUSK_ZENITH + bias)
                    && position.getZenithAngle() < (DUSK_ZENITH + ZENITH_THRESHOLD + bias);
            LogstashLogger.INSTANCE.info("Dusk check, bias: " + bias + ", zenith: " + position.getZenithAngle()
                    + ", result: " + retVal);
            return retVal;
        }
        return false;
    }

    public boolean dawn(double bias) {
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12) {
            AzimuthZenithAngle position = position();
            boolean retVal = position.getZenithAngle() < (DAWN_ZENITH + bias)
                    && position.getZenithAngle() > (DAWN_ZENITH - ZENITH_THRESHOLD + bias);
            LogstashLogger.INSTANCE.info("Dawn check, bias: " + bias + ", zenith: " + position.getZenithAngle()
                    + ", result: " + retVal);
            return retVal;
        }
        return false;
    }

    public boolean down(double bias) {
        AzimuthZenithAngle position = position();
        return position.getZenithAngle() > (DAWN_ZENITH + bias);
    }
}
