package com.riiablo.excel;

import org.junit.jupiter.api.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class SerializerGeneratorTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.excel.SerializerGenerator", Level.TRACE);
  }

  @Test
  public void monstats() {
    FileHandle sourceDir = Gdx.files.absolute(
        "C:\\Users\\csmith\\projects\\libgdx\\riiablo"
            + "\\core\\src\\main\\java\\com\\riiablo\\excel\\txt");
    FileHandle serializerDir = Gdx.files.absolute(
        "C:\\Users\\csmith\\projects\\libgdx\\riiablo"
            + "\\core\\src\\main\\java");
    SerializerGenerator generator = new SerializerGenerator(sourceDir, serializerDir);
    generator.generateSerializers();
  }
}
