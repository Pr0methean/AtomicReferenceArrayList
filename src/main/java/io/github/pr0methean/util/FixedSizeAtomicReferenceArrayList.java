package io.github.pr0methean.util;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Concurrent, fixed-length list backed by an {@link AtomicReferenceArray}.
 *
 * @param <T> the element type
 */
public class FixedSizeAtomicReferenceArrayList<T> extends AbstractList<T> {

    protected final AtomicReferenceArray<T> array;

    protected FixedSizeAtomicReferenceArrayList(AtomicReferenceArray<T> array) {
        this.array = array;
    }

    public FixedSizeAtomicReferenceArrayList(int size) {
        this(new AtomicReferenceArray<T>(size));
    }

    public FixedSizeAtomicReferenceArrayList(T... elements) {
        this(new AtomicReferenceArray<T>(elements));
    }

    @Override
    public T get(int index) {
        return array.get(index);
    }

    @Override
    public T set(int index, T element) {
        return array.getAndSet(index, element);
    }

    @Override
    public int size() {
        return array.length();
    }
}
