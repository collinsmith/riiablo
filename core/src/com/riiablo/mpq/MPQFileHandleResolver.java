package com.riiablo.mpq;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.riiablo.Riiablo;

import java.util.concurrent.CopyOnWriteArrayList;

public class MPQFileHandleResolver implements FileHandleResolver {
  private static final String TAG = "MPQFileHandleResolver";
  private static final boolean DEBUG = !true;

  public final MPQ patch_d2;
  public final MPQ d2exp;
  public final MPQ d2xmusic;
  public final MPQ d2xtalk;
  public final MPQ d2xvideo;
  public final MPQ d2data;
  public final MPQ d2char;
  public final MPQ d2sfx;
  public final MPQ d2music;
  public final MPQ d2speech;
  public final MPQ d2video;

  public MPQFileHandleResolver() {
    this(Riiablo.home);
  }

  public MPQFileHandleResolver(FileHandle home) {
    patch_d2 = add(home.child("patch_d2.mpq"));
    d2exp    = add(home.child("d2exp.mpq"));
    d2xmusic = add(home.child("d2xmusic.mpq"));
    d2xtalk  = add(home.child("d2xtalk.mpq"));
    d2xvideo = add(home.child("d2xvideo.mpq"));
    d2data   = add(home.child("d2data.mpq"));
    d2char   = add(home.child("d2char.mpq"));
    d2sfx    = add(home.child("d2sfx.mpq"));
    d2music  = add(home.child("d2music.mpq"));
    d2speech = add(home.child("d2speech.mpq"));
    d2video  = add(home.child("d2video.mpq"));
  }

  private final CopyOnWriteArrayList<MPQ> mpqs = new CopyOnWriteArrayList<>();

  public MPQ add(MPQ mpq) {
    mpqs.add(mpq);
    return mpq;
  }

  public MPQ add(FileHandle file) {
    return add(MPQ.loadFromFile(file));
  }

  public boolean contains(String fileName) {
    for (MPQ mpq : mpqs) {
      if (mpq.contains(fileName)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public FileHandle resolve(String fileName) {
    if (DEBUG) Gdx.app.debug(TAG, "Resolving " + fileName);
    if (fileName == null) return null;
    if (fileName.endsWith("gem.wav")) {
      return new MPQFileHandle(d2sfx, fileName);
    }

    for (MPQ mpq : mpqs) {
      if (mpq.contains(fileName)) {
        if (DEBUG) Gdx.app.debug(TAG, fileName + " found in " + mpq);
        return new MPQFileHandle(mpq, fileName);
      }
    }

    //Gdx.app.error(TAG, "Could not resolve " + fileName);
    return null;
  }
}
