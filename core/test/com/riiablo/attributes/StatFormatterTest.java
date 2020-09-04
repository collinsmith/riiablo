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
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq.MPQFileHandleResolver;

public class StatFormatterTest {
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

  private static StatFormatter newInstance() {
    return new StatFormatter();
  }

  @Test
  public void strength() { // func 1
    StatGetter stat = StatList.obtain().buildList().put(Stat.strength, 5).last();
    Assert.assertEquals("+5 to Strength", newInstance().format(stat, null));
  }

  @Test
  public void toblock() { // func 2
    StatGetter stat = StatList.obtain().buildList().put(Stat.toblock, 25).last();
    Assert.assertEquals("25% Increased Chance of Blocking", newInstance().format(stat, null));
  }

  @Test
  public void item_maxdamage_percent() { // func 3, valmode 0
    StatGetter stat = StatList.obtain().buildList().put(Stat.item_maxdamage_percent, 300).last();
    Assert.assertEquals("Enhanced Maximum Damage", newInstance().format(stat, null));
  }

  @Test
  public void magic_damage_reduction() { // func 3, valmode 2
    StatGetter stat = StatList.obtain().buildList().put(Stat.magic_damage_reduction, 10).last();
    Assert.assertEquals("Magic Damage Reduced by 10", newInstance().format(stat, null));
  }

  @Test
  public void fireresist() { // func 4, valmode 2
    StatGetter stat = StatList.obtain().buildList().put(Stat.fireresist, 45).last();
    Assert.assertEquals("Fire Resist +45%", newInstance().format(stat, null));
  }

  @Test
  public void maxfireresist() { // func 4, valmode 1
    StatGetter stat = StatList.obtain().buildList().put(Stat.maxfireresist, 5).last();
    Assert.assertEquals("+5% to Maximum Fire Resist", newInstance().format(stat, null));
  }

  @Test
  public void item_addclassskills() { // func 13, valmode 1
    StatGetter stat = StatList.obtain().buildList().put(Stat.item_addclassskills, Riiablo.BARBARIAN, 3).last();
    Assert.assertEquals("+3 to Barbarian Skill Levels", newInstance().format(stat, null));
  }

  @Test
  public void item_nonclassskill() { // func 28, valmost 0
    StatGetter stat = StatList.obtain().buildList().put(Stat.item_nonclassskill, 54, 1).last();
    Assert.assertEquals("+1 to Teleport", newInstance().format(stat, null));
  }

  @Test
  public void item_hp_perlevel() { // func 6, valmode 1, op 2, op_param 3
    Attributes attrs = Attributes.wrappedAttributes(StatList.obtain().buildList().put(Stat.level, 5).build());
    StatGetter stat = StatList.obtain().buildList().put(Stat.item_hp_perlevel, 10 << 3).last();
    Assert.assertEquals("+50 to Life (Based on Character Level)", newInstance().format(stat, attrs));
  }
}