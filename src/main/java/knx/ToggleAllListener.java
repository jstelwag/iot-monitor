package knx;

import lighting.SwitchLights;
import redis.clients.jedis.Jedis;
import speaker.LogstashLogger;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;

public class ToggleAllListener implements NetworkLinkListener {

    KNXAddressList addressList = new KNXAddressList();

    @Override
    public void confirmation(FrameEvent frameEvent) {}

    @Override
    public void indication(FrameEvent frameEvent) {
        try {
            String event = frameEvent.getFrame().toString();
            KNXAddress knx = addressList.findInString(event);
            if (knx != null && knx.type == KNXAddress.Type.homeserver) {
                int switchCount = SwitchLights.toggleLights(knx.room);
                LogstashLogger.INSTANCE.info("Toggled room " + knx.room + ", switched " + switchCount
                        + " lights.");
            }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Caught unexpected exception, " + e.getMessage());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
    }
}
