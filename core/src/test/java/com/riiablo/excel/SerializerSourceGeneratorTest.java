package com.riiablo.excel;

import org.junit.jupiter.api.*;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;

import com.riiablo.excel.txt.MonStats;

public class SerializerSourceGeneratorTest {
  // @Rule
  // public TestName name = new TestName();
  // @Test void getTestInfo(TestInfo testInfo) {} // will be injected into test case

  private static <E extends Excel.Entry, T extends Excel<E, ?>> JavaFile generateFile(SerializerSourceGenerator generator, Class<T> excelClass, Class<E> entryClass) {
    return generator.configure(excelClass, entryClass).generateFile();
  }

  @Test
  public void monstats() throws IOException {
    SerializerSourceGenerator generator = new SerializerSourceGenerator();
    JavaFile file = generateFile(generator, MonStats.class, MonStats.Entry.class);
    file.writeTo(System.out);
  }
}
