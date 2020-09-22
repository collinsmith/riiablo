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

public class ADPCMTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util.ADPCM", Level.TRACE);
  }

  @Test
  public void decompress() {
    FileHandle pcm_in = Gdx.files.internal("test/pcm_in.bin");
    ByteBuf actual = Unpooled.buffer(0x1000).writeBytes(pcm_in.readBytes());
    ADPCM.decompress(actual, 1);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle pcm_out = Gdx.files.internal("test/pcm_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(pcm_out.readBytes());
    Assert.assertTrue(ByteBufUtil.equals(expected, actual));
  }
}