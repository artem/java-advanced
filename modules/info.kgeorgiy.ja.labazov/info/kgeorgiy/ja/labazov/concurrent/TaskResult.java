package info.kgeorgiy.ja.labazov.concurrent;

import java.util.ArrayList;
import java.util.List;

public class TaskResult<T> {
    private final List<T> result;
    private int remains;

    TaskResult(final int remains) {
        this.remains = remains;
        this.result = new ArrayList<>(remains);

        // :NOTE: :(
        for (int i = 0; i < remains; i++) {
            result.add(null);
        }
    }

    synchronized void set(final int index, final T element) {
        result.set(index, element);
        if (--remains == 0) {
            notifyAll();
        }
    }

    synchronized public List<T> get() throws InterruptedException {
        while (remains != 0) {
            wait();
        }

        return result;
    }
}
