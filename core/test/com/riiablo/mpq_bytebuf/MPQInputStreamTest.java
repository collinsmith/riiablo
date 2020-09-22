package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class MPQInputStreamTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf", Level.DEBUG);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MPQInputStream", Level.TRACE);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.util", Level.DEBUG);
  }

  private static MPQ load(String mpq) {
    return MPQ.load(new FileHandle("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\" + mpq + ".mpq"));
  }

  private static void test(MPQ mpq, String filename) {
    FileHandle expectedHandle = Gdx.files.internal("test/" + FilenameUtils.getName(filename));
    final String expected = ByteBufUtil.hexDump(Unpooled.wrappedBuffer(expectedHandle.readBytes()));

    MPQFileHandle actualHandle = new MPQFileHandle(mpq, filename);
    final ByteBuf actualBuffer = actualHandle.readByteBuf();
    final String actual;
    try {
      actual = ByteBufUtil.hexDump(actualBuffer);
    } finally {
      actualBuffer.release();
    }

    boolean equal = actual.equalsIgnoreCase(expected);
    if (!equal) {
      System.out.println("Expected:");
      System.out.println(expected);

      System.out.println("Actual:");
      System.out.println(actual);
    }

    Assert.assertTrue(equal);
  }

  private static void testStream(MPQ mpq, String filename) throws IOException {
    FileHandle expectedHandle = Gdx.files.internal("test/" + FilenameUtils.getName(filename));
    final String expected = ByteBufUtil.hexDump(Unpooled.wrappedBuffer(expectedHandle.readBytes()));

    MPQFileHandle actualHandle = new MPQFileHandle(mpq, filename);
    InputStream in = actualHandle.read();
    final ByteBuf actualBuffer = Unpooled.wrappedBuffer(IOUtils.readFully(in, in.available()));
    final String actual;
    try {
      actual = ByteBufUtil.hexDump(actualBuffer);
    } finally {
      actualBuffer.release();
    }

    boolean equal = actual.equalsIgnoreCase(expected);
    if (!equal) {
      System.out.println("Expected:");
      System.out.println(expected);

      System.out.println("Actual:");
      System.out.println(actual);
    }

    Assert.assertTrue(equal);
  }

  @Test
  public void readBytes() {
    final MPQ d2data = load("d2data");
    test(d2data, "data\\global\\missiles\\blessedhammer.dcc");
  }

  @Test
  public void readBytes_COMPRESSED_ENCRYPTED_KEY_ADJUSTED() {
    final MPQ d2speech = load("d2speech");
    test(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_needhelp.wav");
  }

  @Test
  public void read() throws IOException {
    final MPQ d2data = load("d2data");
    testStream(d2data, "data\\global\\missiles\\blessedhammer.dcc");
  }

  @Test
  public void read_COMPRESSED_ENCRYPTED_KEY_ADJUSTED() throws IOException {
    final MPQ d2speech = load("d2speech");
    testStream(d2speech, "data\\local\\sfx\\Common\\Amazon\\Ama_needhelp.wav");
  }
}