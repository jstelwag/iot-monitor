package knx;

import speaker.LogstashLogger;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.link.NetworkLinkListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simply log all KNX bus messages to Elastic
 */
public class KNXEventListener implements NetworkLinkListener {
    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        LogstashLogger.INSTANCE.message("knx-event", ((CEMILData) frameEvent.getFrame()).getDestination().toString()
                + " " + frameEvent.getFrame().toString());
        if (!((CEMILData) frameEvent.getFrame()).getDestination().toString().startsWith("0")) {
            LogstashLogger.INSTANCE.message("knx-event", replaceReceiverAddress(frameEvent.getFrame().toString()));
        } else {
            LogstashLogger.INSTANCE.message("knx-p1", ((CEMILData) frameEvent.getFrame()).getDestination().toString()
                    + " " + frameEvent.getFrame().toString());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
        LogstashLogger.INSTANCE.message("INFO: KNXLink closing");
    }

    private String replaceReceiverAddress(String in) {
        Pattern pattern = Pattern.compile("\\d{1,3}" + Pattern.quote("/") + "\\d{1,3}" + Pattern.quote("/") + "\\d{1,3}");
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            KNXAddressList address = new KNXAddressList();
            return "receiver: " + address.addresses.get(matcher.group(1)) + ", " + in;
        } else {
            LogstashLogger.INSTANCE.message("WARNING: matcher miss, no address (d/d/d) found in knx event " + in);
        }
        return in;
    }
}
