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

    public ParallelMapperImpl(final int threads) {
        workers = new ArrayList<>(threads);

        // :NOTE: Stream
        for (int i = 0; i < threads; i++) {
            final Thread th = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        getTask().run();
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            ParallelUtils.addAndStartThread(th, workers);
        }

    }

    private Runnable getTask() throws InterruptedException {
        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
            return queue.poll();
        }
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {
        final TaskResult<R> result = new TaskResult<>(args.size());

        // :NOTE: IntStream
        for (int i = 0; i < args.size(); i++) {
            final int pos = i;
            // :NOTE: Обработка ошибок
            final Runnable task = () -> result.set(pos, f.apply(args.get(pos)));
            synchronized (queue) {
                queue.add(task);
                queue.notify();
            }
        }

        return result.get();
    }

    @Override
    public void close() {
        for (final Thread worker : workers) {
            worker.interrupt();
        }

        for (int i = 0; i < workers.size(); ) {
            try {
                workers.get(i).join();
                i++;
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
