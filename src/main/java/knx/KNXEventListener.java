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

    KNXAddressList addressList = new KNXAddressList();
    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        LogstashLogger.INSTANCE.message("knx-event", frameEvent.getFrame().toString());
/*
        LogstashLogger.INSTANCE.message("knx-event", ((CEMILData) frameEvent.getFrame()).getDestination().toString()
                + " " + frameEvent.getFrame().toString());
        if (!((CEMILData) frameEvent.getFrame()).getDestination().toString().startsWith("0")) {
            LogstashLogger.INSTANCE.message("knx-event", addressList.replaceReceiverAddress(frameEvent.getFrame().toString()));
        } else {
            LogstashLogger.INSTANCE.message("knx-p1", ((CEMILData) frameEvent.getFrame()).getDestination().toString()
                    + " " + frameEvent.getFrame().toString());
        }
*/
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.message("INFO: KNXLink closing");
    }

}
