package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {
    public IterativeParallelism() {

    }
    public IterativeParallelism(ParallelMapper mapper) {

    }
    private <T, R> R parallel(
            int threads,
            final List<T> values,
            final Function<Stream<T>, R> collector,
            final Function<Stream<R>, R> combiner
    ) throws InterruptedException {
        threads = Math.max(1, Math.min(values.size(), threads));

        final List<List<T>> chunks = chunks(threads, values);
        final List<R> result = new ArrayList<>(threads);
        // :NOTE: Массив?
        final Thread[] workers = new Thread[threads];

        for (int i = 0; i < threads; ++i) {
            final int pos = i;
            // :NOTE: Чтение-запись
            result.add(null);
            final Thread thread = new Thread(() -> result.set(pos, collector.apply(chunks.get(pos).stream())));
            workers[i] = thread;
            thread.start();
        }

        final List<InterruptedException> errors = new LinkedList<>(); // As in JDK

        // :NOTE: Окончание работы
        for (final Thread thread : workers) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            final InterruptedException firstex = errors.remove(0);

            for (final InterruptedException error : errors) {
                firstex.addSuppressed(error);
            }

            throw firstex;
        }

        return combiner.apply(result.stream());
    }

    private <T> List<List<T>> chunks(final int threads, final List<T> values) {
        final List<List<T>> chunks = new ArrayList<>();
        final int chunkSize = values.size() / threads;
        int extra = values.size() % threads;

        for (int i = 0, start = 0; i < threads; ++i) {
            int offset = chunkSize;
            if (extra != 0) {
                offset++;
                extra--;
            }

            chunks.add(values.subList(start, start += offset));
        }
        return chunks;
    }

    private <T> T parallel(final int threads, final List<T> values, final Function<Stream<T>, T> collector) throws InterruptedException {
        return parallel(threads, values, collector, collector);
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return parallel(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        // :NOTE: Дубль
        return parallel(threads, values,
                s -> s.filter(predicate).collect(Collectors.toList()),
                s -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return parallel(threads, values,
                s -> s.map(f).collect(Collectors.toList()),
                s -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return parallel(threads, values, s -> s.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallel(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private static <T> T applyFun(final Stream<T> stream, final Monoid<T> monoid) {
        return stream.reduce(monoid.getIdentity(), monoid.getOperator());
    }

    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> function, final Monoid<R> monoid) throws InterruptedException {
        return parallel(threads, values,
                s -> applyFun(s.map(function), monoid),
                s -> applyFun(s, monoid));
    }

    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }
}
