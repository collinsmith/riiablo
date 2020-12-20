package com.riiablo.table;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.table.schema.MonStats;
import com.riiablo.table.schema.Weapons;
import com.riiablo.table.table.MonStatsTable;
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
}
