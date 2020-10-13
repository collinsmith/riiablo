package com.riiablo.asset;

import io.netty.util.AsciiString;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.path.PathTransformer;

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

  public FileHandle resolve(AssetDesc asset) {
    return resolve(asset.path);
  }

  protected abstract FileHandle resolveTransformed(AsciiString path);
}
