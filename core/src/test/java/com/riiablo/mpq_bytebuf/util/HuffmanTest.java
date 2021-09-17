package com.riiablo.mpq_bytebuf.util;

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

public class HuffmanTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util.Huffman", Level.TRACE);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/huffman_in.bin,test/huffman_out.bin",
  }, delimiter = ',')
  void inflate(String in, String out) {
    FileHandle huffman_in = Gdx.files.internal(in);
    ByteBuf deflated = Unpooled.buffer(0x1000).writeBytes(huffman_in.readBytes());
    ByteBuf actual = Unpooled.buffer(0x1000);
    new Huffman().inflate(deflated, actual);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle huffman_out = Gdx.files.internal(out);
    ByteBuf expected = Unpooled.wrappedBuffer(huffman_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }

  @Nested
  class reuse {
    final Huffman huffman = new Huffman();

    @ParameterizedTest
    @CsvSource(value = {
        "test/huffman_in.bin,test/huffman_out.bin",
    }, delimiter = ',')
    void inflate(String in, String out) {
      FileHandle huffman_in = Gdx.files.internal(in);
      ByteBuf deflated = Unpooled.buffer(0x1000).writeBytes(huffman_in.readBytes());
      ByteBuf actual = Unpooled.buffer(0x1000);
      huffman.inflate(deflated, actual);
      System.out.println(ByteBufUtil.prettyHexDump(actual));

      FileHandle huffman_out = Gdx.files.internal(out);
      ByteBuf expected = Unpooled.wrappedBuffer(huffman_out.readBytes());
      assertTrue(ByteBufUtil.equals(expected, actual));
    }
  }
}
