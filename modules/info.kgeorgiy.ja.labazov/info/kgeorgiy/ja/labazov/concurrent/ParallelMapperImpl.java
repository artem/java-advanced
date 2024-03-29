package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final Queue<Runnable> queue = new ArrayDeque<>();

    public ParallelMapperImpl(final int threads) {
        // :NOTE: :(
        workers = IntStream.range(0, threads).mapToObj(i -> new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    getTask().run();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        })).collect(Collectors.toList());
        workers.forEach(Thread::start);
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

        IntStream.range(0, args.size()).forEach(pos -> addTask(() -> {
            try {
                result.set(pos, f.apply(args.get(pos)));
            } catch (final RuntimeException ex) {
                result.setException(ex);
            }
        }));

        return result.get();
    }

    private void addTask(final Runnable task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    // :NOTE: "подвисшие" клиенты
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
