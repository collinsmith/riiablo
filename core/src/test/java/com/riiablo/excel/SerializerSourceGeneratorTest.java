package com.riiablo.excel;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.riiablo.excel.txt.MonStats;

public class SerializerSourceGeneratorTest {
  @Rule
  public TestName name = new TestName();

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
