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

public class StatListLabelerTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.attributes", Level.WARN);
    LogManager.setLevel("com.riiablo.attributes.StatListLabeler", Level.TRACE);
  }

  private static StatListLabeler newInstance() {
    return new StatListLabeler(new StatFormatter());
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
  public void Tirant_Grief() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes grief = genItemAttrs(Gdx.files.internal("test/Grief.d2i").readBytes(), 197, 0x12, StatListFlags.FLAG_RUNE);
    Attributes eth = genGemAttrs("r05");
    Attributes tir = genGemAttrs("r03");
    Attributes lo = genGemAttrs("r28");
    Attributes mal = genGemAttrs("r23");
    Attributes ral = genGemAttrs("r08");

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(grief, StatListFlags.FLAG_RUNE, tirant, CharacterClass.SORCERESS.entry())
        .add(eth.list(StatListFlags.GEM_SHIELD_LIST))
        .add(tir.list(StatListFlags.GEM_SHIELD_LIST))
        .add(lo.list(StatListFlags.GEM_SHIELD_LIST))
        .add(mal.list(StatListFlags.GEM_SHIELD_LIST))
        .add(ral.list(StatListFlags.GEM_SHIELD_LIST))
        .apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(grief.remaining(), tirant));
  }

  @Test
  public void Tirant_Annihilus() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes annihilus = genItemAttrs(Gdx.files.internal("test/Annihilus.d2i").readBytes(), 172, -1, StatListFlags.FLAG_MAGIC);

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(annihilus, StatListFlags.FLAG_MAGIC, tirant, CharacterClass.SORCERESS.entry()).apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(annihilus.remaining(), tirant));
  }

  @Test
  public void Tirant_Hunters_Bow_of_Blight() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes bow = genItemAttrs(Gdx.files.internal("test/Hunter's Bow of Blight.d2i").readBytes(), 196, -1, StatListFlags.FLAG_MAGIC);

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(bow, StatListFlags.FLAG_MAGIC, tirant, CharacterClass.SORCERESS.entry()).apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(bow.remaining(), tirant));
  }

  @Test
  public void Tirant_Aldurs_Advance() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes aldurs = genItemAttrs(Gdx.files.internal("test/Aldur's Advance.d2i").readBytes(), 202, -1, StatListFlags.FLAG_MAGIC | StatListFlags.getSetItemFlags(4));

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(aldurs, StatListFlags.FLAG_MAGIC | StatListFlags.getSetItemEquippedFlag(2), tirant, CharacterClass.SORCERESS.entry()).apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(aldurs.remaining(), tirant));
  }

  @Test
  public void Tirant_Spirit() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(spirit, StatListFlags.FLAG_MAGIC | StatListFlags.FLAG_RUNE, tirant, CharacterClass.SORCERESS.entry())
        .add(tal.list(StatListFlags.GEM_SHIELD_LIST))
        .add(thul.list(StatListFlags.GEM_SHIELD_LIST))
        .add(ort.list(StatListFlags.GEM_SHIELD_LIST))
        .add(amn.list(StatListFlags.GEM_SHIELD_LIST))
        .apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(spirit.remaining(), tirant));
  }
}