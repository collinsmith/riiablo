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

public class ExploderTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util.Exploder", Level.TRACE);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/exploder_in.bin,test/exploder_out.bin",
  }, delimiter = ',')
  void explode(String in, String out) {
    FileHandle exploder_in = Gdx.files.internal(in);
    ByteBuf imploded = Unpooled.buffer(0x1000).writeBytes(exploder_in.readBytes());
    ByteBuf actual = Unpooled.buffer(0x1000);
    Exploder.explode(imploded, actual);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle exploder_out = Gdx.files.internal(out);
    ByteBuf expected = Unpooled.wrappedBuffer(exploder_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }
}
