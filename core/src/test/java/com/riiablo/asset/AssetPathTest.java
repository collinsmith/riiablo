package com.riiablo.asset;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

public class AssetPathTest {
  @ParameterizedTest
  @CsvSource(value = {
      "a,a",
      "a,A",
      "A,a",
      "a/a,a/a",
      "a/a,a\\a",
      "a\\a,a/a",
      "a\\a,a\\a",
  }, delimiter = ',')
  void test(String s1, String s2) {
    AssetPath p1 = AssetPath.of(s1);
    AssetPath p2 = AssetPath.of(s2);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }
}
