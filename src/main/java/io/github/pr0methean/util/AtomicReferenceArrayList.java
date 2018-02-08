package io.github.pr0methean.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Concurrent list of mutable length, backed by an {@link AtomicReferenceArray}. I believe all
 * methods have the same big-O time complexity as on an {@link java.util.ArrayList}.
 *
 * @param <T> the element type
 */
public class AtomicReferenceArrayList<T> extends AbstractList<T>
        implements Cloneable, Serializable {
    @SuppressWarnings({"MethodDoesntCallSuperMethod","unchecked"})
    @Override
    public AtomicReferenceArrayList<T> clone() {
        return (AtomicReferenceArrayList<T>) new AtomicReferenceArrayList<>(toArray());
    }

    protected AtomicReferenceArray<T> array;
    private final ReadWriteLock sizeLock = new ReentrantReadWriteLock();
    private final AtomicInteger size = new AtomicInteger(0);

    protected AtomicReferenceArrayList(AtomicReferenceArray<T> array) {
        this.array = array;
    }

    public AtomicReferenceArrayList() {
        this(1);
    }

    public AtomicReferenceArrayList(int size) {
        this(new AtomicReferenceArray<>(size));
    }

    @SafeVarargs
    public AtomicReferenceArrayList(T... elements) {
        this(new AtomicReferenceArray<>(elements));
        size.set(elements.length);
    }

    @Override
    public T set(final int index, T element) {
        sizeLock.readLock().lock();
        try {
            if (index < array.length()) {
                size.updateAndGet(oldLength -> Math.max(oldLength, index + 1));
                return array.getAndSet(index, element);
            }
        } finally {
            sizeLock.readLock().unlock();
        }
        // If we've gotten to here, check whether we're expanding the list
        sizeLock.writeLock().lock();
        try {
            if (array.length() <= index) {
                AtomicReferenceArray<T> oldArray = array;
                array = new AtomicReferenceArray<>(Math.max(index + 1, array.length() * 2));
                for (int i = 0; i < oldArray.length(); i++) {
                    array.set(i, oldArray.get(i));
                }
            }
            return array.getAndSet(index, element);
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    @Override
    public boolean add(T t) {
        sizeLock.writeLock().lock();
        try {
            return super.add(t);
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    @Override
    public void add(int index, T element) {
        sizeLock.writeLock().lock();
        try {
            if (index < size()) {
                for (int i = index; i < size.getAndIncrement(); i++) {
                    array.set(i + 1, array.get(i));
                }
            }
            set(index, element);
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    @Override
    public T remove(int index) {
        sizeLock.writeLock().lock();
        try {
            int size = size();
            if (index >= size) {
                throw new ArrayIndexOutOfBoundsException();
            }
            T removed = get(index);
            for (int i = index; i < size - 1; i++) {
                array.set(i, array.get(i + 1));
            }
            this.size.decrementAndGet();
            return removed;
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    @Override
    public T get(int index) {
        sizeLock.readLock().lock();
        try {
            if (index >= size.get()) {
                throw new ArrayIndexOutOfBoundsException();
            }
            return array.get(index);
        } finally {
            sizeLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        sizeLock.readLock().lock();
        try {
            return size.get();
        } finally {
            sizeLock.readLock().unlock();
        }
    }

    public boolean addAll(List<? extends T> ts) {
        for (T t : ts) {
            add(t);
        }
        return true;
    }

    /**
     * Appends all of the elements in the specified collection that
     * are not already contained in this list, to the end of
     * this list, in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param c collection containing elements to be added to this list
     * @return the number of elements added
     * @throws NullPointerException if the specified collection is null
     * @see #addIfAbsent(Object)
     */
    public int addAllAbsent(List<T> addFrom) {
        sizeLock.writeLock().lock();
        try {
            int added = 0;
            for (T element : addFrom) {
                if (!contains(element) && add(element)) {
                    added++;
                }
            }
            return added;
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    /**
     * Appends the element, if not present.
     *
     * @param e element to be added to this list, if absent
     * @return {@code true} if the element was added
     */
    public boolean addIfAbsent(T element) {
        sizeLock.writeLock().lock();
        try {
            return !contains(element) && add(element);
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        sizeLock.writeLock().lock();
        try {
            array = new AtomicReferenceArray<>(1);
            size.set(0);
        } finally {
            sizeLock.writeLock().unlock();
        }
    }

    /**
     * Returns the index of the first occurrence of the specified element in
     * this list, searching forwards from {@code index}, or returns -1 if
     * the element is not found.
     * More formally, returns the lowest index {@code i} such that
     * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(e==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;e.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     *
     * @param element element to search for
     * @param index index to start searching from
     * @return the index of the first occurrence of the element in
     *         this list at position {@code index} or later in the list;
     *         {@code -1} if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public int indexOf(T element, int index) {
        // TODO
        return 0;
    }

    /**
     * Returns the index of the last occurrence of the specified element in
     * this list, searching backwards from {@code index}, or returns -1 if
     * the element is not found.
     * More formally, returns the highest index {@code i} such that
     * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(e==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;e.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     *
     * @param element element to search for
     * @param index index to start searching backwards from
     * @return the index of the last occurrence of the element at position
     *         less than or equal to {@code index} in this list;
     *         -1 if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is greater
     *         than or equal to the current size of this list
     */
    public int lastIndexOf(T element, T index) {
        // TODO
        return 0;
    }
}
