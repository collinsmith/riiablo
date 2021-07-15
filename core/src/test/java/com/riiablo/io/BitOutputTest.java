package com.riiablo.io;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class BitOutputTest {
  private static BitOutput newInstance() {
    return new ByteOutput(Unpooled.buffer(16, 16)).unalign();
  }

  public static void assertBuffer(BitOutput b, String expected) {
    ByteBuf buffer = b.byteOutput().buffer();
    assertEquals(expected, ByteBufUtil.hexDump(buffer, 0, 16));
  }

  @Test
  public void write_u8_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0xFF, Byte.SIZE);
    assertBuffer(b, "ff000000000000000000000000000000");
  }

  @Test
  public void write_u4_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0xF, 4);
    assertBuffer(b, "00000000000000000000000000000000");
    assertEquals(4, b.bitsCached());
    assertEquals(0xF, b.cache());
    b.flush();
    assertBuffer(b, "0f000000000000000000000000000000");
  }

  @Test
  public void write_u12_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0xFFF, 12);
    assertBuffer(b, "ff000000000000000000000000000000");
    assertEquals(4, b.bitsCached());
    assertEquals(0xF, b.cache());
    b.flush();
    assertBuffer(b, "ff0f0000000000000000000000000000");
  }

  @Test
  public void write_u12_u4_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0xFFF, 12);
    assertBuffer(b, "ff000000000000000000000000000000");
    assertEquals(4, b.bitsCached());
    assertEquals(0xF, b.cache());
    b._writeUnsigned(0xF, 4);
    assertBuffer(b, "ffff0000000000000000000000000000");
    assertEquals(0, b.bitsCached());
    assertEquals(0, b.cache());
  }

  @Test
  public void write_u12_u12_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0xFFF, 12);
    assertBuffer(b, "ff000000000000000000000000000000");
    assertEquals(4, b.bitsCached());
    assertEquals(0xF, b.cache());
    b._writeUnsigned(0xFF, 12);
    assertBuffer(b, "ffff0f00000000000000000000000000");
    assertEquals(0, b.bitsCached());
    assertEquals(0, b.cache());
  }

  @Test
  public void write_u7_u63_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0x7F, 7);
    assertBuffer(b, "00000000000000000000000000000000");
    assertEquals(7, b.bitsCached());
    assertEquals(0x7F, b.cache());
    b._writeUnsigned(0x7FFFFFFF_FFFFFFFFL, 63);
    assertBuffer(b, "ffffffffffffffff0000000000000000");
    assertEquals(6, b.bitsCached());
    assertEquals(0x1f, b.cache());
    b.flush();
    assertBuffer(b, "ffffffffffffffff1f00000000000000");
  }

  @Test
  public void write_u8_u63_aligned() {
    BitOutput b = newInstance();
    b._writeUnsigned(0x7F, 8);
    assertBuffer(b, "7f000000000000000000000000000000");
    assertEquals(0, b.bitsCached());
    assertEquals(0, b.cache());
    b._writeUnsigned(0x7FFFFFFF_FFFFFFFFL, 63);
    assertBuffer(b, "7fffffffffffffff0000000000000000");
    assertEquals(7, b.bitsCached());
    assertEquals(0x7f, b.cache());
    b.flush();
    assertBuffer(b, "7fffffffffffffff7f00000000000000");
  }

//  @Test
//  public void empty_bit_input_is_empty() {
//    BitInput b = BitInput.emptyBitInput();
//    assertEquals(0, b.bitsRemaining());
//    assertEquals(0, b.bytesRemaining());
//  }
//
//  @Test(expected = IllegalArgumentException.class)
//  public void align_neg_bytes_throws_IllegalArgumentException() {
//    BitInput b = newInstance();
//    try {
//      assert b.bitsRead() == 0;
//      b.align(-1);
//    } finally {
//      assertEquals(0L, b.bitsRead());
//    }
//  }
//
//  @Test
//  public void align_0_bytes_aligned() {
//    BitInput b = newInstance();
//    assert b.isAligned();
//    assert b.bitsCached() == 0;
//    b.align(0);
//    assertTrue(b.isAligned());
//    assertEquals(0, b.bitsRead());
//    assertEquals(0, b.bitsCached());
//    assertEquals(0, b.cache());
//  }
//
//  @Test
//  public void align_0_bytes_unaligned() {
//    BitInput b = newInstance();
//    b.skip(4);
//    assert !b.isAligned();
//    assert b.bitsCached() > 0;
//    b.align(0);
//    assertTrue(b.isAligned());
//    assertEquals(Byte.SIZE, b.bitsRead());
//    assertEquals(0, b.bitsCached());
//    assertEquals(0, b.cache());
//  }
//
//  @Test
//  public void align_1_byte_aligned() {
//    BitInput b = newInstance();
//    assert b.isAligned();
//    assert b.bitsCached() == 0;
//    b.align(1);
//    assertTrue(b.isAligned());
//    assertEquals(Byte.SIZE, b.bitsRead());
//    assertEquals(0, b.bitsCached());
//    assertEquals(0, b.cache());
//  }
//
//  @Test
//  public void align_1_byte_unaligned() {
//    BitInput b = newInstance();
//    b.skip(4);
//    assert !b.isAligned();
//    assert b.bitsCached() > 0;
//    b.align(1);
//    assertTrue(b.isAligned());
//    assertEquals(Byte.SIZE, b.bitsRead());
//    assertEquals(0, b.bitsCached());
//    assertEquals(0, b.cache());
//  }
}
