package com.riiablo;

import org.junit.jupiter.api.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.codec.StringTBLs;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.OutputStreamAppender;
import com.riiablo.mpq.MPQFileHandleResolver;

public class RiiabloTest {
  protected static FileHandle testHome;

  protected static FileHandle testAsset(String filename) {
    return testHome.child(filename);
  }

  @BeforeAll
  public static void setup() {
    Gdx.app = new HeadlessApplication(new ApplicationAdapter() {});
    Riiablo.home = Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II");
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.files = new Files();
    Riiablo.logs = new GdxLoggerManager(LogManager.getRegistry());
    Riiablo.logs.getRootLogger().addAppender(new OutputStreamAppender(System.out));
    testHome = Gdx.files.absolute("D:\\mpq");
  }

  @AfterAll
  public static void teardown() {
    Gdx.app.exit();
  }

  public static byte[] toByteArray(final short[] array) {
    final byte[] bytes = new byte[array.length];
    for (int i = 0, s = array.length; i < s; i++) {
      bytes[i] = (byte) array[i];
    }
    return bytes;
  }
}
