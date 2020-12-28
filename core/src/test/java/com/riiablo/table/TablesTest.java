package com.riiablo.table;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.attributes.Stat;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.table.schema.BodyLocs;
import com.riiablo.table.schema.ItemStatCost;
import com.riiablo.table.schema.MonStats;
import com.riiablo.table.schema.Sounds;
import com.riiablo.table.schema.Weapons;
import com.riiablo.table.table.BodyLocsTable;
import com.riiablo.table.table.ItemStatCostTable;
import com.riiablo.table.table.MonStatsTable;
import com.riiablo.table.table.RunesTable;
import com.riiablo.table.table.SoundsTable;
import com.riiablo.table.table.WeaponsTable;

public class TablesTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.table.Tables", Level.TRACE);
  }

  @Test
  public void monstats() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats.parser = null;
    MonStatsTable table = Tables.loadTsv(TableManifest.monstats, parser);
    MonStats record = table.get(0);
    System.out.println(record.Id);
    System.out.println(record.hcIdx);
    System.out.println(Arrays.toString(record.Level));
  }

  @Test
  public void monstats_random_access() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats.parser = null;
    MonStatsTable table = Tables.loadTsv(TableManifest.monstats, parser);
    MonStats record;
    record = table.get(54);
    Assert.assertEquals("NightClan", record.NameStr);
    record = table.get(311);
    Assert.assertEquals("AbyssKnight", record.NameStr);
    record = table.get(411);
    Assert.assertEquals("Charged Bolt Sentry", record.NameStr);
    record = table.get(5);
    Assert.assertEquals("Zombie", record.NameStr);
    record = table.get(700);
    Assert.assertEquals("DoomKnight", record.NameStr);
    record = table.get(578);
    Assert.assertEquals("ReturnedArcher", record.NameStr);
  }

  @Test
  public void weapons_random_access() {
    FileHandle handle = Gdx.files.internal("test/weapons.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.weapons.parser = null;
    WeaponsTable table = Tables.loadTsv(TableManifest.weapons, parser);
    Weapons record;
    record = table.get(54);
    Assert.assertEquals("Brandistock", record.name);
    record = table.get(72);
    Assert.assertEquals("Short Battle Bow", record.name);
    record = table.get(299);
    Assert.assertEquals("Vortex Orb", record.name);
    record = table.get(5);
    Assert.assertEquals("Large Axe", record.name);
    record = table.get(115);
    Assert.assertEquals("Battle Hammer", record.name);
    record = table.get(240);
    Assert.assertEquals("Flying Axe", record.name);
  }

  @Test
  public void runes() {
    LogManager.setLevel("com.riiablo.table.table.RunesTable", Level.TRACE);
    TableManifest.runes.parser = null;
    FileHandle handle = Gdx.files.internal("test/runes.txt");
    RunesTable table = Tables.loadTsv(TableManifest.runes, handle);
    Assert.assertEquals("Ancient's Pledge", table.get(27).Rune_Name);
    Assert.assertEquals("The Beast", table.get(30).Rune_Name);
    Assert.assertEquals("Black", table.get(32).Rune_Name);
    Assert.assertEquals("Bone", table.get(34).Rune_Name);
    Assert.assertEquals("Bramble", table.get(35).Rune_Name);
    Assert.assertEquals("Brand", table.get(36).Rune_Name);
    Assert.assertEquals("Breath of the Dying", table.get(37).Rune_Name);
    Assert.assertEquals("Call to Arms", table.get(39).Rune_Name);
    Assert.assertEquals("Bound by Duty", table.get(40).Rune_Name);
    Assert.assertEquals("Chaos", table.get(42).Rune_Name);
    Assert.assertEquals("Crescent Moon", table.get(43).Rune_Name);
    Assert.assertEquals("Death", table.get(46).Rune_Name);
    Assert.assertEquals("Delirium", table.get(2718).Rune_Name);
    Assert.assertEquals("Destruction", table.get(51).Rune_Name);
    Assert.assertEquals("Doomsayer", table.get(52).Rune_Name);
    Assert.assertEquals("Dragon", table.get(53).Rune_Name);
    Assert.assertEquals("Dream", table.get(55).Rune_Name);
    Assert.assertEquals("Duress", table.get(56).Rune_Name);
    Assert.assertEquals("Edge", table.get(57).Rune_Name);
    Assert.assertEquals("Enigma", table.get(59).Rune_Name);
    Assert.assertEquals("Enlightenment", table.get(60).Rune_Name);
    Assert.assertEquals("Eternity", table.get(62).Rune_Name);
    Assert.assertEquals("Exile's Path", table.get(63).Rune_Name);
    Assert.assertEquals("Faith", table.get(64).Rune_Name);
    Assert.assertEquals("Famine", table.get(65).Rune_Name);
    Assert.assertEquals("Fortitude", table.get(67).Rune_Name);
    Assert.assertEquals("Fury", table.get(70).Rune_Name);
    Assert.assertEquals("Gloom", table.get(71).Rune_Name);
    Assert.assertEquals("Widowmaker", table.get(73).Rune_Name);
    Assert.assertEquals("Hand of Justice", table.get(74).Rune_Name);
    Assert.assertEquals("Harmony", table.get(75).Rune_Name);
    Assert.assertEquals("Heart of the Oak", table.get(77).Rune_Name);
    Assert.assertEquals("Holy Thunder", table.get(80).Rune_Name);
    Assert.assertEquals("Honor", table.get(81).Rune_Name);
    Assert.assertEquals("Ice", table.get(85).Rune_Name);
    Assert.assertEquals("Infinity", table.get(86).Rune_Name);
    Assert.assertEquals("Insight", table.get(88).Rune_Name);
    Assert.assertEquals("King's Grace", table.get(91).Rune_Name);
    Assert.assertEquals("Kingslayer", table.get(92).Rune_Name);
    Assert.assertEquals("Last Wish", table.get(95).Rune_Name);
    Assert.assertEquals("Lawbringer", table.get(97).Rune_Name);
    Assert.assertEquals("Leaf", table.get(98).Rune_Name);
    Assert.assertEquals("Lionheart", table.get(100).Rune_Name);
    Assert.assertEquals("Lore", table.get(101).Rune_Name);
    Assert.assertEquals("Malice", table.get(106).Rune_Name);
    Assert.assertEquals("Melody", table.get(107).Rune_Name);
    Assert.assertEquals("Memory", table.get(108).Rune_Name);
    Assert.assertEquals("Myth", table.get(112).Rune_Name);
    Assert.assertEquals("Nadir", table.get(113).Rune_Name);
    Assert.assertEquals("Oath", table.get(116).Rune_Name);
    Assert.assertEquals("Obedience", table.get(117).Rune_Name);
    Assert.assertEquals("Passion", table.get(120).Rune_Name);
    Assert.assertEquals("Peace", table.get(123).Rune_Name);
    Assert.assertEquals("Winter", table.get(124).Rune_Name);
    Assert.assertEquals("Phoenix", table.get(128).Rune_Name);
    Assert.assertEquals("Pride", table.get(134).Rune_Name);
    Assert.assertEquals("Principle", table.get(135).Rune_Name);
    Assert.assertEquals("Prudence", table.get(137).Rune_Name);
    Assert.assertEquals("Radiance", table.get(141).Rune_Name);
    Assert.assertEquals("Rain", table.get(142).Rune_Name);
    Assert.assertEquals("Rhyme", table.get(145).Rune_Name);
    Assert.assertEquals("Rift", table.get(146).Rune_Name);
    Assert.assertEquals("Sanctuary", table.get(147).Rune_Name);
    Assert.assertEquals("Silence", table.get(151).Rune_Name);
    Assert.assertEquals("Smoke", table.get(153).Rune_Name);
    Assert.assertEquals("Spirit", table.get(155).Rune_Name);
    Assert.assertEquals("Splendor", table.get(156).Rune_Name);
    Assert.assertEquals("Stealth", table.get(158).Rune_Name);
    Assert.assertEquals("Steel", table.get(159).Rune_Name);
    Assert.assertEquals("Stone", table.get(162).Rune_Name);
    Assert.assertEquals("Strength", table.get(164).Rune_Name);
    Assert.assertEquals("Treachery", table.get(173).Rune_Name);
    Assert.assertEquals("Venom", table.get(179).Rune_Name);
    Assert.assertEquals("Wealth", table.get(185).Rune_Name);
    Assert.assertEquals("White", table.get(187).Rune_Name);
    Assert.assertEquals("Wind", table.get(188).Rune_Name);
    Assert.assertEquals("Wrath", table.get(193).Rune_Name);
    Assert.assertEquals("Zephyr", table.get(195).Rune_Name);
  }

  @Test
  public void sounds() {
    LogManager.setLevel("com.riiablo.table.table.SoundsTable", Level.TRACE);
    TableManifest.sounds.parser = null;
    FileHandle handle = Gdx.files.internal("test/sounds.txt");
    SoundsTable table = Tables.loadTsv(TableManifest.sounds, handle);
    for (int i = 0, s = table.parser.parser().numRecords(); i < s; i++) table.get(i);
    for (Sounds record : table) {
      Assert.assertSame(record, table.get(record.Index));
    }
  }

  @Test
  public void bodylocs() {
    LogManager.setLevel("com.riiablo.table.table.BodyLocsTable", Level.TRACE);
    TableManifest.bodylocs.parser = null;
    FileHandle handle = Gdx.files.internal("test/bodylocs.txt");
    BodyLocsTable table = Tables.loadTsv(TableManifest.bodylocs, handle);
    Assert.assertSame(table.get(BodyLocs.RARM), table.get(BodyLocs.RARM2));
    Assert.assertSame(table.get(BodyLocs.LARM), table.get(BodyLocs.LARM2));
  }

  @Test
  public void itemstatcost() {
    LogManager.setLevel("com.riiablo.table.table.ItemStatCostTable", Level.TRACE);
    TableManifest.itemstatcost.parser = null;
    FileHandle handle = Gdx.files.internal("test/itemstatcost.txt");
    ItemStatCostTable table = Tables.loadTsv(TableManifest.itemstatcost, handle);
    Assert.assertNotNull(table.get(Stat.reqstr));
    Assert.assertNotNull(table.get(Stat.reqdex));
    for (int i = 0, s = table.parser.parser().numRecords(); i < s; i++) table.get(i);
    for (ItemStatCost record : table) {
      Assert.assertSame(record, table.get(record.ID));
    }
  }
}
