package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {
    private <T, R> R parallel(int threads, final List<T> values,
                                    final Function<Stream<T>, R> collector,
                                    final Function<Stream<R>, R> combiner) throws InterruptedException {
        threads = Math.max(1, Math.min(values.size(), threads));

        final List<List<T>> chunks = new ArrayList<>();
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

        return combiner.apply(result.stream());
    }

    private <T> T parallel(int threads, final List<T> values, final Function<Stream<T>, T> collector) throws InterruptedException {
        return parallel(threads, values, collector, collector);
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallel(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(threads, values,
                s -> s.filter(predicate).collect(Collectors.toList()),
                s -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallel(threads, values,
                s -> s.map(f).collect(Collectors.toList()),
                s -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallel(threads, values, s -> s.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallel(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <T> T applyFun(final Stream<T> stream, final Monoid<T> monoid) {
        return stream.reduce(monoid.getIdentity(), monoid.getOperator());
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> function, Monoid<R> monoid) throws InterruptedException {
        return parallel(threads, values,
                s -> applyFun(s.map(function), monoid),
                s -> applyFun(s, monoid));
    }

    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }
}
