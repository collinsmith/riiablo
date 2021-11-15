package com.riiablo.mpq_bytebuf;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.Riiablo;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetParams;
import com.riiablo.asset.FileHandleResolver;
import com.riiablo.asset.param.MpqParams;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq_bytebuf.Mpq.HashTable;

import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_LOCALE;
import static org.apache.commons.lang3.StringUtils.appendIfMissingIgnoreCase;

public class MpqFileResolver implements FileHandleResolver, Disposable {
  private static final Logger log = LogManager.getLogger(MpqFileResolver.class);

  static final String[] DEFAULT_MOUNT_POINTS = {
      "", // fully qualified paths
      "DATA\\LOCAL\\",
      "DATA\\GLOBAL\\",
  };

  static final String[] DEFAULT_MPQS = {
      "patch_d2",
      "d2exp",
      "d2xmusic",
      "d2xtalk",
      "d2xvideo",
      "d2data",
      "d2char",
      "d2sfx",
      "d2music",
      "d2speech",
      "d2video",
  };

  final Array<Mpq> mpqs = Array.of(true, 16, Mpq.class);
  final ObjectMap<String, Mpq> lookup = new ObjectMap<>();
  final DecoderExecutorGroup decoder = new DecoderExecutorGroup(2);

  public MpqFileResolver() {
    this(Riiablo.home);
  }

  public MpqFileResolver(FileHandle home) {
    this(home, DEFAULT_MPQS);
  }

  MpqFileResolver(FileHandle home, String... mpqs) {
    for (String mpq : mpqs) add(home, mpq);
  }

  @Override
  public void dispose() {
    decoder.shutdownGracefully();
    for (Mpq mpq : mpqs) mpq.dispose();
    mpqs.clear();
  }

  Mpq add(FileHandle home, String path) {
    path = appendIfMissingIgnoreCase(path, ".mpq");
    FileHandle handle = home.child(path);
    return add(handle);
  }

  public Mpq add(FileHandle handle) {
    Mpq mpq = Mpq.open(handle);
    lookup.put(handle.nameWithoutExtension(), mpq);
    return add(mpq);
  }

  public Mpq add(Mpq mpq) {
    mpqs.add(mpq);
    return mpq;
  }

  public Mpq get(String name) {
    Mpq mpq = lookup.get(name);
    if (mpq == null) throw new RuntimeException("MPQ not found: " + name);
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
  public MpqFileHandle resolve(AssetDesc<?> asset) {
    final AssetParams assetParams = asset.params();
    if (!(assetParams instanceof MpqParams)) return null;
    final MpqParams mpqParams = (MpqParams) assetParams;
    return resolve(asset, mpqParams);
  }

  public MpqFileHandle resolve(AssetDesc<?> asset, MpqParams mpqParams) {
    String filename = Decrypter.fix(asset.path());
    log.trace("Resolving {}:{}...", mpqParams.localeToString(), filename);

    int misses = 0;
    final long key = HashTable.key(filename);
    final int hash = HashTable.hash(filename);
    final Mpq[] mpqs = this.mpqs.items;
    for (int i = 0, s = this.mpqs.size; i < s; i++) {
      final Mpq mpq = mpqs[i];
      final int m = mpq.hashTable.misses;
      final int index = mpq.get(key, hash, mpqParams.locale);
      misses += (mpq.hashTable.misses - m);
      if (index >= 0) {
        log.debug("{}:{} found in {}[{}] ({} misses)", mpqParams.localeToString(), filename, mpq, index, misses);
        return mpq.open(decoder, index, filename);
      }
    }

    log.debug("Failed to resolve {}:{}", mpqParams.localeToString(), filename);
    return null;
  }
}
