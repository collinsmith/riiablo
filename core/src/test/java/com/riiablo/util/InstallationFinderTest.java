package com.riiablo.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.SystemUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

public class InstallationFinderTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel(InstallationFinder.class.getName(), Level.TRACE);
  }

  private InstallationFinder getInstance() {
    return InstallationFinder.getInstance();
  }

  @Test
  public void test_getInstallations() {
    if (SystemUtils.IS_OS_WINDOWS) {
      test_getHomeDirs_windows();
    } else if (SystemUtils.IS_OS_LINUX) {
      test_getHomeDirs_linux();
    } else if (SystemUtils.IS_OS_MAC) {
      test_getHomeDirs_mac();
    }
  }

  private void test_getHomeDirs_windows() {
    final InstallationFinder instance = getInstance();
    final Array<FileHandle> handles = instance.getHomeDirs();
    assertNotNull(handles);
    System.out.println("found: " + handles);
  }

  private void test_getHomeDirs_linux() {
  }

  private void test_getHomeDirs_mac() {
  }

  @Test
  public void test_getTestDirs() {
    if (SystemUtils.IS_OS_WINDOWS) {
      test_getHomeDirs_windows();
    } else if (SystemUtils.IS_OS_LINUX) {
      test_getHomeDirs_linux();
    } else if (SystemUtils.IS_OS_MAC) {
      test_getHomeDirs_mac();
    }
  }

  private void test_getTestDirs_windows() {
    final InstallationFinder instance = getInstance();
    final Array<FileHandle> handles = instance.getTestDirs();
    assertNotNull(handles);
    System.out.println("found: " + handles);
  }

  private void test_getTestDirs_linux() {
  }

  private void test_getTestDirs_mac() {
  }

  @Test
  public void test_getSaves() {
    if (SystemUtils.IS_OS_WINDOWS) {
      test_getSaveDirs_windows();
    } else if (SystemUtils.IS_OS_LINUX) {
      test_getSaveDirs_linux();
    } else if (SystemUtils.IS_OS_MAC) {
      test_getSaveDirs_mac();
    }
  }

  private void test_getSaveDirs_windows() {
    final InstallationFinder instance = getInstance();
    final Array<FileHandle> handles = instance.getSaveDirs(null);
    assertNotNull(handles);
    System.out.println("found: " + handles);
  }

  private void test_getSaveDirs_linux() {
  }

  private void test_getSaveDirs_mac() {
  }
}
