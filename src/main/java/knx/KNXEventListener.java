package knx;

import speaker.LogstashTimedSpeaker;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.link.NetworkLinkListener;

/**
 * Created by Jaap on 8-2-2016.
 */
public class KNXEventListener implements NetworkLinkListener {
    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        if (!((CEMILData) frameEvent.getFrame()).getDestination().toString().startsWith("0")) {
            LogstashTimedSpeaker.INSTANCE.message(frameEvent.getFrame().toString());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {}
}
