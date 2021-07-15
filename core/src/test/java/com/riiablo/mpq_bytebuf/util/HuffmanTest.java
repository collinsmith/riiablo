package com.riiablo.mpq_bytebuf.util;

import org.junit.jupiter.api.*;
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

  @Test
  public void decompress() {
    FileHandle huffman_in = Gdx.files.internal("test/huffman_in.bin");
    ByteBuf actual = Unpooled.buffer(0x1000).writeBytes(huffman_in.readBytes());
    new Huffman().decompress(actual);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle huffman_out = Gdx.files.internal("test/huffman_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(huffman_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }
}
