package knx;

public interface EventHandler {
    public void onEvent(String event, KNXAddress knx);
}
