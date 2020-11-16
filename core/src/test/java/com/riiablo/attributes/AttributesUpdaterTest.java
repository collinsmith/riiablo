package com.riiablo.attributes;

import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;

import com.riiablo.CharacterClass;
import com.riiablo.RiiabloTest;
import com.riiablo.codec.excel.CharStats;
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
    PropertiesGenerator properties = new PropertiesGenerator();
    GemGenerator gems = new GemGenerator(properties);
    return gems.set(Attributes.obtainCompact(), code);
  }

  @Test
  public void Tirant() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    AttributesUpdater updater = newInstance();
    updater.update(tirant, CharacterClass.SORCERESS.entry())
        .apply();
    System.out.println(tirant.dump());
  }

  @Test
  public void Spirit() {
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE);
    AttributesUpdater updater = newInstance();
    updater.update(spirit, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE, null, null).apply();
    System.out.println(spirit.dump());
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
    System.out.println(spirit.dump());
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
    System.out.println(tirant.dump());
  }

  @Test
  public void Tirant_Spirit_Tal_Thul_Ort_Amn_2() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    AttributesUpdater updater = newInstance();

    final CharStats.Entry sorc = CharacterClass.SORCERESS.entry();
    UpdateSequence tirantUpdate = updater.update(tirant, sorc);

    UpdateSequence spiritUpdate = updater.update(spirit, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE, tirant, sorc);

    updater.update(tal, StatListFlags.GEM_SHIELD_LIST, tirant, sorc).apply();
    spiritUpdate.add(tal.remaining());

    updater.update(thul, StatListFlags.GEM_SHIELD_LIST, tirant, sorc).apply();
    spiritUpdate.add(thul.remaining());

    updater.update(ort, StatListFlags.GEM_SHIELD_LIST, tirant, sorc).apply();
    spiritUpdate.add(ort.remaining());

    updater.update(amn, StatListFlags.GEM_SHIELD_LIST, tirant, sorc).apply();
    spiritUpdate.add(amn.remaining());

    spiritUpdate.apply();

    tirantUpdate.add(spirit.remaining()).apply();
    System.out.println(spirit.dump());
  }
}