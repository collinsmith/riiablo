package com.riiablo.io;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

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
    assertTrue(b.aligned());
    assertArrayEquals(signature, bytesRead); // signature
    assertEquals(signature.length * Byte.SIZE, bits.bitsRead());
    assertEquals(0, bits.bitsCached());
    assertEquals(0, bits.cache());
  }

  @Test
  public void read_hunters_bow_of_blight() {
    ByteInput bytes = ByteInput.wrap(new byte[] {
        0x4A, 0x4D, 0x10, 0x00, (byte) 0x80, 0x00, 0x65, 0x00, 0x04,
        (byte) 0x82, 0x26, 0x76, 0x07, (byte) 0x82, 0x09, (byte) 0xD4,
        (byte) 0xAA, 0x12, 0x03, 0x01, (byte) 0x80, 0x70, 0x01, 0x01,
        (byte) 0x91, 0x03, 0x01, 0x04, 0x64, (byte) 0xFC, 0x07});
    assertArrayEquals(new byte[] {0x4A, 0x4D}, bytes.readBytes(2)); // signature
    assertEquals(0x00800010, bytes.read32()); // flags
    assertEquals(101, bytes.read8u()); // version
    BitInput bits = bytes.unalign();
    bits.skipBits(2); // unknown
    assertEquals(0, bits.read7u(3)); // location
    assertEquals(0, bits.read7u(4)); // body location
    assertEquals(2, bits.read7u(4)); // grid x
    assertEquals(0, bits.read7u(4)); // grid y
    assertEquals(1, bits.read7u(3)); // store location
    assertEquals("hbw ", bits.readString(4)); // code
    assertEquals(0, bits.read7u(3)); // sockets filled
    assertEquals(0x2555A813, bits.readRaw(32)); // id
    assertEquals(6, bits.read7u(7)); // ilvl
    assertEquals(4, bits.read7u(4)); // quality
    assertEquals(false, bits.readBoolean()); // picture id
    assertEquals(false, bits.readBoolean()); // class only
    assertEquals(0, bits.read15u(11)); // magic prefix
    assertEquals(737, bits.read15u(11)); // magic suffix
    bits.skipBits(1); // unknown
    assertEquals(32, bits.read15u(8)); // max durability
    assertEquals(32, bits.read15u(9)); // durability
    assertEquals(57, bits.read15u(9)); // poisonmindam
    assertEquals(0, bits.read31u(0)); // poisonmindam param bits
    assertEquals(8, bits.read31u(10)); // poisonmindam value
    assertEquals(0, bits.read31u(0)); // poisonmaxdam param bits
    assertEquals(8, bits.read31u(10)); // poisonmaxdam value
    assertEquals(0, bits.read31u(0)); // poisonlength param bits
    assertEquals(50, bits.read31u(9)); // poisonlength value
    assertEquals(0x1ff, bits.read15u(9)); // stat list finished
    assertEquals(5, bits.bitsRemaining()); // tail end of stream
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
    bits.write7u(0, 3); // location
    bits.write7u(0, 4); // body location
    bits.write7u(2, 4); // grid x
    bits.write7u(0, 4); // grid y
    bits.write7u(1, 3); // store location
    bits.writeString("hbw ", Byte.SIZE); // code
    bits.write7u(0, 3); // sockets filled
    bits.writeRaw(0x2555A813, 32); // id
    bits.write7u(6, 7); // ilvl
    bits.write7u(4, 4); // quality
    bits.writeBoolean(false); // picture id
    bits.writeBoolean(false); // class only
    bits.write15u(0, 11); // magic prefix
    bits.write15u(737, 11); // magic suffix
    bits.skipBits(1); // unknown
    bits.write15u(32, 8); // max durability
    bits.write15u(32, 9); // durability
    bits.write15u(57, 9); // poisonmindam
    bits.write31u(0, 0); // poisonmindam param bits
    bits.write31u(8, 10); // poisonmindam value
    bits.write31u(0, 0); // poisonmaxdam param bits
    bits.write31u(8, 10); // poisonmaxdam value
    bits.write31u(0, 0); // poisonlength param bits
    bits.write31u(50, 9); // poisonlength value
    bits.write15u(0x1ff, 9); // stat list finished
    bits.flush();
    assertTrue(ByteBufUtil.equals(bytes.buffer(), b.buffer()));
  }
}
