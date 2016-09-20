package util;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Concurrent Deque implementation that starts eating from the Deque once it exceeds the limit
 * @param <E>
 */
public class FIFODeque<E> extends ConcurrentLinkedDeque<E> {

    private final int limit;

    public FIFODeque(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        while (size() >= limit) super.removeFirst();
        return super.add(o);
    }
}
