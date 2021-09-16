package com.riiablo;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

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
  public static void setup() {
    Gdx.app = new HeadlessApplication(new ApplicationAdapter() {});
    final InstallationFinder finder = InstallationFinder.getInstance();
    Riiablo.home = getHomeDir(finder);
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.string = new StringTBLs(Riiablo.mpqs);
    Riiablo.files = new Files();
    Riiablo.logs = new GdxLoggerManager(LogManager.getRegistry());
    Riiablo.logs.getRootLogger().addAppender(new OutputStreamAppender(System.out));
    testHome = getTestDir(finder);
  }

  static FileHandle getHomeDir(InstallationFinder finder) {
    Array<FileHandle> homeDirs = finder.getHomeDirs();
    if (homeDirs.size > 0) {
      return homeDirs.first();
    } else {
      return fail("Unable to locate D2 installation!");
    }
  }

  static FileHandle getTestDir(InstallationFinder finder) {
    Array<FileHandle> testDirs = finder.getTestDirs();
    if (testDirs.size > 0) {
      return testDirs.first();
    } else {
      return fail("Unable to locate D2 test files!");
    }
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
