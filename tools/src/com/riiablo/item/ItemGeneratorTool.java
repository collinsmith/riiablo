package com.riiablo.item;

import org.apache.commons.lang3.ObjectUtils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBLs;
import com.riiablo.mpq.MPQFileHandleResolver;

public class ItemGeneratorTool extends ApplicationAdapter {
  private static final String TAG = "ItemGeneratorTool";

  public static void main(String[] args) {
    new HeadlessApplication(new ItemGeneratorTool(args[0]));
  }

  FileHandle home;

  ItemGeneratorTool(String home) {
    this.home = new FileHandle(home);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    Riiablo.home = home = Gdx.files.absolute(home.path());
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.assets = new AssetManager();
    Riiablo.files = new Files(Riiablo.assets);
    Riiablo.string = new StringTBLs(Riiablo.mpqs);

    ItemGenerator generator = new ItemGenerator();

    int numItems = 5;
    String type = "sst";
    for (int i = 0; i < numItems; i++) {
      Item item = generator.generate(type);
      Gdx.app.log(TAG, ObjectUtils.toString(item));
    }

    Gdx.app.exit();
  }

  @Override
  public void dispose() {
    Riiablo.assets.dispose();
  }
}
