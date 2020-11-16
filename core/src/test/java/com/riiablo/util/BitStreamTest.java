package com.riiablo.util;

import org.junit.Assert;
import org.junit.Test;

public class BitStreamTest {
  @Test
  public void no_bits_available_in_empty_stream() {
    BitStream b = BitStream.emptyBitStream();
    Assert.assertEquals(b.bitsAvailable(), 0L);
  }

  @Test
  public void read_0_bits_from_empty_stream() {
    BitStream b = BitStream.emptyBitStream();
    b.readUnsigned(0);
  }

  @Test(expected = BitStream.EndOfStream.class)
  public void read_bits_from_empty_stream_throws_EndOfStream_exception() {
    BitStream b = BitStream.emptyBitStream();
    b.readUnsigned(1);
  }

  @Test
  public void align_byte_when_already_aligned() {
    BitStream b = BitStream.wrap(new byte[] {0x00});
    long before = b.bitsAvailable();
    b.alignToByte();
    long after = b.bitsAvailable();
    Assert.assertEquals(before, after);
  }

  @Test
  public void align_byte_when_unaligned() {
    BitStream b = BitStream.wrap(new byte[]{0x00});
    b.skip(1).alignToByte();
    Assert.assertEquals(0, b.bitsAvailable());
  }

  @Test
  public void read_hunters_bow_of_blight() {
    byte[] bytes = new byte[]{
        0x4A, 0x4D, 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07};
    BitStream b = BitStream.wrap(bytes);
    Assert.assertEquals("JM", b.readString(2)); // signature
    Assert.assertEquals(0x00800010, b.readUnsigned(Integer.SIZE)); // flags
    Assert.assertEquals(101, b.readUnsigned(8)); // version
    b.skip(2); // unknown
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
    b.skip(1); // unknown
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
    Assert.assertEquals(5, b.bitsAvailable()); // tail end of stream
  }

  @Test(expected = IllegalArgumentException.class)
  public void read_u64_throws_IllegalArgumentException() {
    BitStream b = BitStream.emptyBitStream();
    b.readUnsigned(64);
  }

