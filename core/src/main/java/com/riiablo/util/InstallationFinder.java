package com.riiablo.util;

import io.netty.util.internal.SystemPropertyUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.save.D2S;

public abstract class InstallationFinder {
  private static final Logger log = LogManager.getLogger(InstallationFinder.class);

  private static final String SAVE = "Save";
  private static final Array<FileHandle> EMPTY_ARRAY = new Array<>(0);

  private InstallationFinder() {}

  static final String[] FOLDERS = {
      "diablo", "riiablo", "Diablo II"
  };

  public static boolean isD2Home(FileHandle handle) {
    if (handle == null) return false;
    return handle.child("d2data.mpq").exists();
  }

  public static boolean isD2TestHome(FileHandle handle) {
    if (handle == null) return false;
    if (!handle.isDirectory()) return false;
    return handle.child("data").exists();
  }

  public static boolean containsSaves(FileHandle handle) {
    if (handle == null) return false;
    final FileHandle[] children = handle.list();
    for (FileHandle child : children) {
      if (FilenameUtils.isExtension(child.path(), D2S.EXT)) {
        return true;
      }
    }

    return false;
  }

  public static InstallationFinder getInstance() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return new WindowsInstallationFinder();
    } else if (SystemUtils.IS_OS_LINUX) {
      return new LinuxInstallationFinder();
    } else if (SystemUtils.IS_OS_MAC) {
      return new MacInstallationFinder();
    }

    return new StubbedInstallationFinder();
  }

  public Array<FileHandle> getHomeDirs() {
    return EMPTY_ARRAY;
  }

  public Array<FileHandle> getSaveDirs(FileHandle home) {
    return EMPTY_ARRAY;
  }

  public Array<FileHandle> getTestDirs() {
    return EMPTY_ARRAY;
  }

  public static final class StubbedInstallationFinder extends InstallationFinder {
  }

  public static final class WindowsInstallationFinder extends InstallationFinder {
    private static final String D2_REG_KEY = "Software\\Blizzard Entertainment\\Diablo II";

    private static String getD2RegKey(String valueName, String defaultValue) {
      try {
        return WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, D2_REG_KEY, valueName);
      } catch (Throwable t) {
        log.warn(t.getMessage(), t);
        return defaultValue;
      }
    }

    @Override
    public Array<FileHandle> getHomeDirs() {
      final String D2_HOME = SystemUtils.getEnvironmentVariable("D2_HOME", null);
      final String InstallPath = getD2RegKey("InstallPath", null);
      final String HOMEDRIVE = SystemUtils.getEnvironmentVariable("HOMEDRIVE", "C:");
      final String ProgramFiles = SystemUtils.getEnvironmentVariable("ProgramFiles", "C:/Program Files");
      final String ProgramFiles86 = SystemUtils.getEnvironmentVariable("ProgramFiles(x86)", "C:/Program Files (x86)");

      final Array<String> homeDirs = new Array<>();
      if (D2_HOME != null) homeDirs.add(D2_HOME);
      if (InstallPath != null) homeDirs.add(InstallPath);
      homeDirs.add(SystemUtils.USER_HOME);
      homeDirs.add(SystemUtils.USER_DIR);
      homeDirs.add(HOMEDRIVE);
      homeDirs.add(ProgramFiles);
      homeDirs.add(ProgramFiles86);

      log.trace("resolve() paths: {}", homeDirs);
      Array<FileHandle> result = null;
      for (String path : homeDirs) {
        FileHandle handle = new FileHandle(path);
        log.trace("Trying {}", handle);
        if (isD2Home(handle)) {
          if (result == null) result = new Array<>();
          result.add(handle);
        } else {
          for (String folder : FOLDERS) {
            FileHandle child = handle.child(folder);
            if (InstallationFinder.isD2Home(child)) {
              if (result == null) result = new Array<>();
              result.add(child);
            }
          }
        }
      }

      return result == null ? super.getHomeDirs() : result;
    }

    @Override
    public Array<FileHandle> getTestDirs() {
      final String d2test = SystemPropertyUtil.get("d2test", null);
      final String D2_TEST = SystemUtils.getEnvironmentVariable("D2_TEST", null);

      final Array<String> testDirs = new Array<>();
      if (d2test != null) testDirs.add(d2test);
      if (D2_TEST != null) testDirs.add(D2_TEST);

      log.trace("resolve() paths: {}", testDirs);
      Array<FileHandle> result = null;
      for (String path : testDirs) {
        FileHandle handle = new FileHandle(path);
        log.trace("Trying {}", handle);
        if (isD2TestHome(handle)) {
          if (result == null) result = new Array<>();
          result.add(handle);
        }
      }

      return result == null ? super.getTestDirs() : result;
    }

    @Override
    public Array<FileHandle> getSaveDirs(FileHandle home) {
      final Array<FileHandle> saveDirs = new Array<>();
      final String D2_SAVE = SystemUtils.getEnvironmentVariable("D2_SAVE", null);
      log.trace("D2_SAVE: {}", D2_SAVE);

      final String NewSavePath = getD2RegKey("NewSavePath", null);
      log.trace("NewSavePath: {}", NewSavePath);

      final FileHandle homeSaves = home == null ? null : home.child(SAVE);
      log.trace("homeSaves: {}", homeSaves);

      if (D2_SAVE != null) saveDirs.add(new FileHandle(D2_SAVE));
      if (NewSavePath != null) saveDirs.add(new FileHandle(NewSavePath));
      if (home != null) {
        homeSaves.mkdirs();
        saveDirs.add(homeSaves);
      }
      return saveDirs;
    }
  }

  public static final class LinuxInstallationFinder extends InstallationFinder {
    @Override
    public Array<FileHandle> getHomeDirs() {
      final String D2_HOME = SystemUtils.getEnvironmentVariable("D2_HOME", null);

      final Array<String> homeDirs = new Array<>();
      if (D2_HOME != null) homeDirs.add(D2_HOME);
      homeDirs.add(SystemUtils.USER_HOME);
      homeDirs.add(SystemUtils.USER_DIR);

      log.trace("resolve() paths: {}", homeDirs);
      Array<FileHandle> result = null;
      for (String path : homeDirs) {
        FileHandle handle = new FileHandle(path);
        log.trace("Trying {}", handle);
        if (isD2Home(handle)) {
          if (result == null) result = new Array<>();
          result.add(handle);
        } else {
          for (String folder : FOLDERS) {
            FileHandle child = handle.child(folder);
            if (InstallationFinder.isD2Home(child)) {
              if (result == null) result = new Array<>();
              result.add(child);
            }
          }
        }
      }

      return result == null ? super.getHomeDirs() : result;
    }

    @Override
    public Array<FileHandle> getTestDirs() {
      final String d2test = SystemPropertyUtil.get("d2test", null);
      final String D2_TEST = SystemUtils.getEnvironmentVariable("D2_TEST", null);

      final Array<String> testDirs = new Array<>();
      if (d2test != null) testDirs.add(d2test);
      if (D2_TEST != null) testDirs.add(D2_TEST);

      log.trace("resolve() paths: {}", testDirs);
      Array<FileHandle> result = null;
      for (String path : testDirs) {
        FileHandle handle = new FileHandle(path);
        log.trace("Trying {}", handle);
        if (isD2TestHome(handle)) {
          if (result == null) result = new Array<>();
          result.add(handle);
        }
      }

      return result == null ? super.getTestDirs() : result;
    }

    @Override
    public Array<FileHandle> getSaveDirs(FileHandle home) {
      final Array<FileHandle> saveDirs = new Array<>();
      final String D2_SAVE = SystemUtils.getEnvironmentVariable("D2_SAVE", null);
      log.trace("D2_SAVE: {}", D2_SAVE);

      final FileHandle homeSaves = home == null ? null : home.child(SAVE);
      log.trace("homeSaves: {}", homeSaves);

      if (D2_SAVE != null) saveDirs.add(new FileHandle(D2_SAVE));
      if (home != null) {
        homeSaves.mkdirs();
        saveDirs.add(homeSaves);
      }
      return saveDirs;
    }
  }

  public static final class MacInstallationFinder extends InstallationFinder {
    final LinuxInstallationFinder delegate = new LinuxInstallationFinder();

    @Override
    public Array<FileHandle> getHomeDirs() {
      return delegate.getHomeDirs();
    }

    @Override
    public Array<FileHandle> getSaveDirs(FileHandle home) {
      return delegate.getSaveDirs(home);
    }
  }
}
