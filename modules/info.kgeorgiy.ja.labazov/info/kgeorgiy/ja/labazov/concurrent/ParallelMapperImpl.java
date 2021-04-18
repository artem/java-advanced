package info.kgeorgiy.ja.labazov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    public ParallelMapperImpl(int threads) {

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
