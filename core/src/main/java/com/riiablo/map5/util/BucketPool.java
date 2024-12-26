package com.riiablo.map5.util;

import java.util.Arrays;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class BucketPool<E> {
  private static final Logger log = LogManager.getLogger(BucketPool.class);

  final Class<E> clazz;
  final Array<ArrayPool<E>> pools;

  BucketPool(Class<E> clazz, int[] lengths, int offset, int length) {
    this.clazz = clazz;
    Arrays.sort(lengths, offset, offset + length);
    pools = new Array<>(ArrayPool.class);
    for (int i = offset, s = i + length; i < s; i++) {
      pools.add(ArrayPool.get(clazz, lengths[i], 16, Integer.MAX_VALUE));
    }
  }

  public Pool<E> get(int length) {
    for (ArrayPool<E> pool : pools) {
      if (length <= pool.length) {
        return pool;
      }
    }

    throw new NoSuchElementException("No bucket big enough for length: " + length);
  }

  public E obtain(int length) {
    try {
      Pool<E> pool = get(length);
      return pool.obtain();
    } catch (NoSuchElementException t) {
      E instance = ArrayPool.create(clazz, length);
      log.debugf("obtain custom-sized array instance: 0x%h %s.length=%d",
          System.identityHashCode(instance),
          clazz.getSimpleName(),
          length);
      return instance;
    }
  }

  public void free(E o) {
    int length = ArrayUtils.getLength(o);
    if (length <= 0) return;
    try {
      Pool<E> pool = get(length);
      pool.free(o);
    } catch (NoSuchElementException t) {
      log.debugf("free custom-sized array instance: 0x%h %s.length=%d",
          System.identityHashCode(o),
          clazz.getSimpleName(),
          length);
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("pools", pools)
        .toString();
  }

  public static <E> Builder<E> builder(Class<E> clazz) {
    if (!clazz.isArray()) throw new IllegalArgumentException("clazz must be an array type");
    return new Builder<>(clazz);
  }

  public static class Builder<E> {
    final Class<E> clazz;
    final IntArray lengths;

    Builder(Class<E> clazz) {
      this.clazz = clazz;
      lengths = new IntArray(4);
    }

    public Builder<E> add(int length) {
      lengths.add(length);
      return this;
    }

    public Builder<E> scl(int factor) {
      int[] arr = lengths.items;
      for (int i = 0, s = lengths.size; i < s; i++) {
        arr[i] *= factor;
      }
      return this;
    }

    public BucketPool<E> build() {
      return new BucketPool<>(clazz, lengths.items, 0, lengths.size);
    }
  }
}
