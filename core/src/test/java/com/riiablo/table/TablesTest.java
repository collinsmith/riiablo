package com.riiablo.table;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloTest;
import com.riiablo.attributes.Stat;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.table.schema.BodyLocs;
import com.riiablo.table.schema.ItemStatCost;
import com.riiablo.table.schema.MagicPrefix;
import com.riiablo.table.schema.MagicSuffix;
import com.riiablo.table.schema.MonStats;
import com.riiablo.table.schema.MonStats2;
import com.riiablo.table.schema.RarePrefix;
import com.riiablo.table.schema.RareSuffix;
import com.riiablo.table.schema.Sounds;
import com.riiablo.table.schema.Weapons;
import com.riiablo.table.table.ArmTypeTable;
import com.riiablo.table.table.BodyLocsTable;
import com.riiablo.table.table.CharStatsTable;
import com.riiablo.table.table.ColorsTable;
import com.riiablo.table.table.ItemStatCostTable;
import com.riiablo.table.table.MagicPrefixTable;
import com.riiablo.table.table.MagicSuffixTable;
import com.riiablo.table.table.MonPresetTable;
import com.riiablo.table.table.MonStats2Table;
import com.riiablo.table.table.MonStatsTable;
import com.riiablo.table.table.ObjTable;
import com.riiablo.table.table.RarePrefixTable;
import com.riiablo.table.table.RareSuffixTable;
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
  public void monstats_primary_key() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats.parser = null;
    MonStatsTable table = Tables.loadTsv(TableManifest.monstats, parser);
    Assert.assertNotNull(table.get(54));
    Assert.assertNotNull(table.get("goatman2"));
    Assert.assertEquals(54, table.index("goatman2"));
  }

  @Test
  public void monstats2() {
    FileHandle handle = Gdx.files.internal("test/monstats2.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats2.parser = null;
    MonStats2Table table = Tables.loadTsv(TableManifest.monstats2, parser);
    MonStats2 record = table.get(0);
    System.out.println(record.Id);
    System.out.println(record.pixHeight);
    System.out.println(Arrays.toString(record.Modemv));
  }

  @Test
  public void monstats2_primary_key() {
    FileHandle handle = Gdx.files.internal("test/monstats2.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats2.parser = null;
    MonStats2Table table = Tables.loadTsv(TableManifest.monstats2, parser);
    Assert.assertNotNull(table.get(54));
    Assert.assertNotNull(table.get("goatman2"));
    Assert.assertEquals(54, table.index("goatman2"));
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
  public void weapons_primary_key() {
    FileHandle handle = Gdx.files.internal("test/weapons.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.weapons.parser = null;
    WeaponsTable table = Tables.loadTsv(TableManifest.weapons, parser);
    Assert.assertNotNull(table.get(54));
    Assert.assertNotNull(table.get("BRN"));
    Assert.assertEquals(54, table.index("BRN"));
  }

  @Test
  public void monstats_foreign_key() {
    FileHandle handle2 = Gdx.files.internal("test/monstats2.txt");
    TsvParser parser2 = TsvParser.parse(handle2.readBytes());
    TableManifest.monstats2.parser = null;
    MonStats2Table table2 = Tables.loadTsv(TableManifest.monstats2, parser2);
    Assert.assertNotNull(table2.get("SKELETON1"));

    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.monstats.parser = null;
    MonStatsTable table = Tables.loadTsv(TableManifest.monstats, parser);

    MonStats skeleton1 = table.get(0);
    Assert.assertNotNull(skeleton1.monstats2);
    Assert.assertSame(table2.get("SKELETON1"), skeleton1.monstats2);
  }

  @Test
  public void weapons_superclass_access() {
    FileHandle handle = Gdx.files.internal("test/weapons.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    TableManifest.weapons.parser = null;
    WeaponsTable table = Tables.loadTsv(TableManifest.weapons, parser);
    Weapons record;
    record = table.get(54);
    Assert.assertEquals("Brandistock", record.name);
    Assert.assertEquals("spea", record.type);
    Assert.assertEquals("BRN", record.code);
    Assert.assertEquals("2ht", record.wclass);
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

  @Test
  public void monpreset() {
    LogManager.setLevel("com.riiablo.table.table.MonPresetTable", Level.TRACE);
    TableManifest.monpreset.parser = null;
    FileHandle handle = Gdx.files.internal("test/monpreset.txt");
    MonPresetTable table = Tables.loadTsv(TableManifest.monpreset, handle);
    for (int i = 0, s = table.parser.parser().numRecords(); i < s; i++) table.get(i);
    Assert.assertEquals(47, table.getSize(1));
    Assert.assertEquals("gheed", table.getPlace(1, 0));
    Assert.assertEquals("cain1", table.getPlace(1, 1));
    Assert.assertEquals("The Cow King", table.getPlace(1, 45));
    Assert.assertEquals("Corpsefire", table.getPlace(1, 46));
    Assert.assertEquals(59, table.getSize(2));
    Assert.assertEquals("warriv2", table.getPlace(2, 0));
    Assert.assertEquals("skeleton5", table.getPlace(2, 58));
    Assert.assertEquals(39, table.getSize(3));
    Assert.assertEquals("cain3", table.getPlace(3, 0));
    Assert.assertEquals("Maffer Dragonhand", table.getPlace(3, 38));
    Assert.assertEquals(28, table.getSize(4));
    Assert.assertEquals("place_champion", table.getPlace(4, 0));
    Assert.assertEquals("The Feature Creep", table.getPlace(4, 27));
    Assert.assertEquals(56, table.getSize(5));
    Assert.assertEquals("larzuk", table.getPlace(5, 0));
    Assert.assertEquals("place_nothing", table.getPlace(5, 55));
  }

  @Test
  public void obj() {
    LogManager.setLevel("com.riiablo.table.table.ObjTable", Level.TRACE);
    TableManifest.obj.parser = null;
    FileHandle handle = Gdx.files.internal("test/obj.txt");
    ObjTable table = Tables.loadTsv(TableManifest.obj, handle);
    for (int i = 0, s = table.parser.parser().numRecords(); i < s; i++) table.get(i);
    Assert.assertEquals(150, table.getSize(1));
    Assert.assertEquals(12, table.getObjectId(1, 0)); // RogueFountain
    Assert.assertEquals(37, table.getObjectId(1, 1)); // Torch1 Tiki
    Assert.assertEquals(0, table.getObjectId(1, 113)); // 0
    Assert.assertEquals(8, table.getObjectId(1, 114)); // Myhrginoc's Book of Lore
    Assert.assertEquals(150, table.getSize(2));
    Assert.assertEquals(74, table.getObjectId(2, 0)); // Trap Door
    Assert.assertEquals(402, table.getObjectId(2, 134)); // waypoint valleywaypoint
    Assert.assertEquals(150, table.getSize(3));
    Assert.assertEquals(117, table.getObjectId(3, 0)); // jungle torch
    Assert.assertEquals(406, table.getObjectId(3, 115)); // khalim chest
    Assert.assertEquals(150, table.getSize(4));
    Assert.assertEquals(238, table.getObjectId(4, 0)); // waypointh
    Assert.assertEquals(409, table.getObjectId(4, 65)); // fortress brazier #2
    Assert.assertEquals(150, table.getSize(5));
    Assert.assertEquals(452, table.getObjectId(5, 0)); // banner 1
    Assert.assertEquals(397, table.getObjectId(5, 149)); // chest
  }

  @Test
  public void magicprefix() {
    LogManager.setLevel("com.riiablo.table.table.MagicPrefixTable", Level.TRACE);
    TableManifest.magicprefix.parser = null;
    FileHandle handle = Gdx.files.internal("test/magicprefix.txt");
    MagicPrefixTable table = Tables.loadTsv(TableManifest.magicprefix, handle);
    MagicPrefix sturdy = table.get(1);
    Assert.assertEquals("STURDY", sturdy.name);
    Assert.assertEquals(0, sturdy.version);
    Assert.assertEquals(4, sturdy.level);
    Assert.assertEquals(3, sturdy.levelreq);

    Assert.assertEquals("STURDY", table.get(1).name);
    Assert.assertEquals("VICIOUS", table.get(14).name);
    Assert.assertEquals("LORD'S", table.get(39).name);
    Assert.assertEquals("STALWART", table.get(132).name);
    Assert.assertEquals("CRUEL", table.get(668).name);
  }

  @Test
  public void magicsuffix() {
    LogManager.setLevel("com.riiablo.table.table.MagicSuffixTable", Level.TRACE);
    TableManifest.magicsuffix.parser = null;
    FileHandle handle = Gdx.files.internal("test/magicsuffix.txt");
    MagicSuffixTable table = Tables.loadTsv(TableManifest.magicsuffix, handle);
    MagicSuffix of_health = table.get(0);
    Assert.assertEquals("OF HEALTH", of_health.name);
    Assert.assertEquals(0, of_health.version);
    Assert.assertEquals(7, of_health.level);
    Assert.assertEquals(5, of_health.levelreq);

    Assert.assertEquals("OF HEALTH", table.get(0).name);
    Assert.assertEquals("OF SPIKES", table.get(14).name);
    Assert.assertEquals("OF GORE", table.get(39).name);
    Assert.assertEquals("OF AMIANTHUS", table.get(132).name);
    Assert.assertEquals("OF THE VAMPIRE", table.get(746).name);
  }

  @Test
  public void rareprefix() {
    LogManager.setLevel("com.riiablo.table.table.RarePrefixTable", Level.TRACE);
    TableManifest.rareprefix.parser = null;
    FileHandle handle = Gdx.files.internal("test/rareprefix.txt");
    RarePrefixTable table = Tables.loadTsv(TableManifest.rareprefix, handle);
    RarePrefix beast = table.get(0);
    Assert.assertEquals("BEAST", beast.name);
    Assert.assertEquals(0, beast.version);
    Assert.assertEquals("armo", beast.itype1);
    Assert.assertEquals("weap", beast.itype2);

    Assert.assertEquals("BEAST", table.get(0).name);
    Assert.assertEquals("RUNE", table.get(14).name);
    Assert.assertEquals("ENTROPY", table.get(39).name);
    Assert.assertEquals("CORRUPTION", table.get(45).name);
  }

  @Test
  public void raresuffix() {
    LogManager.setLevel("com.riiablo.table.table.RareSuffixTable", Level.TRACE);
    TableManifest.raresuffix.parser = null;
    FileHandle handle = Gdx.files.internal("test/raresuffix.txt");
    RareSuffixTable table = Tables.loadTsv(TableManifest.raresuffix, handle);
    RareSuffix bite = table.get(0);
    Assert.assertEquals("BITE", bite.name);
    Assert.assertEquals(0, bite.version);
    Assert.assertEquals("swor", bite.itype1);
    Assert.assertEquals("knif", bite.itype2);
    Assert.assertEquals("spea", bite.itype3);
    Assert.assertEquals("pole", bite.itype4);
    Assert.assertEquals("axe", bite.itype5);
    Assert.assertEquals("h2h", bite.itype6);

    Assert.assertEquals("BITE", table.get(0).name);
    Assert.assertEquals("REND", table.get(14).name);
    Assert.assertEquals("NEEDLE", table.get(39).name);
    Assert.assertEquals("TRAMPLE", table.get(106).name);
    Assert.assertEquals("FLANGE", table.get(154).name);
  }

  @Test
  public void armtype() {
    LogManager.setLevel("com.riiablo.table.table.ArmTypeTable", Level.TRACE);
    TableManifest.armtype.parser = null;
    FileHandle handle = Gdx.files.internal("test/armtype.txt");
    ArmTypeTable table = Tables.loadTsv(TableManifest.armtype, handle);
    Assert.assertEquals("LIT", table.get(0).Token);
    Assert.assertEquals("MED", table.get(1).Token);
    Assert.assertEquals("HVY", table.get(2).Token);
  }

  @Test
  public void charstats() {
    LogManager.setLevel("com.riiablo.table.table.CharStatsTable", Level.TRACE);
    TableManifest.charstats.parser = null;
    FileHandle handle = Gdx.files.internal("test/charstats.txt");
    CharStatsTable table = Tables.loadTsv(TableManifest.charstats, handle);
    Assert.assertEquals("AMAZON", table.get(Riiablo.AMAZON)._class);
    Assert.assertEquals(20, table.get(Riiablo.AMAZON).str);
    Assert.assertEquals("SORCERESS", table.get(Riiablo.SORCERESS)._class);
    Assert.assertEquals(10, table.get(Riiablo.SORCERESS).str);
    Assert.assertEquals("NECROMANCER", table.get(Riiablo.NECROMANCER)._class);
    Assert.assertEquals(15, table.get(Riiablo.NECROMANCER).str);
    Assert.assertEquals("PALADIN", table.get(Riiablo.PALADIN)._class);
    Assert.assertEquals(25, table.get(Riiablo.PALADIN).str);
    Assert.assertEquals("BARBARIAN", table.get(Riiablo.BARBARIAN)._class);
    Assert.assertEquals(30, table.get(Riiablo.BARBARIAN).str);
    Assert.assertEquals("DRUID", table.get(Riiablo.DRUID)._class);
    Assert.assertEquals(15, table.get(Riiablo.DRUID).str);
    Assert.assertEquals("ASSASSIN", table.get(Riiablo.ASSASSIN)._class);
    Assert.assertEquals(20, table.get(Riiablo.ASSASSIN).str);
  }

  @Test
  public void colors() {
    LogManager.setLevel("com.riiablo.table.table.ColorsTable", Level.TRACE);
    TableManifest.colors.parser = null;
    FileHandle handle = Gdx.files.internal("test/colors.txt");
    ColorsTable table = Tables.loadTsv(TableManifest.colors, handle);
    Assert.assertEquals("WHIT", table.get(0).Code);
    Assert.assertEquals("LGRY", table.get(1).Code);
    Assert.assertEquals("DGRY", table.get(2).Code);
    Assert.assertEquals("BLAC", table.get(3).Code);
    Assert.assertEquals("LBLU", table.get(4).Code);
    Assert.assertEquals("DBLU", table.get(5).Code);
    Assert.assertEquals("CBLU", table.get(6).Code);
    Assert.assertEquals("LRED", table.get(7).Code);
    Assert.assertEquals("DRED", table.get(8).Code);
    Assert.assertEquals("CRED", table.get(9).Code);
    Assert.assertEquals("LGRN", table.get(10).Code);
    Assert.assertEquals("DGRN", table.get(11).Code);
    Assert.assertEquals("CGRN", table.get(12).Code);
    Assert.assertEquals("LYEL", table.get(13).Code);
    Assert.assertEquals("DYEL", table.get(14).Code);
    Assert.assertEquals("LGLD", table.get(15).Code);
  }
}
