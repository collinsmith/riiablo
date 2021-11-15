package com.riiablo.asset;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Transformer;

public class PriorityContainer<T> implements Comparable<PriorityContainer<T>> {
  static <T> PriorityContainer<T> wrap(int priority, T ref) {
    return new PriorityContainer<>(priority, ref);
  }

  /** transforms a stream of containers into their elements */
  static <T> Iterable<T> unwrap(Iterable<? extends PriorityContainer<T>> it) {
    return IteratorUtils.asIterable(IteratorUtils.transformedIterator(
        it.iterator(),
        new Transformer<PriorityContainer<T>, T>() {
      @Override
      public T transform(PriorityContainer<T> input) {
        return input.ref;
      }
    }));
  }

  static <T> T[] toArray(Iterable<? extends PriorityContainer<T>> it, Class<T> clazz) {
    return IteratorUtils.toArray(unwrap(it).iterator(), clazz);
  }

  final int priority;
  final T ref;

  PriorityContainer(int priority, T ref) {
    this.priority = priority;
    this.ref = ref;
  }

  @Override
  public int compareTo(PriorityContainer<T> other) {
    return Integer.compare(priority, other.priority);
  }
}
