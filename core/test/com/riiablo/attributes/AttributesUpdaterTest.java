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

public class AttributesUpdaterTest {
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
    LogManager.setLevel("com.riiablo.attributes.StatList", Level.DEBUG);
    LogManager.setLevel("com.riiablo.attributes.UpdateSequence", Level.TRACE);
    LogManager.setLevel("com.riiablo.attributes.Updater", Level.TRACE);
  }

  private static AttributesUpdater newInstance() {
    return new AttributesUpdater();
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

  private void dump(Attributes attrs) {
    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("base:");
    for (StatGetter stat : attrs.base()) {
      System.out.println(stat.debugString());
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("lists:");
    for (StatListGetter list : attrs.list().listIterator()) {
      System.out.println("list:");
      for (StatGetter stat : list) {
        System.out.println("  " + stat.debugString());
      }
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("aggregate:");
    for (StatGetter stat : attrs.aggregate()) {
      System.out.println(stat.debugString());
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("remaining:");
    for (StatGetter stat : attrs.remaining()) {
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
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
    AttributesUpdater updater = newInstance();
    updater.update(spirit, FLAG_MAGIC | FLAG_RUNE, null, null).apply();
    dump(spirit);
  }

  @Test
  public void Spirit_Tal_Thul_Ort_Amn() {
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    AttributesUpdater updater = newInstance();
    updater.update(spirit, FLAG_MAGIC | FLAG_RUNE, null, null)
        .add(tal.list(GEM_SHIELD_LIST))
        .add(thul.list(GEM_SHIELD_LIST))
        .add(ort.list(GEM_SHIELD_LIST))
        .add(amn.list(GEM_SHIELD_LIST))
        .apply();
    dump(spirit);
  }

  @Test
  public void Tirant_Spirit() {
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    AttributesUpdater updater = newInstance();
    updater.update(spirit, FLAG_MAGIC | FLAG_RUNE, null, null)
        .add(tal.list(GEM_SHIELD_LIST))
        .add(thul.list(GEM_SHIELD_LIST))
        .add(ort.list(GEM_SHIELD_LIST))
        .add(amn.list(GEM_SHIELD_LIST))
        .apply();

    updater.update(tirant, CharacterClass.SORCERESS.entry())
        .add(spirit.remaining())
        .apply();
    dump(tirant);
  }

  @Test
  public void Tirant_Spirit_2() {
    Attributes parent = Attributes.aggregateAttributes();
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    AttributesUpdater updater = newInstance();
    updater.update(spirit, FLAG_MAGIC | FLAG_RUNE, null, null)
        .add(tal.list(GEM_SHIELD_LIST))
        .add(thul.list(GEM_SHIELD_LIST))
        .add(ort.list(GEM_SHIELD_LIST))
        .add(amn.list(GEM_SHIELD_LIST))
        .apply();

    updater.update(parent, CharacterClass.SORCERESS.entry())
        .add(tirant.base())
        .add(spirit.remaining())
        .apply();
    dump(parent);
  }

  @Test
  public void Tirant_Spirit_3() {
    AttributesUpdater updater = newInstance();
    Attributes parent = Attributes.aggregateAttributes();
    Attributes tirant = genCharacterAttrs(Gdx.files.internal("test/Tirant.d2s").readBytes(), 0x2fd, 0x33);
    UpdateSequence playerSequence = updater.update(parent, CharacterClass.SORCERESS.entry()).add(tirant.base());
    Attributes spirit = genItemAttrs(Gdx.files.internal("test/Spirit.d2i").readBytes(), 216, 0x19, FLAG_MAGIC | FLAG_RUNE);
    Attributes tal = genGemAttrs("r07");
    Attributes thul = genGemAttrs("r10");
    Attributes ort = genGemAttrs("r09");
    Attributes amn = genGemAttrs("r11");
    UpdateSequence itemSequence = updater.update(spirit, FLAG_MAGIC | FLAG_RUNE, null, null);
    updater.update(tal, 1 << GEM_SHIELD_LIST, null, null).apply();
    itemSequence.add(tal.remaining());
    updater.update(thul, 1 << GEM_SHIELD_LIST, null, null).apply();
    itemSequence.add(thul.remaining());
    updater.update(ort, 1 << GEM_SHIELD_LIST, null, null).apply();
    itemSequence.add(ort.remaining());
    updater.update(amn, 1 << GEM_SHIELD_LIST, null, null).apply();
    itemSequence.add(amn.remaining());
    itemSequence.apply();
//    dump(spirit);

    playerSequence.add(spirit.remaining()).apply();
    dump(parent);
  }
}