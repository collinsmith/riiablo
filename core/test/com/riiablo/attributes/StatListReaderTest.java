package com.riiablo.attributes;

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
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq.MPQFileHandleResolver;

public class StatListReaderTest {
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

  @Test
  public void read_char_stats() {
    final byte[] bytes = {
        (byte) 0x00, (byte) 0xD4, (byte) 0x08, (byte) 0x30, (byte) 0x82,
        (byte) 0x80, (byte) 0x0C, (byte) 0x06, (byte) 0xD8, (byte) 0x65,
        (byte) 0x80, (byte) 0x7B, (byte) 0xDA, (byte) 0x1C, (byte) 0x00,
        (byte) 0xA0, (byte) 0x1C, (byte) 0x08, (byte) 0x2A, (byte) 0xC3,
        (byte) 0x45, (byte) 0x02, (byte) 0x00, (byte) 0x6A, (byte) 0xA0,
        (byte) 0xC0, (byte) 0xFD, (byte) 0x94, (byte) 0x2C, (byte) 0x00,
        (byte) 0x70, (byte) 0x10, (byte) 0x0C, (byte) 0xB4, (byte) 0x0D,
        (byte) 0x98, (byte) 0x73, (byte) 0x78, (byte) 0xCE, (byte) 0x1E,
        (byte) 0xC4, (byte) 0x6C, (byte) 0x25, (byte) 0xF8, (byte) 0x0F
    };

    final BitInput bits = ByteInput.wrap(bytes).unalign();
    final Attributes attrs = Attributes.aggregateAttributes();
    final StatListReader reader = new StatListReader();
    reader.read(attrs.base().buildList(), bits);
  }

  @Test
  public void read_item_grief_stats() {
    final byte[] bytes = {
        (byte) 0x4A, (byte) 0x4D, (byte) 0x10, (byte) 0x08, (byte) 0x80,
        (byte) 0x04, (byte) 0x64, (byte) 0x00, (byte) 0x10, (byte) 0x72,
        (byte) 0x33, (byte) 0x26, (byte) 0x07, (byte) 0xD2, (byte) 0x0A,
        (byte) 0x0C, (byte) 0x57, (byte) 0x3B, (byte) 0xED, (byte) 0x90,
        (byte) 0x24, (byte) 0x28, (byte) 0x00, (byte) 0xF5, (byte) 0xDF,
        (byte) 0xCA, (byte) 0xA2, (byte) 0xCB, (byte) 0xEC, (byte) 0x4D,
        (byte) 0xE2, (byte) 0x39, (byte) 0x8D, (byte) 0x3D, (byte) 0x16,
        (byte) 0x8D, (byte) 0xE6, (byte) 0x3D, (byte) 0x50, (byte) 0x2F,
        (byte) 0xFE, (byte) 0x03, (byte) 0x4A, (byte) 0x4D, (byte) 0x10,
        (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0x64, (byte) 0x18,
        (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x53, (byte) 0x03,
        (byte) 0x02, (byte) 0x4A, (byte) 0x4D, (byte) 0x10, (byte) 0x00,
        (byte) 0xA0, (byte) 0x00, (byte) 0x64, (byte) 0x18, (byte) 0x02,
        (byte) 0x20, (byte) 0x07, (byte) 0x33, (byte) 0x03, (byte) 0x02,
        (byte) 0x4A, (byte) 0x4D, (byte) 0x10, (byte) 0x00, (byte) 0xA0,
        (byte) 0x00, (byte) 0x64, (byte) 0x18, (byte) 0x04, (byte) 0x20,
        (byte) 0x27, (byte) 0x83, (byte) 0x03, (byte) 0x02, (byte) 0x4A,
        (byte) 0x4D, (byte) 0x10, (byte) 0x00, (byte) 0xA0, (byte) 0x00,
        (byte) 0x64, (byte) 0x18, (byte) 0x06, (byte) 0x20, (byte) 0x27,
        (byte) 0x33, (byte) 0x03, (byte) 0x02, (byte) 0x4A, (byte) 0x4D,
        (byte) 0x10, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0x64,
        (byte) 0x18, (byte) 0x08, (byte) 0x20, (byte) 0x07, (byte) 0x83,
        (byte) 0x03, (byte) 0x02
    };

    final BitInput bits = ByteInput.wrap(bytes).skipBytes(24).unalign().skipBits(5);
    final Attributes attrs = Attributes.aggregateAttributes();
    final StatListReader reader = new StatListReader();
    final StatListGetter stats = reader.read(attrs.base().buildList(), bits);
    Assert.assertTrue(stats.contains(Stat.item_healafterkill));
    Assert.assertEquals(11, stats.get(Stat.item_healafterkill).value1());

    Assert.assertTrue(stats.contains(Stat.item_fasterattackrate));
    Assert.assertEquals(31, stats.get(Stat.item_fasterattackrate).value1());

    Assert.assertTrue(stats.contains(Stat.item_damage_demon_perlevel));
    Assert.assertEquals(15, stats.get(Stat.item_damage_demon_perlevel).value1());

    Assert.assertTrue(stats.contains(Stat.item_normaldamage));
    Assert.assertEquals(373, stats.get(Stat.item_normaldamage).value1());

    Assert.assertTrue(stats.contains(Stat.item_ignoretargetac));
    Assert.assertEquals(1, stats.get(Stat.item_ignoretargetac).value1());

    Assert.assertTrue(stats.contains(Stat.item_skillonhit));
    Assert.assertEquals(15, stats.first(Stat.item_skillonhit).param1());
    Assert.assertEquals(278, stats.first(Stat.item_skillonhit).param2());
    Assert.assertEquals(35, stats.first(Stat.item_skillonhit).value1());

    Assert.assertTrue(stats.contains(Stat.item_damage_demon_perlevel));
    Assert.assertEquals(15, stats.get(Stat.item_damage_demon_perlevel).value1());

    Assert.assertTrue(stats.contains(Stat.passive_pois_pierce));
    Assert.assertEquals(23, stats.get(Stat.passive_pois_pierce).value1());
  }
}