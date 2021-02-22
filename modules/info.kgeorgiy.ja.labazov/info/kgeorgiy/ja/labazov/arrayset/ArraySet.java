package info.kgeorgiy.ja.labazov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final Comparator<? super E> comparator;
    private final List<E> array;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, null);
    }

    public ArraySet(Collection<? extends E> c, Comparator<? super E> comparator) {
        this.comparator = comparator;
        NavigableSet<E> tmpSet = new TreeSet<>(comparator);
        tmpSet.addAll(c);
        this.array = Collections.unmodifiableList(new ArrayList<>(tmpSet));
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.array = list;
    }

    private int searchIdx(E e, boolean direction, boolean inclusive) {
        int pos = Collections.binarySearch(array, e, comparator);
        if (pos >= 0) {
            return pos + (inclusive ? 0 : (direction ? 1 : -1));
        }
        return -(pos + 1) + (direction ? 0 : -1);
    }

    @Override
    public E lower(E e) {
        return getCheckBounds(searchIdx(e, false, false));
    }

    @Override
    public E floor(E e) {
        return getCheckBounds(searchIdx(e, false, true));
    }

    @Override
    public E ceiling(E e) {
        return getCheckBounds(searchIdx(e, true, true));
    }

    @Override
    public E higher(E e) {
        return getCheckBounds(searchIdx(e, true, false));
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return array.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(ReversedListView.reverse(array), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    private int compare(E e1, E e2) {
        return (comparator == null) ? ((Comparable<E>) e1).compareTo(e2) : comparator.compare(e1, e2);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException(fromElement + " should be less than " + toElement);
        }

        int posFrom = searchIdx(fromElement, true, fromInclusive);
        int posTo = searchIdx(toElement, false, toInclusive);

        if (posTo < posFrom) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        }

        return new ArraySet<>(array.subList(posFrom, posTo + 1), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int pos = searchIdx(toElement, false, inclusive);
        return new ArraySet<>(array.subList(0, pos + 1), comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int pos = searchIdx(fromElement, true, inclusive);
        return new ArraySet<>(array.subList(pos, size()), comparator);
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    private E get(int idx) {
        E ret = getOrNull(idx);
        if (ret == null) {
            throw new NoSuchElementException("ArraySet is empty");
        }

        return ret;
    }

    private E getOrNull(int idx) {
        if (isEmpty()) {
            return null;
        }

        return array.get(idx);
    }

    private E getCheckBounds(int idx) {
        if (idx < 0 || idx >= size()) {
            return null;
        }

        return array.get(idx);
    }

    @Override
    public E first() {
        return get(0);
    }

    @Override
    public E last() {
        return get(size() - 1);
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(array, (E) o, comparator) >= 0;
    }
}
