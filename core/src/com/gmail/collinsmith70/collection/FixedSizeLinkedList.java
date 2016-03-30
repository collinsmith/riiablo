package com.gmail.collinsmith70.collection;

import java.util.LinkedList;

public class FixedSizeLinkedList<E> extends LinkedList<E> {

  private static final int DEFAULT_CAPACITY = 1 << 8;

  private int capacity;

  public FixedSizeLinkedList() {
    this(DEFAULT_CAPACITY);
  }

  public FixedSizeLinkedList(int capacity) {
    this.capacity = capacity;
  }

  @Override
  public boolean add(E e) {
    if (super.size() == capacity) {
      super.remove(0);
    }

    return super.add(e);
  }

}