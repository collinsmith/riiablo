package com.riiablo.util;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/** borrowed from <a href="https://github.com/EsotericSoftware/kryo">kryo</a> */
public abstract class Pool<T> {
  private final Queue<T> freeObjects;
  private int peak;

  public Pool(boolean threadSafe, boolean softReferences) {
    this(threadSafe, softReferences, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public Pool(boolean threadSafe, boolean softReferences, final int maxCapacity) {
    final Queue<T> queue;
    if (threadSafe) {
      queue = new LinkedBlockingQueue<T>(maxCapacity) {
        @Override
        public boolean add(T o) {
          if (size() >= maxCapacity) return false;
          super.add(o);
          return true;
        }
      };
    } else if (softReferences) {
      queue = new LinkedList<T>() { // More efficient clean() than ArrayDeque.
        @Override
        public boolean add(T o) {
          if (size() >= maxCapacity) return false;
          super.add(o);
          return true;
        }
      };
    } else {
      queue = new ArrayDeque<T>() {
        @Override
        public boolean offer(T o) {
          if (size() >= maxCapacity) return false;
          super.offer(o);
          return true;
        }
      };
    }

    freeObjects = softReferences
        ? new SoftReferenceQueue<>((Queue<SoftReference<T>>) queue)
        : queue;
  }

  protected abstract T newInstance();

  public T obtain() {
    final T object = freeObjects.poll();
    return object != null ? object : newInstance();
  }

  public void release(T object) {
    if (object == null) throw new IllegalArgumentException("object cannot be null");
    reset(object);
    if (!freeObjects.offer(object) && freeObjects instanceof SoftReferenceQueue) {
      ((SoftReferenceQueue<T>) freeObjects).cleanOne();
      freeObjects.offer(object);
    }

    peak = Math.max(peak, freeObjects.size());
  }

  protected void reset(T object) {
    if (object instanceof Poolable) ((Poolable) object).reset();
  }

  public void clear() {
    freeObjects.clear();
  }

  public void clean() {
    if (freeObjects instanceof SoftReferenceQueue) ((SoftReferenceQueue<T>) freeObjects).clean();
  }

  public int free() {
    return freeObjects.size();
  }

  public int peak() {
    return peak;
  }

  public void resetPeak() {
    peak = 0;
  }

  public interface Poolable {
    void reset();
  }

  static class SoftReferenceQueue<T> implements Queue<T> {
    private final Queue<SoftReference<T>> delegate;

    public SoftReferenceQueue(Queue<SoftReference<T>> delegate) {
      this.delegate = delegate;
    }

    @Override
    public T poll() {
      while (true) {
        SoftReference<T> reference = delegate.poll();
        if (reference == null) return null;
        T object = reference.get();
        if (object != null) return object;
      }
    }

    @Override
    public boolean offer(T e) {
      return delegate.add(new SoftReference<>(e));
    }

    @Override
    public int size() {
      return delegate.size();
    }

    @Override
    public void clear() {
      delegate.clear();
    }

    void cleanOne() {
      for (Iterator<SoftReference<T>> it = delegate.iterator(); it.hasNext();) {
        if (it.next().get() == null) {
          it.remove();
          break;
        }
      }
    }

    void clean() {
      CollectionUtils.filter(delegate, new Predicate<SoftReference<T>>() {
        @Override
        public boolean evaluate(SoftReference<T> object) {
          return object.get() != null;
        }
      });
    }

    @Override
    public boolean add(T e) {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean contains(Object o) {
      return false;
    }

    @Override
    public Iterator<T> iterator() {
      return null;
    }

    @Override
    public T remove() {
      return null;
    }

    @Override
    public Object[] toArray() {
      return null;
    }

    @Override
    public T element() {
      return null;
    }

    @Override
    public T peek() {
      return null;
    }

    @Override
    public <E> E[] toArray(E[] a) {
      return null;
    }

    @Override
    public boolean remove(Object o) {
      return false;
    }

    @Override
    public boolean containsAll(Collection c) {
      return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
      return false;
    }

    @Override
    public boolean removeAll(Collection c) {
      return false;
    }

    @Override
    public boolean retainAll(Collection c) {
      return false;
    }
  }
}
