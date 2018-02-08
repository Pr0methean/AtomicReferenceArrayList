package io.github.pr0methean.util;

import java.util.AbstractList;
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
public class AtomicReferenceArrayList<T> extends AbstractList<T> {
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

    public AtomicReferenceArrayList(T... elements) {
        this(new AtomicReferenceArray<>(elements));
        size.set(elements.length);
    }

    @Override
    public T set(final int index, T element) {
        sizeLock.readLock().lock();
        if (index < array.length()) {
            try {
                size.updateAndGet(oldLength -> Math.max(oldLength, index + 1));
                return array.getAndSet(index, element);
            } finally {
                sizeLock.readLock().unlock();
            }
        } else {
            sizeLock.readLock().unlock();
            sizeLock.writeLock().lock();
            try {
                AtomicReferenceArray<T> oldArray = array;
                array = new AtomicReferenceArray<>(array.length() * 2);
                for (int i = 0; i < oldArray.length(); i++) {
                    array.set(i, oldArray.get(i));
                }
                array.set(index, element);
                return null;
            } finally {
                sizeLock.writeLock().unlock();
            }
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
}
