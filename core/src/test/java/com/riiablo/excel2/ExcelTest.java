package com.riiablo.excel2;

import java.io.IOException;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class ExcelTest extends RiiabloTest {
  @org.junit.BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.excel2", Level.TRACE);
  }

  @Test
  public void parse_monstats_columns() throws IOException {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    Excel.loadTxt(null, handle);
  }
}
