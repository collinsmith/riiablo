package com.riiablo.mpq_bytebuf;

import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Riiablo;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class MPQFileHandleResolver implements FileHandleResolver {
  private static final Logger log = LogManager.getLogger(MPQFileHandleResolver.class);

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

  private final CopyOnWriteArrayList<MPQ> mpqs = new CopyOnWriteArrayList<>();

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

  public MPQ add(MPQ mpq) {
    mpqs.add(mpq);
    return mpq;
  }

  public MPQ add(FileHandle file) {
    return add(MPQ.load(file));
  }

  public boolean contains(String filename) {
    return contains(filename, MPQ.DEFAULT_LOCALE);
  }

  public boolean contains(String filename, short locale) {
    filename = filename.replace('/', '\\');
    final long key = MPQ.File.key(filename);
    final int offset = MPQ.File.offset(filename);
    for (MPQ mpq : mpqs) {
      if (mpq.contains(key, offset, locale)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public MPQFileHandle resolve(String filename) {
    return resolve(filename, MPQ.DEFAULT_LOCALE);
  }

  public MPQFileHandle resolve(String filename, short locale) {
    filename = filename.replace('/', '\\');
    log.debug("Resolving {}:{}...", filename, locale);
    if (filename == null) return null;

    final long key = MPQ.File.key(filename);
    final int offset = MPQ.File.offset(filename);
    for (MPQ mpq : mpqs) {
      MPQ.Entry entry = mpq.getEntry(key, offset, locale);
      if (entry != null) {
        log.debug("{}:{} found in {}", filename, locale, mpq);
        return new MPQFileHandle(mpq, filename, key, offset, locale, entry);
      }
    }

    log.error("Failed to resolve {}:{}", filename, locale);
    return null;
  }
}
