package com.gmail.collinsmith70.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implementation of a {@link Collection} which stores a fixed number of values and when a new
 * element is added once the {@linkplain Collection} is full, the oldest element is replaced.
 *
 * @param <E> class type which is stored within this {@linkplain FixedSizeArrayList}
 */
public class FixedSizeArrayList<E> implements Collection<E> {

private static final int DEFAULT_SIZE = 1<<8;

private Object[] data;
private int head;
private int size;
private int pushes;

public FixedSizeArrayList() {
    this(DEFAULT_SIZE);
}

public FixedSizeArrayList(int size) {
    if (size < 0) {
        throw new IllegalArgumentException(
                "size must be a positive integer");
    }

    clear(size);
}

public int size() {
    return size;
}

public boolean isEmpty() {
    return size() != 0;
}

public int pushes() {
    return pushes;
}

public int limit() {
    return data.length;
}

public void clear() {
    clear(data.length);
}

private void clear(int size) {
    assert size > 0 : "size should be > 0";
    this.data = new Object[size];
    this.head = 0;
    this.size = 0;
    this.pushes = 0;
}

public void push(E obj) {
    data[head] = obj;
    head = increment(head);
    pushes++;
    size = Math.min(limit(), pushes);
}

public E head() {
    return get(decrement(head));
}

@SuppressWarnings("unchecked")
protected E get(int i) {
    return (E)data[i];
}

private int increment(int i) {
    if (i + 1 >= data.length) {
        return 0;
    }

    return i + 1;
}

private int decrement(int i) {
    if (i - 1 < 0) {
        return data.length - 1;
    }

    return i - 1;
}

@Override
public Iterator<E> iterator() {
    return new Iterator<E>() {

        private int head = FixedSizeArrayList.this.decrement(
                FixedSizeArrayList.this.head);
        private int iterations = 0;

        @Override
        public boolean hasNext() {
            return iterations < FixedSizeArrayList.this.size()
                    && FixedSizeArrayList.this.get(head) != null;
        }

        @Override
        public E next() {
            E data = FixedSizeArrayList.this.get(head);
            head = FixedSizeArrayList.this.decrement(head);
            iterations++;
            return data;
        }

    };
}

@Override
public boolean contains(Object o) {
    if (o == null) {
        return false;
    }

    for (Object obj : data) {
        if (obj.equals(o)) {
            return true;
        }
    }

    return false;
}

@Override
public Object[] toArray() {
    return Arrays.copyOf(data, size());
}

@Override
@SuppressWarnings("unchecked")
public <T> T[] toArray(T[] a) {
    if (!a.getClass().isAssignableFrom(data.getClass())) {
        throw new ClassCastException("passed array should be a superclass of " + data.getClass().getName());
    }

    return Arrays.copyOf((T[])data, size());
}

@Override
public boolean add(E e) {
    push(e);
    return true;
}

@Override
public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported.");
}

@Override
public boolean containsAll(Collection<?> c) {
    for (Object obj : c) {
        if (!contains(obj)) {
            return false;
        }
    }

    return true;
}

@Override
public boolean addAll(Collection<? extends E> c) {
    for (E obj : c) {
        add(obj);
    }

    return true;
}

@Override
public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported.");
}

@Override
public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported.");
}

}