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

public class StatListTest {
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

  private static StatList newInstance() {
    return new StatList();
  }

  @Test
  public void strength() {
    StatList stats = newInstance();
    final int list = stats.newList(0);
    final int index = stats.put(0, Stat.strength, 15);
    Assert.assertTrue(stats.contains(list, Stat.strength));
    Assert.assertEquals(0, stats.param(index));
    Assert.assertEquals(15, stats.value(index));
  }

  @Test
  public void strength_builder() {
    StatList stats = newInstance();
    final int list = stats.buildList(0)
        .put(Stat.strength, 15)
        .listIndex();
    final int index = stats.indexOf(list, Stat.strength);
    Assert.assertEquals(0, stats.param(index));
    Assert.assertEquals(15, stats.value(index));
    System.out.println(stats);
  }

  @Test
  public void hitpoints() {
    StatList stats = newInstance();
    final int list = stats.buildList(0)
        .put(Stat.hitpoints, 1 << 8)
        .listIndex();
    final int index = stats.indexOf(list, Stat.hitpoints);
    Assert.assertEquals(0, stats.param(index));
    Assert.assertEquals(1 << 8, stats.value(index));
    Assert.assertEquals(1.0f, stats.asFixed(index), 0f);
    System.out.println(stats);
  }

  @Test
  public void hitpoints_float() {
    StatList stats = newInstance();
    final int list = stats.buildList(0)
        .put(Stat.hitpoints, 1.0f)
        .listIndex();
    final int index = stats.indexOf(list, Stat.hitpoints);
    Assert.assertEquals(0, stats.param(index));
    Assert.assertEquals(1 << 8, stats.value(index));
    Assert.assertEquals(1.0f, stats.asFixed(index), 0f);
    System.out.println(stats);
  }

  @Test
  public void experience() {
    StatList stats = newInstance();
    final int list = stats.buildList(0)
        .put(Stat.experience, 0xFFFFFFFF)
        .listIndex();
    final int index = stats.indexOf(list, Stat.experience);
    Assert.assertEquals(0, stats.param(index));
    Assert.assertEquals(0xFFFFFFFF, stats.value(index));
    Assert.assertEquals(0xFFFFFFFFL, stats.asLong(index));
    System.out.println(stats);
  }

  @Test
  public void experience_long() {
    StatList stats = newInstance();
    final int list = stats.buildList(0)
        .put(Stat.experience, 0xFFFFFFFFL)
        .listIndex();
    final int index = stats.indexOf(list, Stat.experience);
    Assert.assertEquals(0, stats.param(index));
    Assert.assertEquals(0xFFFFFFFF, stats.value(index));
    Assert.assertEquals(0xFFFFFFFFL, stats.asLong(index));
    System.out.println(stats);
  }

  @Test
  public void strength_add() {
    StatList stats = newInstance();
    final int statsList = stats.buildList(0)
        .put(Stat.strength, 15)
        .listIndex();
    final int statsIndex = stats.indexOf(statsList, Stat.strength);
    assert stats.asInt(statsIndex) == 15;
    StatList add = newInstance();
    final int addList = add.buildList(0)
        .put(Stat.strength, 5)
        .listIndex();
    final int addIndex = stats.indexOf(addList, Stat.strength);
    assert add.asInt(addIndex) == 5;
    stats.add(statsList, Stat.strength, add, addList);
    Assert.assertEquals(0, stats.param(statsIndex));
    Assert.assertEquals(20, stats.value(statsIndex));
    Assert.assertEquals(0, add.param(addIndex));
    Assert.assertEquals(5, add.value(addIndex));
    System.out.println(stats);
    System.out.println(add);
  }

  @Test
  public void strength_add_internal() {
    StatList stats = newInstance();
    final int statsList = stats.buildList(0)
        .put(Stat.strength, 15)
        .listIndex();
    final int statsIndex = stats.indexOf(statsList, Stat.strength);
    assert stats.asInt(statsIndex) == 15;
    final int addList = stats.buildList(0)
        .put(Stat.strength, 5)
        .listIndex();
    final int addIndex = stats.indexOf(addList, Stat.strength);
    assert stats.asInt(addIndex) == 5;
    stats.add(statsList, Stat.strength, stats, addList);
    Assert.assertEquals(0, stats.param(statsIndex));
    Assert.assertEquals(20, stats.value(statsIndex));
    Assert.assertEquals(0, stats.param(addIndex));
    Assert.assertEquals(5, stats.value(addIndex));
    System.out.println(stats);
  }
}