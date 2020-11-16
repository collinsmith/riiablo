package com.riiablo.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq_bytebuf.MPQFileHandle;
import com.riiablo.mpq_bytebuf.MPQFileHandleResolver;

import org.junit.BeforeClass;
import org.junit.Test;

public class AssetManagerTest extends RiiabloTest {
  @BeforeClass
  public static void before() {
    LogManager.setLevel("com.riiablo.assets", Level.TRACE);
  }

  @Test
  public void init() {
    AssetManager assets = new AssetManager(1);
    MPQFileHandleResolver resolver = new MPQFileHandleResolver(Gdx.files.absolute("C:\\diablo ii"));
    assets.setAdapter(FileHandle.class, new GdxFileHandleAdapter());
    assets.setAdapter(MPQFileHandle.class, new MPQFileHandleAdapter());
    assets.dispose();
  }
}