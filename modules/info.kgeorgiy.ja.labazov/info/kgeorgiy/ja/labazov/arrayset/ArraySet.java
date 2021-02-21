package info.kgeorgiy.ja.labazov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final Comparator<? super E> comparator;
    private final ArrayList<E> array;

    public ArraySet() {
        this.comparator = null;
        array = new ArrayList<>(0);
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, null);
    }

    public ArraySet(Collection<? extends E> c, Comparator<? super E> comparator) {
        this.comparator = comparator;
        array = new ArrayList<>(c);
        array.sort(comparator);
    }

    @Override
    public E lower(E e) {
        return null;
    }

    @Override
    public E floor(E e) {
        return null;
    }

    @Override
    public E ceiling(E e) {
        return null;
    }

    @Override
    public E higher(E e) {
        return null;
    }

    @Override
    public E pollFirst() {
        return null;
    }

    @Override
    public E pollLast() {
        return null;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return null;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return null;
    }

    @Override
    public NavigableSet<E> subSet(E e, boolean b, E e1, boolean b1) {
        return null;
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b) {
        return null;
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b) {
        return null;
    }

    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public SortedSet<E> subSet(E e, E e1) {
        return null;
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return null;
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return null;
    }

    @Override
    public E first() {
        return null;
    }

    @Override
    public E last() {
        return null;
    }

    @Override
    public int size() {
        return array.size();
    }
}
