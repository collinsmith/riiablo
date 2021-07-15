package com.riiablo.excel;

import org.junit.jupiter.api.*;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.excel.txt.MonStats;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class ExcelTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.excel.Excel", Level.TRACE);
  }

  @Test
  public void parse_monstats_columns() throws IOException {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    Excel.loadTxt(new MonStats(), handle);
  }
}
