package com.riiablo.mpq_bytebuf;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.Riiablo;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq_bytebuf.Mpq.HashTable;

import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_LOCALE;
import static org.apache.commons.lang3.StringUtils.appendIfMissingIgnoreCase;

public class MpqFileResolver implements FileHandleResolver, Disposable {
  private static final Logger log = LogManager.getLogger(MpqFileResolver.class);

  static final String[] DEFAULT_MOUNT_POINTS = { "", "DATA\\LOCAL\\", "DATA\\GLOBAL\\" };

  public final Mpq patch_d2;
  public final Mpq d2exp;
  public final Mpq d2xmusic;
  public final Mpq d2xtalk;
  public final Mpq d2xvideo;
  public final Mpq d2data;
  public final Mpq d2char;
  public final Mpq d2sfx;
  public final Mpq d2music;
  public final Mpq d2speech;
  public final Mpq d2video;

  final Array<Mpq> mpqs = Array.of(true, 16, Mpq.class);
  final DecodingService decoder = new DecodingService(2);
  final Array<MountPoint> mounts = Array.of(true, 4, MountPoint.class);

  public MpqFileResolver() {
    this(Riiablo.home);
  }

  public MpqFileResolver(FileHandle home) {
    mountDefaults();
    patch_d2 = add(home, "patch_d2");
    d2exp = add(home, "d2exp");
    d2xmusic = add(home, "d2xmusic");
    d2xtalk = add(home, "d2xtalk");
    d2xvideo = add(home, "d2xvideo");
    d2data = add(home, "d2data");
    d2char = add(home, "d2char");
    d2sfx = add(home, "d2sfx");
    d2music = add(home, "d2music");
    d2speech = add(home, "d2speech");
    d2video = add(home, "d2video");
  }

  MpqFileResolver(FileHandle home, String... mpqs) {
    mountDefaults();
    for (String mpq : mpqs) add(home, mpq);
    patch_d2 = d2exp = d2xmusic = d2xtalk = d2xvideo = d2data
        = d2char = d2sfx = d2music = d2speech = d2video = null;
  }

  @Override
  public void dispose() {
    decoder.gracefulShutdown();
    for (Mpq mpq : mpqs) mpq.dispose();
    mpqs.clear();
  }

  void mountDefaults() {
    for (String mount : DEFAULT_MOUNT_POINTS) {
      mount(mount);
    }
  }

  MpqFileResolver mount(String path) {
    mounts.add(MountPoint.of(path));
    return this;
  }

  Mpq add(FileHandle home, String path) {
    path = appendIfMissingIgnoreCase(path, ".mpq");
    FileHandle handle = home.child(path);
    return add(handle);
  }

  public Mpq add(FileHandle handle) {
    Mpq mpq = Mpq.open(handle);
    return add(mpq);
  }

  public Mpq add(Mpq mpq) {
    mpqs.add(mpq);
    return mpq;
  }
  public boolean contains(String filename) {
    return contains(filename, DEFAULT_LOCALE);
  }

  public boolean contains(String filename, short locale) {
    filename = Decrypter.fix(filename);
    final long key = HashTable.key(filename);
    final int hash = HashTable.hash(filename);
    for (Mpq mpq : mpqs) {
      if (mpq.contains(key, hash, locale)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public MpqFileHandle resolve(String filename) {
    return resolve(filename, DEFAULT_LOCALE);
  }

  public MpqFileHandle resolve(String filename, short locale) {
    filename = Decrypter.fix(filename);
    for (MountPoint mount : mounts) {
      final MpqFileHandle handle = resolve(mount, filename, locale);
      if (handle != null) return handle;
    }

    log.error("Failed to resolve {}:{}", HashTable.localeToString(locale), filename);
    return null;
  }

  MpqFileHandle resolve(MountPoint mount, CharSequence filename, short locale) {
    filename = mount.join(filename);
    log.trace("Resolving {}:{}...", HashTable.localeToString(locale), filename);

    int misses = 0;
    final long key = HashTable.key(filename);
    final int hash = HashTable.hash(filename);
    for (Mpq mpq : mpqs) {
      final int m = mpq.hashTable.misses;
      final int index = mpq.get(key, hash, locale);
      misses += (mpq.hashTable.misses - m);
      if (index >= 0) {
        log.debug("{}:{} found in {}[{}] ({} misses)", HashTable.localeToString(locale), filename, mpq, index, misses);
        return mpq.open(decoder, index, filename);
      }
    }

    return null;
  }

  static final class MountPoint {
    static MountPoint of(String path) {
      return new MountPoint(path);
    }

    final String path;

    MountPoint(String path) {
      if (!path.isEmpty()) path = StringUtils.appendIfMissing(path, "\\");
      this.path = Decrypter.fix(path);
    }

    CharSequence join(CharSequence path) {
      return this.path.isEmpty() ? path : this.path + path;
    }

    @Override
    public String toString() {
      return path;
    }
  }
}
