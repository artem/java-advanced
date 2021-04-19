package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper parallelMapper;
    public IterativeParallelism() {

        parallelMapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        parallelMapper = mapper;
    }

    private <T, R, U> R parallel(
            int threads,
            final List<T> values,
            final Function<Stream<T>, U> collector,
            final Function<Stream<U>, R> combiner
    ) throws InterruptedException {
        threads = Math.max(1, Math.min(values.size(), threads));

        final List<Stream<T>> chunks = chunks(threads, values);
        final List<U> result;

        if (parallelMapper != null) {
            result = parallelMapper.map(collector, chunks);
        } else {
            result = iterativeMapper(threads, collector, chunks);
        }

        return combiner.apply(result.stream());
    }

    private <T, R> List<R> iterativeMapper(int threads, Function<Stream<T>, R> collector, List<Stream<T>> chunks) throws InterruptedException {
        final List<R> result = new ArrayList<>(threads);
        final List<Thread> workers = new ArrayList<>(threads);

        for (int i = 0; i < threads; i++) {
            result.add(null);
        }

        for (int i = 0; i < threads; ++i) {
            final int pos = i;
            final Thread thread = new Thread(() -> result.set(pos, collector.apply(chunks.get(pos))));
            workers.add(thread);
            thread.start();
        }

        /**
         * {@link URLClassLoader#close()}
         */
        final List<InterruptedException> errors = new LinkedList<>();

        for (final Thread thread : workers) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                errors.add(e);
                for (final Thread th : workers) {
                    th.interrupt();
                }
                break;
            }
        }

        if (!errors.isEmpty()) {
            final InterruptedException firstex = errors.remove(0);

            for (final InterruptedException error : errors) {
                firstex.addSuppressed(error);
            }

            throw firstex;
        }

        return result;
    }

    private <T> List<Stream<T>> chunks(final int threads, final List<T> values) {
        final List<Stream<T>> chunks = new ArrayList<>();
        final int chunkSize = values.size() / threads;
        int extra = values.size() % threads;

        for (int i = 0, start = 0; i < threads; ++i) {
            int offset = chunkSize;
            if (extra != 0) {
                offset++;
                extra--;
            }

            chunks.add(values.subList(start, start += offset).stream());
        }
        return chunks;
    }

    private <T> T parallel(final int threads, final List<T> values, final Function<Stream<T>, T> collector) throws InterruptedException {
        return parallel(threads, values, collector, collector);
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return parallel(threads, values,
                s -> s.map(Objects::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    private <T, U> List<U> streamOperation(final int threads,
                                           final List<T> values,
                                           final Function<Stream<T>, Stream<? extends U>> collector
    ) throws InterruptedException {
        return this.parallel(threads, values, collector, s -> s.flatMap(Function.identity()).collect(Collectors.toList()));
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return this.streamOperation(threads, values, s -> s.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return streamOperation(threads, values, s -> s.map(f));
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
