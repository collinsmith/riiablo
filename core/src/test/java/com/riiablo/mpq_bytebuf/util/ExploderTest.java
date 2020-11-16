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

public class ExploderTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util.Exploder", Level.TRACE);
  }

  @Test
  public void explode() {
    FileHandle exploder_in = Gdx.files.internal("test/exploder_in.bin");
    ByteBuf actual = Unpooled.buffer(0x1000).writeBytes(exploder_in.readBytes());
    Exploder.pkexplode(actual);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle exploder_out = Gdx.files.internal("test/exploder_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(exploder_out.readBytes());
    Assert.assertTrue(ByteBufUtil.equals(expected, actual));
  }
}