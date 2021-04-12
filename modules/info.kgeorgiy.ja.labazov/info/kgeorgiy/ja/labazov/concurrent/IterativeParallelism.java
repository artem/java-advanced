package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    private <T, R> List<R> parallel(int threads, final List<? extends T> values,
                                    final Function<? super Stream<? extends T>, ? extends R> collector) throws InterruptedException {
        threads = Math.max(1, Math.min(values.size(), threads));

        final List<List<? extends T>> chunks = new ArrayList<>();
        final int chunkSize = values.size() / threads;
        int extra = values.size() % threads;

        for (int i = 0, start = 0; i < threads; ++i) {
            int shift = chunkSize;
            if (extra != 0) {
                shift++;
                extra--;
            }

            chunks.add(values.subList(start, start += shift));
        }

        List<R> result = new ArrayList<>(threads);
        List<Thread> workers = new ArrayList<>(threads);

        for (int i = 0; i < threads; ++i) {
            final int pos = i;
            result.add(null);
            Thread thread = new Thread(() -> result.set(pos, collector.apply(chunks.get(pos).stream())));
            workers.add(thread);
            thread.start();
        }

        List<InterruptedException> errors = new LinkedList<>(); // As in JDK

        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            InterruptedException firstex = errors.remove(0);

            for (InterruptedException error : errors) {
                firstex.addSuppressed(error);
            }

            throw firstex;
        }

        return result;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        final Function<Stream<?>, Stream<String>> pred = stream -> stream.map(Object::toString);

        final List<Stream<String>> res = parallel(threads, values, pred);

        return res.stream().flatMap(s -> s).collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        final Function<Stream<? extends T>, Stream<? extends T>> pred = stream -> stream.filter(predicate);

        final List<Stream<? extends T>> res = parallel(threads, values, pred);


        return res.stream().flatMap(s -> s).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        final Function<Stream<? extends T>, Stream<U>> map = stream -> stream.map(f);

        final List<Stream<U>> res = parallel(threads, values, map);

        return res.stream().flatMap(s -> s).collect(Collectors.toList());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        final Function<Stream<? extends T>, ? extends T> maxx = stream -> stream.max(comparator).orElse(null);

        final List<T> res = parallel(threads, values, maxx);

        return maxx.apply(res.stream());
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        final Function<Stream<? extends T>, Boolean> all = stream -> stream.allMatch(predicate);

        final List<Boolean> res = parallel(threads, values, all);

        return res.stream().allMatch(e -> e);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        final Function<Stream<? extends T>, Boolean> any = stream -> stream.anyMatch(predicate);

        final List<Boolean> res = parallel(threads, values, any);

        return res.stream().anyMatch(e -> e);
    }
}
