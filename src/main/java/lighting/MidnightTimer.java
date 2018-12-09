package lighting;

import knx.KNXLink;
import speaker.LogstashLogger;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

import java.util.Date;

public class MidnightTimer extends Runnable {


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
