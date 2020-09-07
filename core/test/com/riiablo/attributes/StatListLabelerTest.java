package com.riiablo.attributes;

import io.netty.buffer.Unpooled;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;

import com.riiablo.CharacterClass;
import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBLs;
import com.riiablo.io.BitInput;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq.MPQFileHandleResolver;

import static com.riiablo.attributes.StatListFlags.FLAG_MAGIC;
import static com.riiablo.attributes.StatListFlags.FLAG_RUNE;
import static com.riiablo.attributes.StatListFlags.GEM_SHIELD_LIST;
import static com.riiablo.attributes.StatListFlags.NUM_ITEM_LISTS;
import static com.riiablo.attributes.StatListFlags.getSetItemEquippedFlag;
import static com.riiablo.attributes.StatListFlags.getSetItemFlags;

public class StatListLabelerTest {
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
    LogManager.setLevel("com.riiablo.attributes", Level.WARN);
    LogManager.setLevel("com.riiablo.attributes.StatListLabeler", Level.TRACE);
  }

  private static StatListLabeler newInstance() {
    return new StatListLabeler(new StatFormatter());
  }

  private Attributes genCharacterAttrs(byte[] data, int bytesToSkip, int length) {
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, bytesToSkip, length));
    BitInput bitInput = in.skipBytes(2).unalign(); // skip signature
    StatListReader reader = new StatListReader();
    final Attributes attrs = Attributes.aggregateAttributes(true);
    reader.read(attrs, bitInput, true);
    return attrs;
  }

  private Attributes genItemAttrs(byte[] data, long bitsToSkip, int length, int flags) {
    final int offset = (int) (bitsToSkip >> 3);
    final int bitOffset = (int) (bitsToSkip & 0x7);
    if (length < 0) length = data.length - offset;
    ByteInput in = ByteInput.wrap(Unpooled.wrappedBuffer(data, offset, length));
    BitInput bitInput = in.unalign().skipBits(bitOffset);
    StatListReader reader = new StatListReader();
    final Attributes attrs = Attributes.aggregateAttributes();
    reader.read(attrs, bitInput, flags, NUM_ITEM_LISTS);
    return attrs;
  }

  private Attributes genGemAttrs(String code) {
    PropertiesGenerator properties = new PropertiesGenerator();
    GemGenerator gems = new GemGenerator(properties);
    return gems.set(Attributes.gemAttributes(), code);
  }

  @Test
  public void Tirant_Grief() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes grief = genItemAttrs(Gdx.files.internal("test/Grief.d2i").readBytes(), 197, 0x12, FLAG_RUNE);
    Attributes eth = genGemAttrs("r05");
    Attributes tir = genGemAttrs("r03");
    Attributes lo = genGemAttrs("r28");
    Attributes mal = genGemAttrs("r23");
    Attributes ral = genGemAttrs("r08");

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(grief, FLAG_RUNE, tirant, CharacterClass.SORCERESS.entry())
        .add(eth.list(GEM_SHIELD_LIST))
        .add(tir.list(GEM_SHIELD_LIST))
        .add(lo.list(GEM_SHIELD_LIST))
        .add(mal.list(GEM_SHIELD_LIST))
        .add(ral.list(GEM_SHIELD_LIST))
        .apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(grief.remaining(), tirant));
  }

  @Test
  public void Tirant_Annihilus() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes annihilus = genItemAttrs(Gdx.files.internal("test/Annihilus.d2i").readBytes(), 172, -1, FLAG_MAGIC);

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(annihilus, FLAG_MAGIC, tirant, CharacterClass.SORCERESS.entry()).apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(annihilus.remaining(), tirant));
  }

  @Test
  public void Tirant_Hunters_Bow_of_Blight() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes bow = genItemAttrs(Gdx.files.internal("test/Hunter's Bow of Blight.d2i").readBytes(), 196, -1, FLAG_MAGIC);

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(bow, FLAG_MAGIC, tirant, CharacterClass.SORCERESS.entry()).apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(bow.remaining(), tirant));
  }

  @Test
  public void Tirant_Aldurs_Advance() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes aldurs = genItemAttrs(Gdx.files.internal("test/Aldur's Advance.d2i").readBytes(), 202, -1, FLAG_MAGIC | getSetItemFlags(4));

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(aldurs, FLAG_MAGIC | getSetItemEquippedFlag(2), tirant, CharacterClass.SORCERESS.entry()).apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(aldurs.remaining(), tirant));
  }

  @Test
  public void Tirant_Spirit() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");

    tirant.reset(); // must be called to copy base into agg
    AttributesUpdater updater = new AttributesUpdater();
    updater.update(spirit, FLAG_MAGIC | FLAG_RUNE, tirant, CharacterClass.SORCERESS.entry())
        .add(tal.list(GEM_SHIELD_LIST))
        .add(thul.list(GEM_SHIELD_LIST))
        .add(ort.list(GEM_SHIELD_LIST))
        .add(amn.list(GEM_SHIELD_LIST))
        .apply();

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(spirit.remaining(), tirant));
  }
}