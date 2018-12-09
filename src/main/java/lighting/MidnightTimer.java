package lighting;

import knx.KNXLink;
import speaker.LogstashLogger;

import java.util.Date;
import java.util.TimerTask;

public class MidnightTimer extends TimerTask {


    public Date midnight() {
        Long time = new Date().getTime();
        return new Date(time - time % (24 * 60 * 60 * 1000));
    }
    @Override
    public void run() {
        LogstashLogger.INSTANCE.info("Switching midnight - lights off");

        try {
            //3/0/106	button	garden	yet_unknown	koetshuis, buitenlamp lindeboom
            KNXLink.getInstance().writeBoolean(new GroupAddress("3/0/106"), false);
            Thread.sleep(500);
        } catch (KNXException | InterruptedException e) {
            LogstashLogger.INSTANCE.error("Midnight time knx swithching problem " + e.getMessage());
        }

    }
}
