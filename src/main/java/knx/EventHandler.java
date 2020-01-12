package knx;

public interface EventHandler extends Runnable {
    public EventHandler onEvent(String event, KNXAddress knx);
}
