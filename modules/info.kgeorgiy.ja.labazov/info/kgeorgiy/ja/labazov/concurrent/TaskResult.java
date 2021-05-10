package info.kgeorgiy.ja.labazov.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TaskResult<T> {
    private final List<T> result;
    private RuntimeException ex;
    private int remains;

    TaskResult(final int remains) {
        this.remains = remains;
        this.result = new ArrayList<>(Collections.nCopies(remains, null));
    }

    synchronized void set(final int index, final T element) {
        result.set(index, element);
        if (--remains == 0) {
            // :NOTE: notifyAll
            notifyAll();
        }
    }

    synchronized public List<T> get() throws InterruptedException {
        while (remains != 0) {
            wait();
        }

        if (ex != null) {
            throw ex;
        }

        return result;
    }

    // :NOTE: Бесконечное ожидание
    synchronized public void setException(final RuntimeException ex) {
        if (this.ex == null) {
            this.ex = ex;
        } else {
            this.ex.addSuppressed(ex);
        }
    }
}
