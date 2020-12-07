package com.riiablo.excel;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class TxtParserTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.excel.TxtParser", Level.TRACE);
  }

  @Test
  public void parse_monstats_header() throws IOException {
    FileHandle monstats = Gdx.files.internal("test/monstats.txt");
    TxtParser parser = TxtParser.parse(monstats);
    String[] columnNames = parser.getColumnNames();
  }
}
