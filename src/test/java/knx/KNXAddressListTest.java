package knx;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jaap on 12-8-2017.
 */
public class KNXAddressListTest {
    @Test
    public void replaceReceiverAddress() {
        KNXAddressList list = new KNXAddressList();
        Assert.assertEquals("Address is recognized"
                , list.replaceReceiverAddress("1.0.81->0/2/154 L_Data.ind, low priority hop count 6, tpdu 00 80 00 00 02 6")
                , "receiver: 0/2/154: yet_unknown (P1) 'actueel verbruik L3 (W)', 1.0.81->0/2/154 L_Data.ind, low priority hop count 6, tpdu 00 80 00 00 02 6");

    }
}