  @Test
  public void read_raw_64_bits() {
    BitStream b = BitStream.wrap(new byte[]{
        0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    Assert.assertEquals(0xEFCDAB89_67452301L, b.readRaw(Long.SIZE));
  }

  @Test
  public void read_raw_64_bits_partial_byte_order() {
    BitStream b = BitStream.wrap(new byte[]{
        0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    Assert.assertEquals(0x0000AB89_67452301L, b.readRaw(48));
    Assert.assertEquals(0x00000000_0000EFCDL, b.readRaw(16));
  }

  @Test
  public void read_raw_64_bits_partial_bit_order() {
    BitStream b = BitStream.wrap(new byte[]{
        0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    Assert.assertEquals(0x000DAB89_67452301L, b.readRaw(52));
    Assert.assertEquals(0x00000000_00000EFCL, b.readRaw(12));
  }

  @Test
  public void read_npc_data() {
    BitStream b = BitStream.wrap(new byte[] {
        0x01, 0x77,
        0x34, 0x00,
        (byte) 0xAC, (byte) 0xAE, (byte) 0xA5, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAC, (byte) 0xBE, (byte) 0xA4, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAE, (byte) 0xAE, (byte) 0xA4, (byte) 0xC9, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xFA, (byte) 0x7B, (byte) 0xE7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0xDA, (byte) 0x79, (byte) 0xC7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0x10, (byte) 0x51, (byte) 0xE3, (byte) 0x18, 0x00, 0x00, 0x00, 0x00});
    Assert.assertArrayEquals(new byte[] {0x01, 0x77}, b.read(2)); // signature
    Assert.assertEquals(52, b.readUnsigned(16)); // size
    Assert.assertEquals(0x00000002_89A5AEACL, b.readRaw(64));
    Assert.assertEquals(0x00000002_89A4BEACL, b.readRaw(64));
    Assert.assertEquals(0x00000002_C9A4AEAEL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18E77BFAL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18C779DAL, b.readRaw(64));
    Assert.assertEquals(0x00000000_18E35110L, b.readRaw(64));
  }

  @Test
  public void read_dcc_stream_sizes() {
    // TODO...
  }

  @Test
  public void read_byte_array() {
    BitStream b = BitStream.wrap(new byte[]{
        0x01, 0x77,
        0x34, 0x00,
        (byte) 0xAC, (byte) 0xAE, (byte) 0xA5, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAC, (byte) 0xBE, (byte) 0xA4, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAE, (byte) 0xAE, (byte) 0xA4, (byte) 0xC9, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xFA, (byte) 0x7B, (byte) 0xE7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0xDA, (byte) 0x79, (byte) 0xC7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0x10, (byte) 0x51, (byte) 0xE3, (byte) 0x18, 0x00, 0x00, 0x00, 0x00});
    byte[] expected = new byte[] {0x01, 0x77, 0x34, 0x00};
    byte[] actual = b.read(expected.length);
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void read_byte_array_when_unaligned() {
    BitStream b = BitStream.wrap(new byte[]{
        0x01, 0x77,
        0x34, 0x00,
        (byte) 0xAC, (byte) 0xAE, (byte) 0xA5, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAC, (byte) 0xBE, (byte) 0xA4, (byte) 0x89, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xAE, (byte) 0xAE, (byte) 0xA4, (byte) 0xC9, 0x02, 0x00, 0x00, 0x00,
        (byte) 0xFA, (byte) 0x7B, (byte) 0xE7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0xDA, (byte) 0x79, (byte) 0xC7, (byte) 0x18, 0x00, 0x00, 0x00, 0x00,
        (byte) 0x10, (byte) 0x51, (byte) 0xE3, (byte) 0x18, 0x00, 0x00, 0x00, 0x00});
    b.skip(1);
    byte[] expected = new byte[] {0x77, 0x34, 0x00, (byte) 0xAC};
    byte[] actual = b.read(expected.length);
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void read_u63() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80});
    Assert.assertEquals(0, b.readU63(63));
  }

  @Test
  public void read_s64() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80});
    Assert.assertEquals(Long.MIN_VALUE, b.readSigned(Long.SIZE));
  }

  @Test
  public void bits_read_increments() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
        (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    Assert.assertEquals(0, b.bitsRead());
    b.readUnsigned(4);
    Assert.assertEquals(4, b.bitsRead());
    b.readUnsigned(8);
    Assert.assertEquals(12, b.bitsRead());
    b.readUnsigned(16);
    Assert.assertEquals(28, b.bitsRead());
  }

  @Test
  public void read_slice_seeks() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
        (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    b.readSlice(31);
    Assert.assertEquals(31, b.bitsRead());
  }

  @Test(expected = BitStream.EndOfStream.class)
  public void read_slice_throws_EndOfStream() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
        (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    BitStream slice = b.readSlice(31);
    slice.readUnsigned(32);
  }

  @Test
  public void read_slice_align_to_align() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA});
    BitStream slice = b.readSlice(32);
    Assert.assertEquals(0, slice.cache());
    Assert.assertEquals(0, slice.bitsCached());
    Assert.assertEquals(0, b.cache());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0xDEADBEEFL, slice.readUnsigned(32));
    Assert.assertEquals(0xDABBAD00L, b.readUnsigned(32));
  }

  @Test
  public void read_slice_align_to_unalign() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA});
    BitStream slice = b.readSlice(28);
    Assert.assertEquals(0, slice.cache());
    Assert.assertEquals(0, slice.bitsCached());
    Assert.assertEquals(0xD, b.cache());
    Assert.assertEquals(4, b.bitsCached());
    Assert.assertEquals(0xDABBAD00DL, b.readUnsigned(36));
    Assert.assertEquals(0xEADBEEFL, slice.readUnsigned(28));
  }

  @Test
  public void read_slice_unalign_to_align() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA});
    BitStream slice = b.skip(4).readSlice(28);
    Assert.assertEquals(0xE, slice.cache());
    Assert.assertEquals(4, slice.bitsCached());
    Assert.assertEquals(0, b.cache());
    Assert.assertEquals(0, b.bitsCached());
    Assert.assertEquals(0xDABBAD00L, b.readUnsigned(32));
    Assert.assertEquals(0xDEADBEEL, slice.readUnsigned(28));
  }

  @Test
  public void read_slice_unalign_to_unalign() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA});
    BitStream slice = b.skip(4).readSlice(32);
    Assert.assertEquals(0xE, slice.cache());
    Assert.assertEquals(4, slice.bitsCached());
    Assert.assertEquals(0, b.cache());
    Assert.assertEquals(4, b.bitsCached());
    Assert.assertEquals(0xDABBAD0L, b.readUnsigned(28));
    Assert.assertEquals(0x0DEADBEEL, slice.readUnsigned(32));
  }

  @Test
  public void read_slice_unalign_to_unalign_complex_single_byte() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA});
    BitStream slice = b.skip(3).readSlice(4);
    Assert.assertEquals(0b11101, slice.cache());
    Assert.assertEquals(5, slice.bitsCached());
    Assert.assertEquals(0b1, b.cache());
    Assert.assertEquals(1, b.bitsCached());
  }

  @Test
  public void read_slice_unalign_to_unalign_complex_multi_byte() {
    BitStream b = BitStream.wrap(new byte[]{
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA});
    BitStream slice = b.skip(3).readSlice(22);
    Assert.assertEquals(0b11101, slice.cache());
    Assert.assertEquals(5, slice.bitsCached());
    Assert.assertEquals(0b1101111, b.cache());
    Assert.assertEquals(7, b.bitsCached());
  }
}