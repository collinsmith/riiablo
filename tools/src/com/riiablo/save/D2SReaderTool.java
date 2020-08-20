package com.riiablo.save;

import java.util.Arrays;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.StringTBLs;
import com.riiablo.io.ByteInput;
import com.riiablo.item.ItemReader;
import com.riiablo.mpq.MPQFileHandleResolver;

public class D2SReaderTool extends ApplicationAdapter {
  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new D2SReaderTool(args), config);
  }

  String[] d2ss;

  D2SReaderTool(String[] args) {
    Riiablo.home = new FileHandle(args[0]);
    if (args.length > 1) {
      this.d2ss = Arrays.copyOfRange(args, 1, args.length);
    }
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    Riiablo.home = Gdx.files.absolute(Riiablo.home.path());
    Riiablo.assets = new AssetManager();
    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.files = new Files(Riiablo.assets);
    Riiablo.string = new StringTBLs(Riiablo.mpqs);

    ItemReader itemReader = new ItemReader();
    D2SReader serializer = D2SReader.INSTANCE;
    for (String d2ss0 : d2ss) {
      FileHandle handle = Riiablo.home.child("Save/" + d2ss0 + '.' + D2S.EXT);
      ByteInput byteInput = ByteInput.wrap(handle.readBytes());
      D2S d2s = serializer.readD2S(byteInput);
      serializer.readRemaining(d2s, byteInput, itemReader);
    }

    Gdx.app.exit();
  }

  @Override
  public void dispose() {
    Riiablo.assets.dispose();
  }
}
