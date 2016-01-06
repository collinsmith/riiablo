package com.gmail.collinsmith70.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class FixedArrayList<V> implements Collection<V> {

private static final int DEFAULT_SIZE = 1<<8;

private Object[] data;
private int head;
private int size;
private int pushes;

public FixedArrayList() {
    this(DEFAULT_SIZE);
}

public FixedArrayList(int size) {
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

public void push(V obj) {
    data[head] = obj;
    head = increment(head);
    pushes++;
    size = Math.min(limit(), pushes);
}

public V head() {
    return get(decrement(head));
}

@SuppressWarnings("unchecked")
protected V get(int i) {
    return (V)data[i];
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
public Iterator<V> iterator() {
    return new Iterator<V>() {

        private int head = FixedArrayList.this.decrement(
                FixedArrayList.this.head);
        private int iterations = 0;

        @Override
        public boolean hasNext() {
            return iterations < FixedArrayList.this.size()
                    && FixedArrayList.this.get(head) != null;
        }

        @Override
        public V next() {
            V data = FixedArrayList.this.get(head);
            head = FixedArrayList.this.decrement(head);
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
    // TODO: validate that a is a superclass of "V"
    return Arrays.copyOf((T[])data, size());
}

@Override
public boolean add(V v) {
    push(v);
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
public boolean addAll(Collection<? extends V> c) {
    for (V obj : c) {
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