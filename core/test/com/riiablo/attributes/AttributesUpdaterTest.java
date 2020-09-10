package com.riiablo.attributes;

import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;

import com.riiablo.CharacterClass;
import com.riiablo.RiiabloTest;
import com.riiablo.io.BitInput;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class AttributesUpdaterTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.attributes", Level.WARN);
    LogManager.setLevel("com.riiablo.attributes.StatList", Level.DEBUG);
    LogManager.setLevel("com.riiablo.attributes.UpdateSequence", Level.TRACE);
    LogManager.setLevel("com.riiablo.attributes.AttributesUpdater", Level.TRACE);
  }

  private static AttributesUpdater newInstance() {
    return new AttributesUpdater();
  }

  private static Attributes genCharacterAttrs(byte[] data, int bytesToSkip, int length) {
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, bytesToSkip, length));
    BitInput bitInput = in.skipBytes(2).unalign(); // skip signature
    StatListReader reader = new StatListReader();
    Attributes attrs = Attributes.obtainLarge();
    reader.read(attrs.base(), bitInput, true);
    return attrs;
  }

  private static Attributes genItemAttrs(byte[] data, long bitsToSkip, int length, int flags) {
    final int offset = (int) (bitsToSkip >> 3);
    final int bitOffset = (int) (bitsToSkip & 0x7);
    if (length < 0) length = data.length - offset;
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, offset, length));
    BitInput bitInput = in.unalign().skipBits(bitOffset);
    StatListReader reader = new StatListReader();
    Attributes attrs = Attributes.obtainStandard();
    reader.read(attrs.list(), bitInput, flags);
    return attrs;
  }

  private Attributes genGemAttrs(String code) {
    com.riiablo.attributes.PropertiesGenerator properties = new com.riiablo.attributes.PropertiesGenerator();
    com.riiablo.attributes.GemGenerator gems = new com.riiablo.attributes.GemGenerator(properties);
    return gems.set(Attributes.obtainCompact(), code);
  }

  private static void dump(Attributes attrs) {
    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("base:");
    for (StatRef stat : attrs.base()) {
      System.out.println(stat.debugString());
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("lists:");
    for (StatListRef list : attrs.list().listIterator()) {
      System.out.println("list:");
      for (StatRef stat : list) {
        System.out.println("  " + stat.debugString());
      }
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("aggregate:");
    for (StatRef stat : attrs.aggregate()) {
      System.out.println(stat.debugString());
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("remaining:");
    for (StatRef stat : attrs.remaining()) {
      System.out.println(stat.debugString());
    }
  }

  @Test
  public void Tirant() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    AttributesUpdater updater = newInstance();
    updater.update(tirant, CharacterClass.SORCERESS.entry())
        .apply();
    dump(tirant);
  }

  @Test
  public void Spirit() {
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE);
    AttributesUpdater updater = newInstance();
    updater.update(spirit, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE, null, null).apply();
    dump(spirit);
  }

  @Test
  public void Spirit_Tal_Thul_Ort_Amn() {
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    AttributesUpdater updater = newInstance();
    updater.update(spirit, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE, null, null)
        .add(tal.list(StatListFlags.GEM_SHIELD_LIST))
        .add(thul.list(StatListFlags.GEM_SHIELD_LIST))
        .add(ort.list(StatListFlags.GEM_SHIELD_LIST))
        .add(amn.list(StatListFlags.GEM_SHIELD_LIST))
        .apply();
    dump(spirit);
  }

  @Test
  public void Tirant_Spirit_Tal_Thul_Ort_Amn() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    AttributesUpdater updater = newInstance();
    updater.update(spirit, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE, null, null)
        .add(tal.list(StatListFlags.GEM_SHIELD_LIST))
        .add(thul.list(StatListFlags.GEM_SHIELD_LIST))
        .add(ort.list(StatListFlags.GEM_SHIELD_LIST))
        .add(amn.list(StatListFlags.GEM_SHIELD_LIST))
        .apply();

    updater.update(tirant, CharacterClass.SORCERESS.entry())
        .add(spirit.remaining())
        .apply();
    dump(tirant);
  }
}