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

public class ADPCMTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util.ADPCM", Level.TRACE);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/pcm_in.bin,test/pcm_out.bin",
  }, delimiter = ',')
  void decode(String in, String out) {
    FileHandle pcm_in = Gdx.files.internal(in);
    ByteBuf encoded = Unpooled.buffer(0x1000).writeBytes(pcm_in.readBytes());
    ByteBuf actual = Unpooled.buffer(0x1000);
    ADPCM.decode(encoded, actual, 1);
    System.out.println(ByteBufUtil.prettyHexDump(actual));

    FileHandle pcm_out = Gdx.files.internal(out);
    ByteBuf expected = Unpooled.wrappedBuffer(pcm_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }
}
