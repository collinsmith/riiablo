package com.riiablo.asset;

public class PriorityContainer<T> implements Comparable<PriorityContainer<T>> {
  public static <T> PriorityContainer<T> wrap(int priority, T ref) {
    return new PriorityContainer<>(priority, ref);
  }

//  public static <T> Iterable<T> unwrap(Iterable<PriorityContainer<T>> it) {
//    return IteratorUtils.transformedIterator(it, new Transformer<PriorityContainer<T>, T>() {
//      @Override
//      public T transform(PriorityContainer<T> input) {
//        return input.ref;
//      }
//    });
//  }

  final int priority;
  final T ref;

  PriorityContainer(int priority, T ref) {
    this.priority = priority;
    this.ref = ref;
  }

  @Override
  public int compareTo(PriorityContainer<T> other) {
    return Integer.compare(other.priority, priority);
  }
}
