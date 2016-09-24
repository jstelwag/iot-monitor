package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.link.NetworkLinkListener;

/**
 * Simply log all KNX bus messages to Elastic
 */
public class KNXEventListener implements NetworkLinkListener {
    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        if (!((CEMILData) frameEvent.getFrame()).getDestination().toString().startsWith("0")) {
            LogstashLogger.INSTANCE.message("knx-event", frameEvent.getFrame().toString());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {}
}
