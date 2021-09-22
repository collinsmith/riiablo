package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

class MpqFileResolverTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf", Level.WARN);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MpqFileResolver", Level.TRACE);
  }
}
