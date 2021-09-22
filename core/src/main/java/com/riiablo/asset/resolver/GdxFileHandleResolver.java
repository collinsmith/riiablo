package com.riiablo.asset.resolver;

import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.FileHandleResolver;

/**
 * Adapter for {@link com.badlogic.gdx.assets.loaders.FileHandleResolver}
 */
public enum GdxFileHandleResolver implements FileHandleResolver {
  /** @see ClasspathFileHandleResolver */
  Classpath(new ClasspathFileHandleResolver()),
  /** @see InternalFileHandleResolver */
  Internal(new InternalFileHandleResolver()),
  /** @see ExternalFileHandleResolver */
  External(new ExternalFileHandleResolver()),
  /** @see AbsoluteFileHandleResolver */
  Absolute(new AbsoluteFileHandleResolver()),
  /** @see LocalFileHandleResolver */
  Local(new LocalFileHandleResolver()),
  ;

  final com.badlogic.gdx.assets.loaders.FileHandleResolver delegate;

  GdxFileHandleResolver(com.badlogic.gdx.assets.loaders.FileHandleResolver delegate) {
    this.delegate = delegate;
  }

  @Override
  public FileHandle resolve(AssetDesc<?> asset) {
    final FileHandle handle = delegate.resolve(asset.path());
    return handle.exists() ? handle : null;
  }
}
