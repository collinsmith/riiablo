package com.riiablo.table;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class TsvParserTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.table.TsvParser", Level.TRACE);
  }

  @Test
  public void monstats_field_names() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    System.out.println(parser.fieldNames());
  }

  @Test
  public void monstats_record_indexes() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    System.out.println(parser.recordNames());
  }

  @Test
  public void monstats_record_names() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    parser.primaryKey("ID");
    System.out.println(parser.recordNames());
  }

  @Test
  public void monstats_skeleton1_tokens() {
    FileHandle handle = Gdx.files.internal("test/monstats.txt");
    TsvParser parser = TsvParser.parse(handle.readBytes());
    System.out.println(parser.tokens(0));
  }
}
