package com.riiablo.asset;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

public class AssetDescTest {
  @ParameterizedTest
  @CsvSource(value = {
      "data\\global\\CHARS\\BA\\LG\\BALGLITTNhth.dcc,com.riiablo.codec.DCC",
      "data\\global\\CHARS\\BA\\TR\\BATRLITTNhth.dcc,com.riiablo.codec.DCC",
      "data\\global\\CHARS\\BA\\LA\\BALALITTNhth.dcc,com.riiablo.codec.DCC",
      "data\\global\\CHARS\\BA\\RA\\BARALITTN1hs.dcc,com.riiablo.codec.DCC",
      "data\\global\\CHARS\\BA\\S1\\BAS1LITTNhth.dcc,com.riiablo.codec.DCC",
      "data\\global\\CHARS\\BA\\S2\\BAS2LITTNhth.dcc,com.riiablo.codec.DCC",
  }, delimiter = ',')
  @SuppressWarnings("unchecked") // expected
  void test(String path, String className) throws ClassNotFoundException {
    Class type = Class.forName(className);
    AssetParams params = new AssetParams();
    AssetDesc t = AssetDesc.of(path, type, params);
    assertEquals(path, t.path());
    assertEquals(type, t.type);
    assertEquals(params, t.params);
  }
}
