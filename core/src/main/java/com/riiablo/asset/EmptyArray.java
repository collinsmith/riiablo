package com.riiablo.asset;

import com.badlogic.gdx.utils.Array;

import static com.riiablo.util.ImplUtils.unsupported;

// TODO: implement additional methods to tighten immutability contract
public final class EmptyArray<T> extends Array<T> {
  @SuppressWarnings("rawtypes")
  public static final EmptyArray INSTANCE = new EmptyArray<>();

  @SuppressWarnings("unchecked")
  public static <T> Array<T> empty() {
    return (Array<T>) INSTANCE;
  }

  EmptyArray() {
    super(false, 0);
  }

  @Override
  public void add(T value) {
    unsupported("immutable");
  }

  @Override
  public void add(T value1, T value2) {
    unsupported("immutable");
  }

  @Override
  public void add(T value1, T value2, T value3) {
    unsupported("immutable");
  }

  @Override
  public void add(T value1, T value2, T value3, T value4) {
    unsupported("immutable");
  }

  @Override
  public void addAll(T... array) {
    unsupported("immutable");
  }

  @Override
  public void addAll(Array<? extends T> array) {
    unsupported("immutable");
  }

  @Override
  public void addAll(T[] array, int start, int count) {
    unsupported("immutable");
  }

  @Override
  public void addAll(Array<? extends T> array, int start, int count) {
    unsupported("immutable");
  }
}
