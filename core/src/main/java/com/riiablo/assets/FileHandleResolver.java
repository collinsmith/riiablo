package com.riiablo.assets;

import io.netty.util.AsciiString;

import com.badlogic.gdx.files.FileHandle;

public abstract class FileHandleResolver {
  final PathTransformer transformer;

  protected FileHandleResolver(PathTransformer transformer) {
    this.transformer = transformer;
  }

  public PathTransformer transformer() {
    return transformer;
  }

  public FileHandle resolve(AsciiString path) {
    return resolveTransformed(transformer.transform(path));
  }

  public FileHandle resolve(Asset asset) {
    return resolve(asset.path);
  }

  protected abstract FileHandle resolveTransformed(AsciiString path);
}
