package info.kgeorgiy.ja.labazov.concurrent;

import java.util.List;

public class ParallelUtils {
    public static void addAndStartThread(Thread th, List<Thread> list) {
        list.add(th);
        th.start();
    }
}
