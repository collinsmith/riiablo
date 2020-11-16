package com.riiablo.mpq_bytebuf.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class DecompressorTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util", Level.TRACE);
  }

  @Test
  public void decompress() {
    FileHandle decompressor_in = Gdx.files.internal("test/decompressor_in.bin");
    ByteBuf actual = Unpooled.buffer(0x1000).writeBytes(decompressor_in.readBytes());
    Decompressor.decompress(actual, 1528, 4096);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle decompressor_out = Gdx.files.internal("test/decompressor_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(decompressor_out.readBytes());
    Assert.assertTrue(ByteBufUtil.equals(expected, actual));
  }

  @Test
  public void decompress_exploder() {
    FileHandle decompressor_in = Gdx.files.internal("test/decompressor_exploder_in.bin");
    ByteBuf actual = Unpooled.buffer(0x1000).writeBytes(decompressor_in.readBytes());
    Decompressor.decompress(actual, 0xaf8, 4096);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle decompressor_out = Gdx.files.internal("test/decompressor_exploder_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(decompressor_out.readBytes());
    Assert.assertTrue(ByteBufUtil.equals(expected, actual));
  }
}