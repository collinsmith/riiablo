package com.riiablo.assets;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public class FileHandleReader implements SyncReader<FileHandle> {
  final FileHandleResolver resolver;

  public FileHandleReader(FileHandleResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public FileHandle read(Asset asset) {
    return resolver.resolve(asset.path());
  }
}
