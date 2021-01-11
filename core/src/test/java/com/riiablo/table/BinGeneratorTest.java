package com.riiablo.table;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class BinGeneratorTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.table.BinGenerator", Level.TRACE);
  }

  @Test
  public void generate() {
    FileHandle dstDir = Gdx.files.absolute("C:\\Users\\csmith\\projects\\libgdx\\riiablo\\assets");
    new BinGenerator().generate(dstDir);
  }
}
