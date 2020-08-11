package com.riiablo.io;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class IOTest {
  private static ByteInput newInstance() {
    return ByteInput.wrap(new byte[] {
        (byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE,
        (byte) 0x00, (byte) 0xAD, (byte) 0xBB, (byte) 0xDA
    });
  }

  @Test
  public void readBytes_aligned() {
    final byte[] signature = new byte[]{0x4A, 0x4D};
    ByteInput b = ByteInput.wrap(new byte[]{
        signature[0], signature[1], 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07});
    BitInput bits = b.bitInput(); // creates bit input instance
    assert b.aligned();
    final byte[] bytesRead = b.readBytes(signature.length);
    Assert.assertTrue(b.aligned());
    Assert.assertArrayEquals(signature, bytesRead); // signature
    Assert.assertEquals(signature.length * Byte.SIZE, bits.bitsRead());
    Assert.assertEquals(0, bits.bitsCached());
    Assert.assertEquals(0, bits.cache());
  }

  @Test
  public void read_hunters_bow_of_blight() {
    ByteInput bytes = ByteInput.wrap(new byte[] {
        0x4A, 0x4D, 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07});
    Assert.assertArrayEquals(new byte[] {0x4A, 0x4D}, bytes.readBytes(2)); // signature
    Assert.assertEquals(0x00800010, bytes.read32()); // flags
    Assert.assertEquals(101, bytes.read8u()); // version
    BitInput bits = bytes.unalign();
    bits.skipBits(2); // unknown
    Assert.assertEquals(0, bits.read7u(3)); // location
    Assert.assertEquals(0, bits.read7u(4)); // body location
    Assert.assertEquals(2, bits.read7u(4)); // grid x
    Assert.assertEquals(0, bits.read7u(4)); // grid y
    Assert.assertEquals(1, bits.read7u(3)); // store location
    Assert.assertEquals("hbw ", bits.readString(4)); // code
    Assert.assertEquals(0, bits.read7u(3)); // sockets filled
    Assert.assertEquals(0x2555A813, bits.readRaw(32)); // id
    Assert.assertEquals(6, bits.read7u(7)); // ilvl
    Assert.assertEquals(4, bits.read7u(4)); // quality
    Assert.assertEquals(false, bits.readBoolean()); // picture id
    Assert.assertEquals(false, bits.readBoolean()); // class only
    Assert.assertEquals(0, bits.read15u(11)); // magic prefix
    Assert.assertEquals(737, bits.read15u(11)); // magic suffix
    bits.skipBits(1); // unknown
    Assert.assertEquals(32, bits.read15u(8)); // max durability
    Assert.assertEquals(32, bits.read15u(9)); // durability
    Assert.assertEquals(57, bits.read15u(9)); // poisonmindam
    Assert.assertEquals(0, bits.read31u(0)); // poisonmindam param bits
    Assert.assertEquals(8, bits.read31u(10)); // poisonmindam value
    Assert.assertEquals(0, bits.read31u(0)); // poisonmaxdam param bits
    Assert.assertEquals(8, bits.read31u(10)); // poisonmaxdam value
    Assert.assertEquals(0, bits.read31u(0)); // poisonlength param bits
    Assert.assertEquals(50, bits.read31u(9)); // poisonlength value
    Assert.assertEquals(0x1ff, bits.read15u(9)); // stat list finished
    Assert.assertEquals(5, bits.bitsRemaining()); // tail end of stream
  }

  @Test
  public void write_hunters_bow_of_blight() {
    ByteInput bytes = ByteInput.wrap(new byte[] {
        0x4A, 0x4D, 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07});
    ByteOutput b = ByteOutput.wrap(Unpooled.buffer(256));
    b.writeBytes(new byte[]{0x4A, 0x4D}); // signature
    b.write32(0x00800010); // flags
    b.write8(101); // version
    BitOutput bits = b.unalign();
    bits.skipBits(2); // unknown
    bits.write7u((byte) 0, 3); // location
    bits.write7u((byte) 0, 4); // body location
    bits.write7u((byte) 2, 4); // grid x
    bits.write7u((byte) 0, 4); // grid y
    bits.write7u((byte) 1, 3); // store location
    bits.writeString("hbw ", Byte.SIZE); // code
    bits.write7u((byte) 0, 3); // sockets filled
    bits.writeRaw(0x2555A813, 32); // id
    bits.write7u((byte) 6, 7); // ilvl
    bits.write7u((byte) 4, 4); // quality
    bits.writeBoolean(false); // picture id
    bits.writeBoolean(false); // class only
    bits.write15u((short) 0, 11); // magic prefix
    bits.write15u((short) 737, 11); // magic suffix
    bits.skipBits(1); // unknown
    bits.write15u((short) 32, 8); // max durability
    bits.write15u((short) 32, 9); // durability
    bits.write15u((short) 57, 9); // poisonmindam
    bits.write31u(0, 0); // poisonmindam param bits
    bits.write31u(8, 10); // poisonmindam value
    bits.write31u(0, 0); // poisonmaxdam param bits
    bits.write31u(8, 10); // poisonmaxdam value
    bits.write31u(0, 0); // poisonlength param bits
    bits.write31u(50, 9); // poisonlength value
    bits.write15u((short) 0x1ff, 9); // stat list finished
    bits.flush();
    System.out.println(ByteBufUtil.prettyHexDump(b.buffer()));
    Assert.assertTrue(ByteBufUtil.equals(bytes.buffer(), b.buffer()));
  }
}