package com.riiablo.excel2;

import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.Gdx;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class SerializerGeneratorTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.excel2", Level.TRACE);
  }

  @Test
  public void monstats() {
    SerializerGenerator generator = new SerializerGenerator();
    generator.init();
    generator.sourceDir = Gdx.files.absolute(
        "C:\\Users\\csmith\\projects\\libgdx\\riiablo"
            + "\\core\\src\\main\\java\\com\\riiablo\\excel2\\txt");
    generator.serializerDir = Gdx.files.absolute(
        "C:\\Users\\csmith\\projects\\libgdx\\riiablo"
            + "\\core\\src\\main\\java");
    generator.sourcePackage = "com.riiablo.excel2.txt";
    generator.serializerPackage = "com.riiablo.excel2.serializer";
    generator.generateSerializers();
  }
}
