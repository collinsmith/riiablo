package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class DecoderTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util", Level.TRACE);
  }

  private Decoder newInstance() {
    return new Decoder();
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/decompressor_in.bin,test/decompressor_out.bin",
  }, delimiter = ',')
  void decode(String in, String out) {
    FileHandle decompressor_in = Gdx.files.internal(in);
    FileHandle decompressor_out = Gdx.files.internal(out);

    ByteBuf compressed = Unpooled.buffer(0x1000).writeBytes(decompressor_in.readBytes());
    ByteBuf scratch = Unpooled.buffer(0x1000);
    ByteBuf actual = Unpooled.buffer(0x1000);
    newInstance().decode(
        compressed,
        actual,
        scratch,
        (int) decompressor_in.length(),
        (int) decompressor_out.length());
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    ByteBuf expected = Unpooled.wrappedBuffer(decompressor_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/decompressor_exploder_in.bin,test/decompressor_exploder_out.bin",
  }, delimiter = ',')
  void decode_explode(String in, String out) {
    FileHandle decompressor_in = Gdx.files.internal(in);
    FileHandle decompressor_out = Gdx.files.internal(out);

    ByteBuf compressed = Unpooled.buffer(0x1000).writeBytes(decompressor_in.readBytes());
    ByteBuf scratch = Unpooled.buffer(0x1000);
    ByteBuf actual = Unpooled.buffer(0x1000);
    newInstance().decode(
        compressed,
        actual,
        scratch,
        (int) decompressor_in.length(),
        (int) decompressor_out.length());
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    ByteBuf expected = Unpooled.wrappedBuffer(decompressor_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }
}
