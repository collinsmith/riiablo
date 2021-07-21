package com.riiablo.map2.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.OutputStreamAppender;

class BucketPoolTest {
  @BeforeAll
  static void before() {
    LogManager.getRootLogger().addAppender(new OutputStreamAppender(System.out));
    LogManager.setLevel("com.riiablo.map2.util", Level.TRACE);
  }

  static BucketPool<byte[]> newByteInstance() {
    return BucketPool
        .builder(byte[].class)
        .add(16)
        .add(32)
        .add(64)
        .build();
  }

  @Test
  void byte_array_pool() {
    BucketPool<byte[]> buckets = newByteInstance();
    assertEquals(3, buckets.pools.size);
    assertEquals(16, buckets.pools.get(0).length);
    assertEquals(32, buckets.pools.get(1).length);
    assertEquals(64, buckets.pools.get(2).length);
  }

  @Test
  void byte_array_pool_get() {
    BucketPool<byte[]> buckets = newByteInstance();
    assertEquals(buckets.get(15), buckets.pools.get(0));
    assertEquals(buckets.get(16), buckets.pools.get(0));
    assertEquals(buckets.get(31), buckets.pools.get(1));
    assertEquals(buckets.get(32), buckets.pools.get(1));
    assertEquals(buckets.get(63), buckets.pools.get(2));
    assertEquals(buckets.get(64), buckets.pools.get(2));
  }

  @Test
  void byte_array_pool_get_high() {
    BucketPool<byte[]> buckets = newByteInstance();
    assertThrows(NoSuchElementException.class, () -> buckets.get(65));
  }

  @Test
  void byte_array_pool_obtain_15() {
    BucketPool<byte[]> buckets = newByteInstance();
    byte[] data = buckets.obtain(15);
    assertTrue(data.length >= 15);
    buckets.free(data);
  }

  @Test
  void byte_array_pool_obtain_16() {
    BucketPool<byte[]> buckets = newByteInstance();
    byte[] data = buckets.obtain(16);
    assertTrue(data.length >= 16);
    buckets.free(data);
  }

  @Test
  void byte_array_pool_obtain_65() {
    BucketPool<byte[]> buckets = newByteInstance();
    byte[] data = buckets.obtain(65);
    assertTrue(data.length >= 65);
    buckets.free(data);
  }
}
