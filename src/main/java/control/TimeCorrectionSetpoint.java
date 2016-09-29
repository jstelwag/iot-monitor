package control;

import java.util.Calendar;

/**
 * Created by Jaap on 29-9-2016.
 */
public class TimeCorrectionSetpoint {

    public static double correct(double in) {
        switch (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            case 22:
                return in - 0.5;
            case 23:
                return in - 1.0;
            case 0:
                return in - 3.0;
            case 1:
                return in - 3.0;
            case 2:
                return in - 3.0;
            case 3:
                return in - 3.0;
            case 4:
                return in - 3.0;
            case 5:
                return in - 2.0;
            case 6:
                return in - 1.0;
            case 7:
                return in - 0.5;
            default:
                return in;
        }
    }
}
