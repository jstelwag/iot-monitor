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
        if (!((CEMILData) frameEvent.getFrame()).getDestination().toString().startsWith("0")) {
            LogstashLogger.INSTANCE.message("knx-event", replaceReceiverAddress(frameEvent.getFrame().toString()));
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {}

    private String replaceReceiverAddress(String in) {
        Pattern pattern = Pattern.compile("\\d{1,3}" + Pattern.quote("/") + "\\d{1,3}" + Pattern.quote("/") + "\\d{1,3}");
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            KNXAddressList address = new KNXAddressList();
            return "receiver: [" + address.addresses.get(matcher.group(1)) + "] " + in;
        }
        return in;
    }
}
