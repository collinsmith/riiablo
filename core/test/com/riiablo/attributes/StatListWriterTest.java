package com.riiablo.attributes;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;

import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBLs;
import com.riiablo.io.BitInput;
import com.riiablo.io.BitOutput;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq.MPQFileHandleResolver;

public class StatListWriterTest {
  @BeforeClass
  public static void setup() {
    Gdx.app = new HeadlessApplication(new ApplicationAdapter() {});
    Riiablo.home = Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II");
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.files = new Files();
  }

  @AfterClass
  public static void teardown() {
    Gdx.app.exit();
  }

  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.attributes", Level.TRACE);
  }

  private void testItem(byte[] data, long bitsToSkip, int length) {
    final int offset = (int) (bitsToSkip >> 3);
    final int bitOffset = (int) (bitsToSkip & 0x7);
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, offset, length));
    BitInput bitInput = in.unalign().skipBits(bitOffset);
    StatListReader reader = new StatListReader();
    final StatListGetter stats = reader.read(StatList.obtain().buildList(), bitInput);
    System.out.println(ByteBufUtil.prettyHexDump(in.buffer(), 0, in.buffer().readerIndex()));

    ByteOutput out = ByteOutput.wrap(Unpooled.buffer(length, length));
    BitOutput bitOutput = out.unalign().writeRaw(data[offset], bitOffset);
    System.out.println(ByteBufUtil.prettyHexDump(out.buffer()));
    StatListWriter writer = new StatListWriter();
    writer.write(stats, bitOutput);
    bitOutput.flush();
    System.out.println(ByteBufUtil.prettyHexDump(out.buffer()));

    Assert.assertTrue(ByteBufUtil.equals(in.buffer(), 0, out.buffer(), 0, in.buffer().readerIndex()));
  }

  @Test
  public void read_item_grief_stats() {
    testItem(Gdx.files.internal("test/Grief.d2i").readBytes(), 197, 0x12);
  }
}