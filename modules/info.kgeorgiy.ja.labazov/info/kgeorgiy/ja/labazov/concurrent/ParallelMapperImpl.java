package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final Queue<Runnable> queue = new ArrayDeque<>();

    public ParallelMapperImpl(int threads) {
        workers = new ArrayList<>(threads);

        for (int i = 0; i < threads; i++) {
            Thread th = new Thread(() -> {
                try {
                    handleRequest();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            ParallelUtils.addAndStartThread(th, workers);
        }

    }

    private void handleRequest() throws InterruptedException {
        while (!Thread.interrupted()) {
            Runnable got;
            synchronized (queue) {
                while (queue.isEmpty()) {
                    queue.wait();
                }
                got = queue.poll();
            }
            got.run();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        TaskResult<R> res = new TaskResult<>(args.size());

        for (int i = 0; i < args.size(); i++) {
            final int pos = i;
            Runnable task = () -> res.set(pos, f.apply(args.get(pos)));
            synchronized (queue) {
                queue.add(task);
                queue.notify();
            }
        }

        return res.get();
    }

    @Override
    public void close() {
        for (Thread worker : workers) {
            worker.interrupt();
        }

        for (int i = 0; i < workers.size(); ) {
            try {
                workers.get(i).join();
                i++;
            } catch (InterruptedException ignored) {
            }
        }
    }
}
