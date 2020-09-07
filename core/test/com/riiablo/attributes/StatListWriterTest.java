package com.riiablo.attributes;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
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

import static com.riiablo.attributes.StatListFlags.FLAG_MAGIC;
import static com.riiablo.attributes.StatListFlags.FLAG_NONE;
import static com.riiablo.attributes.StatListFlags.FLAG_RUNE;
import static com.riiablo.attributes.StatListFlags.NUM_ITEM_LISTS;
import static com.riiablo.attributes.StatListFlags.getSetItemFlags;

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

  private void testItem(byte[] data, long bitsToSkip, int length, int flags) {
    final int offset = (int) (bitsToSkip >> 3);
    final int bitOffset = (int) (bitsToSkip & 0x7);
    if (length < 0) length = data.length - offset;
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, offset, length));
    BitInput bitInput = in.unalign().skipBits(bitOffset);
    StatListReader reader = new StatListReader();
    final Attributes attrs = Attributes.aggregateAttributes();
    reader.read(attrs, bitInput, flags, NUM_ITEM_LISTS);
    final String firstHexDump = ByteBufUtil.prettyHexDump(in.buffer(), 0, in.buffer().readerIndex());
    System.out.println(firstHexDump);

    ByteOutput out = ByteOutput.wrap(Unpooled.buffer(length, length));
    BitOutput bitOutput = out.unalign().writeRaw(data[offset], bitOffset);
    StatListWriter writer = new StatListWriter();
    writer.write(attrs, bitOutput, flags, NUM_ITEM_LISTS);
    bitOutput.flush();
    System.out.println("Actual:");
    System.out.println(ByteBufUtil.prettyHexDump(out.buffer()));

    boolean equal = ByteBufUtil.equals(in.buffer(), 0, out.buffer(), 0, in.buffer().readerIndex());
    if (!equal) {
      System.out.println("Expected:");
      System.out.println(firstHexDump);
    }
    Assert.assertTrue(equal);
  }

  @Test
  public void Spirit() {
    testItem(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
  }

  @Test
  public void Annihilus() {
    testItem(Gdx.files.internal("test/Annihilus.d2i").readBytes(), 172, -1, FLAG_MAGIC);
  }

  @Test
  public void Hunters_Bow_of_Blight() {
    testItem(Gdx.files.internal("test/Hunter's Bow of Blight.d2i").readBytes(), 196, -1, FLAG_MAGIC);
  }

  @Test
  public void Horadric_Malus() {
    testItem(Gdx.files.internal("test/Horadric Malus.d2i").readBytes(), 186, -1, FLAG_NONE);
  }

  @Test
  public void Wirts_Leg() {
    testItem(Gdx.files.internal("test/Wirt's Leg.d2i").readBytes(), 178, -1, FLAG_NONE);
  }

  @Test
  public void Grief() {
    testItem(Gdx.files.internal("test/Grief.d2i").readBytes(), 197, 0x12, FLAG_RUNE);
  }

  @Test
  public void Horadric_Cube() {
    testItem(Gdx.files.internal("test/Horadric Cube.d2i").readBytes(), 157, -1, FLAG_NONE);
  }

  @Test
  public void Tome_of_Town_Portal() {
    testItem(Gdx.files.internal("test/Tome of Town Portal.d2i").readBytes(), 171, -1, FLAG_MAGIC);
  }

  @Test
  public void Tome_of_Identify() {
    testItem(Gdx.files.internal("test/Tome of Identify.d2i").readBytes(), 171, -1, FLAG_MAGIC);
  }

  @Test
  public void Rugged_Small_Charm_of_Vita() {
    testItem(Gdx.files.internal("test/Rugged Small Charm of Vita.d2i").readBytes(), 182, -1, FLAG_MAGIC);
  }

  @Test
  public void Aldurs_Advance() {
    testItem(Gdx.files.internal("test/Aldur's Advance.d2i").readBytes(), 202, -1, FLAG_MAGIC | getSetItemFlags(4));
  }

  @Test
  public void Blood_Eye() {
    testItem(Gdx.files.internal("test/Blood Eye.d2i").readBytes(), 237, -1, FLAG_MAGIC);
  }

  @Test
  public void Vampire_Gaze() {
    testItem(Gdx.files.internal("test/Vampire Gaze.d2i").readBytes(), 197, -1, FLAG_MAGIC);
  }

  private void testCharacter(byte[] data, int bytesToSkip, int length) {
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, bytesToSkip, length));
    BitInput bitInput = in.skipBytes(2).unalign(); // skip signature
    StatListReader reader = new StatListReader();
    final Attributes attrs = Attributes.aggregateAttributes(true);
    reader.read(attrs, bitInput, true);
    final String firstHexDump = ByteBufUtil.prettyHexDump(in.buffer(), 0, in.buffer().readerIndex());
    System.out.println(firstHexDump);

    ByteOutput out = ByteOutput.wrap(Unpooled.buffer(length, length));
    BitOutput bitOutput = out.writeBytes(Arrays.copyOfRange(data, bytesToSkip, bytesToSkip + 2)).unalign();
    StatListWriter writer = new StatListWriter();
    writer.write(attrs, bitOutput, true);
    bitOutput.flush();
    System.out.println("Actual:");
    System.out.println(ByteBufUtil.prettyHexDump(out.buffer()));

    boolean equal = ByteBufUtil.equals(in.buffer(), 0, out.buffer(), 0, in.buffer().readerIndex());
    if (!equal) {
      System.out.println("Expected:");
      System.out.println(firstHexDump);
    }
    Assert.assertTrue(equal);
  }

  @Test
  public void Tirant() {
    testCharacter(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
  }
}