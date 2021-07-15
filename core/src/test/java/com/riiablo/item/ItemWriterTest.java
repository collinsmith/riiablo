package com.riiablo.item;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;

import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBLs;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;
import com.riiablo.mpq.MPQFileHandleResolver;

public class ItemWriterTest {
  @BeforeAll
  public static void setup() {
    Gdx.app = new HeadlessApplication(new ApplicationAdapter() {});
    Riiablo.home = Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II");
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.files = new Files();
  }

  @AfterAll
  public static void teardown() {
    Gdx.app.exit();
  }

  private void testItem(byte[] data) {
    ByteInput in = ByteInput.wrap(data);
    ItemReader reader = new ItemReader();
    Item spirit = reader.readItem(in);
    final String firstHexDump = ByteBufUtil.prettyHexDump(in.buffer(), 0, in.buffer().readerIndex());
    System.out.println(firstHexDump);

    ByteOutput out = ByteOutput.wrap(Unpooled.buffer(data.length, data.length));
    ItemWriter writer = new ItemWriter();
    writer.writeItem(spirit, out);
    System.out.println("Actual:");
    System.out.println(ByteBufUtil.prettyHexDump(out.buffer()));

    boolean equal = ByteBufUtil.equals(in.buffer(), 0, out.buffer(), 0, in.buffer().readerIndex());
    if (!equal) {
      System.out.println("Expected:");
      System.out.println(firstHexDump);
    }
    assertTrue(equal);
  }

  @Test
  public void Spirit() {
    testItem(Gdx.files.internal("test/Spirit.d2i").readBytes());
  }

  @Test
  public void Annihilus() {
    testItem(Gdx.files.internal("test/Annihilus.d2i").readBytes());
  }

  @Test
  public void Hunters_Bow_of_Blight() {
    testItem(Gdx.files.internal("test/Hunter's Bow of Blight.d2i").readBytes());
  }

  @Test
  public void Horadric_Malus() {
    testItem(Gdx.files.internal("test/Horadric Malus.d2i").readBytes());
  }

  @Test
  public void Wirts_Leg() {
    testItem(Gdx.files.internal("test/Wirt's Leg.d2i").readBytes());
  }

  @Test
  public void Grief() {
    testItem(Gdx.files.internal("test/Grief.d2i").readBytes());
  }

  @Test
  public void Horadric_Cube() {
    testItem(Gdx.files.internal("test/Horadric Cube.d2i").readBytes());
  }

  @Test
  public void Flawed_Ruby() {
    testItem(Gdx.files.internal("test/Flawed Ruby.d2i").readBytes());
  }

  @Test
  public void Thul_Rune() {
    testItem(Gdx.files.internal("test/Thul Rune.d2i").readBytes());
  }

  @Test
  public void Tome_of_Town_Portal() {
    testItem(Gdx.files.internal("test/Tome of Town Portal.d2i").readBytes());
  }

  @Test
  public void Tome_of_Identify() {
    testItem(Gdx.files.internal("test/Tome of Identify.d2i").readBytes());
  }

  @Test
  public void Rugged_Small_Charm_of_Vita() {
    testItem(Gdx.files.internal("test/Rugged Small Charm of Vita.d2i").readBytes());
  }

  @Test
  public void Aldurs_Advance() {
    testItem(Gdx.files.internal("test/Aldur's Advance.d2i").readBytes());
  }

  @Test
  public void Blood_Eye() {
    testItem(Gdx.files.internal("test/Blood Eye.d2i").readBytes());
  }

  @Test
  public void Vampire_Gaze() {
    testItem(Gdx.files.internal("test/Vampire Gaze.d2i").readBytes());
  }

  @Test
  @Disabled("item is erroneously flagged socketed")
  public void Tome_of_Town_Portal_2() {
    // FIXME: should gracefully handle this as original game does
    testItem(Gdx.files.internal("test/Tome of Town Portal 2.d2i").readBytes());
  }
}
