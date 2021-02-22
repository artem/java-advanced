package info.kgeorgiy.ja.labazov.arrayset;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

public class ReversedListView<E> extends AbstractList<E> {
    protected final List<E> inner;
    private ReversedListView(List<E> list) {
        inner = list;
    }

    public static <T> List<T> reverse(List<T> list) {
        if (list == null) {
            return null;
        }

        if (list instanceof ReversedListView) {
            return ((ReversedListView<T>) list).inner;
        } else if (list instanceof RandomAccess) {
            return new IndexedReversedListView<>(list);
        } else {
            return new ReversedListView<>(list);
        }
    }

    protected int reverseIdx(int idx) {
        return inner.size() - idx - 1;
    }

    protected List<E> extractSubList(int fromIndex, int toIndex) {
        return inner.subList(reverseIdx(toIndex - 1), reverseIdx(fromIndex) + 1);
    }

    @Override
    public E get(int i) {
        return inner.get(reverseIdx(i));
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new ReversedListView<>(extractSubList(fromIndex, toIndex));
    }

    private static class IndexedReversedListView<E> extends ReversedListView<E> implements RandomAccess {
        public IndexedReversedListView(List<E> list) {
            super(list);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return new IndexedReversedListView<>(extractSubList(fromIndex, toIndex));
        }
    }
}
