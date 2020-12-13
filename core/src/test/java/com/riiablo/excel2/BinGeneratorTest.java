package com.riiablo.excel2;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.excel2.txt.MonStats;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class BinGeneratorTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.excel2.BinGenerator", Level.TRACE);
  }

  @Test
  public void monstats() throws IOException {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    Excel monstats = Excel.loadTxt(new MonStats(), handle);

    BinGenerator generator = new BinGenerator();
    generator.binDir = Gdx.files.absolute(
        "C:\\Users\\csmith\\projects\\libgdx\\riiablo\\assets");
    generator.generateBin(monstats);
  }
}
