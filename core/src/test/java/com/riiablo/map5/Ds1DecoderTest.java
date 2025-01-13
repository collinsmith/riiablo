package com.riiablo.map5;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import org.apache.commons.io.FilenameUtils;
import org.mockito.Mockito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

class Ds1DecoderTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    Gdx.gl = Mockito.mock();
    LogManager.setLevel("com.riiablo.map5", Level.TRACE);
  }

  private Ds1 testDs1(FileHandle handle) {
    ByteInput in = ByteInput.wrap(handle.readBytes());
    return Ds1Decoder.decode(handle, in);
  }

  static FileHandle internal(String path) {
    String filename = FilenameUtils.getName(path);
    String asset = FilenameUtils.concat("test", filename);
    return Gdx.files.internal(asset);
  }

  /**
   * The following files have 4B at the end of their streams, so far 4x{@code 0x00}
   */
  @DisplayName("V13 Extra Bytes")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "data\\global\\tiles\\ACT1\\OUTDOORS\\URiver.ds1",
      "data\\global\\tiles\\ACT1\\OUTDOORS\\LRiverC.ds1",
      "data\\global\\tiles\\ACT1\\TOWN\\TOWNEW.DS1",
  })
  public void v13(String path) {
    Ds1 ds1 = testDs1(internal(path));
    System.out.println(ds1);
  }

  @DisplayName("Towns")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "data\\global\\tiles\\ACT1\\TOWN\\townE1.ds1",
      "data\\global\\tiles\\ACT1\\TOWN\\townN1.ds1",
      "data\\global\\tiles\\ACT1\\TOWN\\townW1.ds1",
      "data\\global\\tiles\\ACT1\\TOWN\\townS1.ds1",
  })
  public void towns(String path) {
    Ds1 ds1 = testDs1(internal(path));
    System.out.println(ds1);
  }
}
