package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

class MpqFileResolverTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf", Level.WARN);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MpqFileResolver", Level.TRACE);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "data\\global\\MONSTERS\\FA\\cof\\FANUHTH.cof,DATA\\GLOBAL\\MONSTERS\\FA\\COF\\FANUHTH.COF",
      "MONSTERS\\FA\\cof\\FANUHTH.cof,DATA\\GLOBAL\\MONSTERS\\FA\\COF\\FANUHTH.COF",
  }, delimiter = ',')
  void resolve(String in, String out) {
    MpqFileResolver resolver = new MpqFileResolver();
    try {
      MpqFileHandle handle = resolver.resolve(in);
      try {
        assertEquals(out, handle.filename);
      } finally {
        handle.release();
      }
    } finally {
      resolver.dispose();
    }
  }
}
