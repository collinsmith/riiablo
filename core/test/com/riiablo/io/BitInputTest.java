package com.riiablo.io;

import org.junit.Assert;
import org.junit.Test;

public class BitInputTest {
  private static BitInput newInstance() {
    return BitInput.wrap(new byte[] {
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA
    });
  }

  @Test
  public void align_matches_unalign() {
    BitInput b = newInstance();
    Assert.assertEquals(b, b.align().unalign());
  }

  @Test
  public void empty_bit_input_is_empty() {
    BitInput b = ByteInput.emptyByteInput().unalign();
    Assert.assertEquals(0, b.bitsRemaining());
    Assert.assertEquals(0, b.bytesRemaining());
  }

  @Test(expected = IllegalArgumentException.class)
  public void skip_neg_bits_throws_IllegalArgumentException() {
    BitInput b = newInstance();
    try {
      assert b.bitsRead() == 0;
      b.skipBits(-1);
    } finally {
      Assert.assertEquals(0L, b.bitsRead());
    }
  }

  @Test
  public void skip_0_bits_aligned() {
    BitInput b = newInstance();
    long bitsRead = b.bitsRead();
    assert b.aligned();
    b.skipBits(0);
    Assert.assertTrue(b.aligned());
    Assert.assertEquals(bitsRead, b.bitsRead());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0, b.cache());
  }

  @Test
  public void skip_0_bits_unaligned() {
    BitInput b = newInstance();
    b.skipBits(4);
    long bitsRead = b.bitsRead();
    int bitsCached = b.bitsCached();
    long cache = b.cache();
    assert !b.aligned();
    b.skipBits(0);
    Assert.assertTrue(!b.aligned());
    Assert.assertEquals(bitsRead, b.bitsRead());
    Assert.assertEquals(bitsCached, b.bitsCached());
    Assert.assertEquals(cache, b.cache());
  }

  @Test
  public void skip_n_bits_aligned_to_aligned() {
    BitInput b = newInstance();
    assert b.aligned();
    b.skipBits(Byte.SIZE);
    Assert.assertEquals(Byte.SIZE, b.bitsRead());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0, b.cache());
  }

  @Test
  public void skip_n_bits_aligned_to_unaligned() {
    BitInput b = newInstance();
    assert b.aligned();
    b.skipBits(Byte.SIZE - 1);
    Assert.assertTrue(!b.aligned());
    Assert.assertEquals(Byte.SIZE - 1, b.bitsRead());
    Assert.assertEquals(1, b.bitsCached());
    Assert.assertEquals(0b1, b.cache());
  }

  @Test
  public void skip_n_bits_unaligned_to_aligned() {
    BitInput b = newInstance();
    b.skipBits(1);
    assert b.bitsRead() == 1;
    assert !b.aligned();
    b.skipBits(Byte.SIZE - 1);
    Assert.assertTrue(b.aligned());
    Assert.assertEquals(Byte.SIZE, b.bitsRead());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0, b.cache());
  }

  @Test
  public void skip_n_bits_unaligned_to_unaligned() {
    BitInput b = newInstance();
    b.skipBits(1);
    assert !b.aligned();
    b.skipBits(Byte.SIZE - 2);
    Assert.assertTrue(!b.aligned());
    Assert.assertEquals(Byte.SIZE - 1, b.bitsRead());
    Assert.assertEquals(1, b.bitsCached());
    Assert.assertEquals(0b1, b.cache());
  }

  @Test
  public void skip_n_bits_aligned_to_aligned_multibyte() {
    BitInput b = newInstance();
    assert b.aligned();
    b.skipBits(Byte.SIZE + Byte.SIZE);
    Assert.assertEquals(Byte.SIZE + Byte.SIZE, b.bitsRead());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0, b.cache());
  }

  @Test
  public void skip_n_bits_aligned_to_unaligned_multibyte() {
    BitInput b = newInstance();
    assert b.aligned();
    b.skipBits(Byte.SIZE + Byte.SIZE - 1);
    Assert.assertTrue(!b.aligned());
    Assert.assertEquals(Byte.SIZE + Byte.SIZE - 1, b.bitsRead());
    Assert.assertEquals(1, b.bitsCached());
    Assert.assertEquals(0b1, b.cache());
  }

  @Test
  public void skip_n_bits_unaligned_to_aligned_multibyte() {
    BitInput b = newInstance();
    b.skipBits(1);
    assert b.bitsRead() == 1;
    assert !b.aligned();
    b.skipBits(Byte.SIZE + Byte.SIZE - 1);
    Assert.assertTrue(b.aligned());
    Assert.assertEquals(Byte.SIZE + Byte.SIZE, b.bitsRead());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0, b.cache());
  }

  @Test
  public void skip_n_bits_unaligned_to_unaligned_multibyte() {
    BitInput b = newInstance();
    b.skipBits(1);
    assert !b.aligned();
    b.skipBits(Byte.SIZE);
    Assert.assertTrue(!b.aligned());
    Assert.assertEquals(Byte.SIZE + 1, b.bitsRead());
    Assert.assertEquals(7, b.bitsCached());
    Assert.assertEquals(0b1011111, b.cache());
  }

  @Test
  public void read_npc_data() {
    BitInput b = BitInput.wrap(new byte[] {
        0x01, 0x77,
        0x34, 0x00,
        (byte) 0xAC, (byte) 0xAE, (byte) 0xA5, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAC, (byte) 0xBE, (byte) 0xA4, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAE, (byte) 0xAE, (byte) 0xA4, (byte) 0xC9, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xFA, (byte) 0x7B, (byte) 0xE7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0xDA, (byte) 0x79, (byte) 0xC7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0x10, (byte) 0x51, (byte) 0xE3, (byte) 0x18, 0x00, 0x00, 0x00, 0x00});
    Assert.assertArrayEquals(new byte[] {0x01, 0x77}, b.align().readBytes(2)); // signature
    Assert.assertEquals(52, b.readUnsigned(16)); // size
    Assert.assertEquals(0x00000002_89A5AEACL, b.readRaw(64));
    Assert.assertEquals(0x00000002_89A4BEACL, b.readRaw(64));
    Assert.assertEquals(0x00000002_C9A4AEAEL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18E77BFAL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18C779DAL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18E35110L, b.readRaw(64));
    Assert.assertEquals(0, b.bitsRemaining());
  }

  @Test
  public void read_npc_data_using_api() {
    BitInput b = BitInput.wrap(new byte[]{
        0x01, 0x77,
        0x34, 0x00,
        (byte) 0xAC, (byte) 0xAE, (byte) 0xA5, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAC, (byte) 0xBE, (byte) 0xA4, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAE, (byte) 0xAE, (byte) 0xA4, (byte) 0xC9, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xFA, (byte) 0x7B, (byte) 0xE7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0xDA, (byte) 0x79, (byte) 0xC7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0x10, (byte) 0x51, (byte) 0xE3, (byte) 0x18, 0x00, 0x00, 0x00, 0x00});
    Assert.assertArrayEquals(new byte[]{0x01, 0x77}, b.align().readBytes(2)); // signature
    Assert.assertEquals(52, b.read16u()); // size
    Assert.assertEquals(0x00000002_89A5AEACL, b.readRaw(64));
    Assert.assertEquals(0x00000002_89A4BEACL, b.readRaw(64));
    Assert.assertEquals(0x00000002_C9A4AEAEL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18E77BFAL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18C779DAL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18E35110L, b.readRaw(64));
    Assert.assertEquals(0, b.bitsRemaining());
  }

  @Test
  public void read_hunters_bow_of_blight() {
    BitInput b = BitInput.wrap(new byte[] {
        0x4A, 0x4D, 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07});
    Assert.assertArrayEquals(new byte[] {0x4A, 0x4D}, b.align().readBytes(2)); // signature
    Assert.assertEquals(0x00800010, b.readUnsigned(Integer.SIZE)); // flags
    Assert.assertEquals(101, b.readUnsigned(8)); // version
    b.skipBits(2); // unknown
    Assert.assertEquals(0, b.readUnsigned(3)); // location
    Assert.assertEquals(0, b.readUnsigned(4)); // body location
    Assert.assertEquals(2, b.readUnsigned(4)); // grid x
    Assert.assertEquals(0, b.readUnsigned(4)); // grid y
    Assert.assertEquals(1, b.readUnsigned(3)); // store location
    Assert.assertEquals("hbw ", b.readString(4)); // code
    Assert.assertEquals(0, b.readUnsigned(3)); // sockets filled
    Assert.assertEquals(0x2555A813, b.readUnsigned(Integer.SIZE)); // id
    Assert.assertEquals(6, b.readUnsigned(7)); // ilvl
    Assert.assertEquals(4, b.readUnsigned(4)); // quality
    Assert.assertEquals(false, b.readBoolean()); // picture id
    Assert.assertEquals(false, b.readBoolean()); // class only
    Assert.assertEquals(0, b.readUnsigned(11)); // magic prefix
    Assert.assertEquals(737, b.readUnsigned(11)); // magic suffix
    b.skipBits(1); // unknown
    Assert.assertEquals(32, b.readUnsigned(8)); // max durability
    Assert.assertEquals(32, b.readUnsigned(9)); // durability
    Assert.assertEquals(57, b.readUnsigned(9)); // poisonmindam
    Assert.assertEquals(0, b.readUnsigned(0)); // poisonmindam param bits
    Assert.assertEquals(8, b.readUnsigned(10)); // poisonmindam value
    Assert.assertEquals(0, b.readUnsigned(0)); // poisonmaxdam param bits
    Assert.assertEquals(8, b.readUnsigned(10)); // poisonmaxdam value
    Assert.assertEquals(0, b.readUnsigned(0)); // poisonlength param bits
    Assert.assertEquals(50, b.readUnsigned(9)); // poisonlength value
    Assert.assertEquals(0x1ff, b.readUnsigned(9)); // stat list finished
    Assert.assertEquals(5, b.bitsRemaining()); // tail end of stream
  }

  @Test
  public void read_hunters_bow_of_blight_using_api() {
    BitInput b = BitInput.wrap(new byte[] {
        0x4A, 0x4D, 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07});
    Assert.assertArrayEquals(new byte[] {0x4A, 0x4D}, b.align().readBytes(2)); // signature
    Assert.assertEquals(0x00800010, b.readRaw(32)); // flags
    Assert.assertEquals(101, b.read8u()); // version
    b.skipBits(2); // unknown
    Assert.assertEquals(0, b.read7u(3)); // location
    Assert.assertEquals(0, b.read7u(4)); // body location
    Assert.assertEquals(2, b.read7u(4)); // grid x
    Assert.assertEquals(0, b.read7u(4)); // grid y
    Assert.assertEquals(1, b.read7u(3)); // store location
    Assert.assertEquals("hbw ", b.readString(4)); // code
    Assert.assertEquals(0, b.read7u(3)); // sockets filled
    Assert.assertEquals(0x2555A813, b.readRaw(32)); // id
    Assert.assertEquals(6, b.read7u(7)); // ilvl
    Assert.assertEquals(4, b.read7u(4)); // quality
    Assert.assertEquals(false, b.readBoolean()); // picture id
    Assert.assertEquals(false, b.readBoolean()); // class only
    Assert.assertEquals(0, b.read15u(11)); // magic prefix
    Assert.assertEquals(737, b.read15u(11)); // magic suffix
    b.skipBits(1); // unknown
    Assert.assertEquals(32, b.read15u(8)); // max durability
    Assert.assertEquals(32, b.read15u(9)); // durability
    Assert.assertEquals(57, b.read15u(9)); // poisonmindam
    Assert.assertEquals(0, b.read31u(0)); // poisonmindam param bits
    Assert.assertEquals(8, b.read31u(10)); // poisonmindam value
    Assert.assertEquals(0, b.read31u(0)); // poisonmaxdam param bits
    Assert.assertEquals(8, b.read31u(10)); // poisonmaxdam value
    Assert.assertEquals(0, b.read31u(0)); // poisonlength param bits
    Assert.assertEquals(50, b.read31u(9)); // poisonlength value
    Assert.assertEquals(0x1ff, b.read15u(9)); // stat list finished
    Assert.assertEquals(5, b.bitsRemaining()); // tail end of stream
  }
}