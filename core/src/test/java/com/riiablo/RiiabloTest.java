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
import com.riiablo.util.InstallationFinder;

public class RiiabloTest {
  protected static FileHandle testHome;

  protected static FileHandle testAsset(String filename) {
    return testHome.child(filename);
  }

  @BeforeAll
  public static void setup() throws InstallationFinder.DefaultNotFound {
    Gdx.app = new HeadlessApplication(new ApplicationAdapter() {});
    final InstallationFinder finder = InstallationFinder.getInstance();
    Riiablo.home = finder.defaultHomeDir();
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.files = new Files();
    Riiablo.logs = new GdxLoggerManager(LogManager.getRegistry());
    Riiablo.logs.getRootLogger().addAppender(new OutputStreamAppender(System.out));
    testHome = finder.defaultTestDir();
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
