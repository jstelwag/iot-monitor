package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;

/**
 * Simply log all KNX bus messages to Elastic
 */
public class KNXEventListener implements NetworkLinkListener {

    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        KNXLink.getInstance().eventHandler(frameEvent.getFrame().toString());
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.info("Eventlistener closing");
    }

}
