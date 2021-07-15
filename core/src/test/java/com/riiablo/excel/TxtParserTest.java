package com.riiablo.excel;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class TxtParserTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.excel.TxtParser", Level.TRACE);
  }

  @Test
  public void parse_monstats_columns() throws IOException {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TxtParser.parse(handle.read());
  }

  @Test
  public void parse_monstats_first() throws IOException {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TxtParser parser = TxtParser.parse(handle.read());
    parser.cacheLine();
  }

  @Test
  public void parse_monstats_first_2() throws IOException {
    LogManager.setLevel("com.riiablo.excel", Level.DEBUG);
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TxtParser parser = TxtParser.parse(handle.read());
    parser.cacheLine();
    parser.cacheLine();
    LogManager.setLevel("com.riiablo.excel", Level.TRACE);
  }

  @Test
  public void parse_monstats_parseInt() throws IOException {
    LogManager.setLevel("com.riiablo.excel", Level.DEBUG);
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TxtParser parser = TxtParser.parse(handle.read());
    parser.cacheLine();
    assertEquals(0, parser.parseInt(1, -1));
    LogManager.setLevel("com.riiablo.excel", Level.TRACE);
  }
}
