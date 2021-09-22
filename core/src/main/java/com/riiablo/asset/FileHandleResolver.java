package com.riiablo.asset;

import com.badlogic.gdx.files.FileHandle;

public interface FileHandleResolver {
  FileHandle resolve(AssetDesc<?> asset);
}
