package com.riiablo.map2;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class DS1Test extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.map2", Level.TRACE);
  }

  private DS1 testDs1(FileHandle handle) {
    ByteInput in = ByteInput.wrap(handle.readBytes());
    DS1Reader reader = new DS1Reader();
    return reader.readDs1(handle.name(), in);
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\cott2a.dt1")
  public void cott2a() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/cott2a.ds1"));
    assertEquals(18, ds1.version);
    assertEquals(8, ds1.width);
    assertEquals(8, ds1.height);
    assertEquals(1, ds1.act);
    assertEquals(0, ds1.tagType);

    String[] dependencies = new String[] {
        "\\d2\\data\\global\\tiles\\act1\\outdoors\\cottages.tg1",
        "\\d2\\data\\global\\tiles\\act1\\town\\floor.tg1",
    };
    assertEquals(dependencies.length, ds1.numDependencies);
    assertTrue(ds1.dependencies.length >= dependencies.length);
    for (int i = 0, s = dependencies.length; i < s; i++) {
      assertEquals(dependencies[i], ds1.dependencies[i]);
    }

    assertEquals(3, ds1.numWalls);
    assertEquals(27, ds1.wallRun);
    assertEquals(243, ds1.wallLen);

    assertEquals(1, ds1.numFloors);
    assertEquals(9, ds1.floorRun);
    assertEquals(81, ds1.floorLen);

    assertEquals(1, ds1.numShadows);
    assertEquals(9, ds1.shadowRun);
    assertEquals(81, ds1.shadowLen);

    assertEquals(0, ds1.numTags);
    assertEquals(0, ds1.tagRun);
    assertEquals(0, ds1.tagLen);
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\Uriver.ds1")
  public void URiver() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/Uriver.ds1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\UriverC.ds1")
  public void URiverC() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/UriverC.ds1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\UriverN.ds1")
  public void URiverN() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/UriverN.ds1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\UriverS.ds1")
  public void URiverS() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/UriverS.ds1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\OUTDOORS\\denent2.ds1")
  public void denent2() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/denent2.ds1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\TOWN\\townN1.ds1")
  public void townN1() {
    DS1 ds1 = testDs1(Gdx.files.internal("test/townN1.ds1"));
  }
}
