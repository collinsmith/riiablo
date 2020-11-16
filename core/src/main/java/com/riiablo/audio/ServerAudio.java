package com.riiablo.audio;

import com.badlogic.gdx.assets.AssetManager;
import com.riiablo.codec.excel.Sounds;

public class ServerAudio extends Audio {
  public ServerAudio(AssetManager assets) {
    super(assets);
  }

  @Override
  public Instance play(int id, boolean global) {
    return null;
  }

  @Override
  public Instance play(String id, boolean global) {
    return null;
  }

  @Override
  public synchronized Instance play(Sounds.Entry sound, boolean global) {
    return null;
  }
}
