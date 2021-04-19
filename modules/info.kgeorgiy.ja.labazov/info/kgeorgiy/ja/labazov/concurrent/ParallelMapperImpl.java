package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final int threads;

    private final List<Thread> workers;
    private final Queue<Thread> queue = new ArrayDeque<>();

    public ParallelMapperImpl(int threads) {
        this.threads = threads;
        workers = new ArrayList<>(threads);

        for (int i = 0; i < threads; i++) {
            Thread th = new Thread(
                    () -> {
                        try {
                            handleRequest();
                        } catch (InterruptedException e) {
                            e.printStackTrace(); //TODO
                        }
                    }
            );
            workers.add(th);
            th.start();
        }

    }

    private void handleRequest() throws InterruptedException {
        while (!Thread.interrupted()) {
            Thread got;
            synchronized (queue) {
                while (queue.isEmpty()) {
                    queue.wait();
                }
                got = queue.poll();
            }
            got.start();
            got.join();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> ans = new ArrayList<>();

        for (T arg : args) {
            ans.add(f.apply(arg));
        }

        ans.wait();
        return ans;
    }

    @Override
    public void close() {

    }
}
