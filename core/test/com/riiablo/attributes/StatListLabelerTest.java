package com.riiablo.attributes;

import io.netty.buffer.Unpooled;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;

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
import static com.riiablo.attributes.StatListFlags.GEM_WEAPON_LIST;
import static com.riiablo.attributes.StatListFlags.ITEM_MAGIC_LIST;
import static com.riiablo.attributes.StatListFlags.ITEM_RUNE_LIST;
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
    final Attributes attrs = Attributes.aggregateAttributes();
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
    Attributes attrs = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33).reset();
    Attributes stats = genItemAttrs(Gdx.files.internal("test/Grief.d2i").readBytes(), 197, 0x12, FLAG_RUNE).reset();

    AttributesUpdater updater = new AttributesUpdater();
    updater.update(stats, FLAG_RUNE, attrs);

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
    System.out.println("----------");

    updater.add(stats, genGemAttrs("r05").list(GEM_WEAPON_LIST), attrs);
    updater.add(stats, genGemAttrs("r03").list(GEM_WEAPON_LIST), attrs);
    updater.add(stats, genGemAttrs("r28").list(GEM_WEAPON_LIST), attrs);
    updater.add(stats, genGemAttrs("r23").list(GEM_WEAPON_LIST), attrs);
    updater.add(stats, genGemAttrs("r08").list(GEM_WEAPON_LIST), attrs);
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
  }

  @Test
  public void Tirant_Annihilus() {
    Attributes attrs = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33).reset();
    Attributes stats = genItemAttrs(Gdx.files.internal("test/Annihilus.d2i").readBytes(), 172, -1, FLAG_MAGIC).reset();

    AttributesUpdater updater = new AttributesUpdater();
    updater.update(stats, FLAG_MAGIC, attrs);

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
  }

  @Test
  public void Tirant_Hunters_Bow_of_Blight() {
    Attributes attrs = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33).reset();
    Attributes stats = genItemAttrs(Gdx.files.internal("test/Hunter's Bow of Blight.d2i").readBytes(), 196, -1, FLAG_MAGIC).reset();

    AttributesUpdater updater = new AttributesUpdater();
    updater.update(stats, FLAG_MAGIC, attrs);

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
  }

  @Test
  public void Tirant_Aldurs_Advance() {
    Attributes attrs = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33).reset();
    Attributes stats = genItemAttrs(Gdx.files.internal("test/Aldur's Advance.d2i").readBytes(), 202, -1, FLAG_MAGIC | getSetItemFlags(4)).reset();

    AttributesUpdater updater = new AttributesUpdater();
    updater.update(stats, FLAG_MAGIC | getSetItemEquippedFlag(2), attrs);

    StatListLabeler labeler = newInstance();
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
  }

  @Test
  public void Tirant_Spirit() {
    Attributes attrs = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33).reset();
    Attributes stats = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE).reset();

    AttributesUpdater updater = new AttributesUpdater();
    updater.update(stats, FLAG_MAGIC | FLAG_RUNE, attrs);

    StatListLabeler labeler = newInstance();
    System.out.println("----------");
    System.out.println(labeler.createDebugLabel(stats.list(ITEM_MAGIC_LIST), attrs));
    System.out.println("----------");
    System.out.println(labeler.createDebugLabel(stats.list(ITEM_RUNE_LIST), attrs));
    System.out.println("----------");
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
    System.out.println("----------");

    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    updater.add(stats, tal.list(GEM_SHIELD_LIST), attrs);
    updater.add(stats, thul.list(GEM_SHIELD_LIST), attrs);
    updater.add(stats, ort.list(GEM_SHIELD_LIST), attrs);
    updater.add(stats, amn.list(GEM_SHIELD_LIST), attrs);
    System.out.println(labeler.createLabel(stats.remaining(), attrs));
  }
}