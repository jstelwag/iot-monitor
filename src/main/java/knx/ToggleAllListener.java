package knx;

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
            if (knx != null) {
                if (knx.type == KNXAddress.Type.homeserver) {
                    Jedis jedis = new Jedis("localhost");

                    boolean desiredState = !"ON".equals(jedis.get(knx.room + ".all.state"));
                    jedis.set(knx.room + ".all.state", desiredState ? "ON" : "OFF");
                    int switchCount = 0;
                    //for (KNXAddress address : addressList.addressesByRoom(knx.room, KNXAddress.Type.button)) {
                    //    try {
                    //        KNXLink.getInstance().writeBoolean(new GroupAddress(address.address), desiredState);
                            switchCount++;
                            Thread.sleep(50); //Wait a little to reduce LED switching peaks
                    //    } catch (KNXException | InterruptedException e) {
                    //        LogstashLogger.INSTANCE.error("Failed to toggle room " + address + ", " + e.getMessage());
                    //    }
                    }
                    LogstashLogger.INSTANCE.info("Toggled room " + knx.room + ", switched " + switchCount
                            + " lights " + (desiredState ? "on" : "off"));
                }
            }
        } catch (Exception e) {
            LogstashLogger.INSTANCE.error("Caught unexpected exception, " + e.getMessage());
        }
    }

    @Override
    public void linkClosed(CloseEvent closeEvent) {
    }
}
