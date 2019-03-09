package com.riiablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public enum GdxFileHandleResolvers implements FileHandleResolver {
  INTERNAL {
    @Override
    public FileHandle resolve(String fileName) {
      return Gdx.files.internal(fileName);
    }
  },
  ABSOLUTE {
    @Override
    public FileHandle resolve(String fileName) {
      return Gdx.files.absolute(fileName);
    }
  },
  CLASSPATH {
    @Override
    public FileHandle resolve(String fileName) {
      return Gdx.files.classpath(fileName);
    }
  },
  EXTERNAL {
    @Override
    public FileHandle resolve(String fileName) {
      return Gdx.files.external(fileName);
    }
  },
  LOCAL {
    @Override
    public FileHandle resolve(String fileName) {
      return Gdx.files.local(fileName);
    }
  }
}
