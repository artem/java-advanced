package info.kgeorgiy.ja.labazov.arrayset;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

public class ReversedListView<E> extends AbstractList<E> implements RandomAccess {
    private final List<E> inner;
    public ReversedListView(List<E> list) {
        inner = list;
    }

    public static <T> List<T> reverse(List<T> list) {
        if (list instanceof ReversedListView) {
            return ((ReversedListView<T>) list).inner;
        } else {
            return new ReversedListView<>(list);
        }
    }

    @Override
    public E get(int i) {
        return inner.get(size() - i - 1);
    }

    @Override
    public int size() {
        return inner.size();
    }
}
