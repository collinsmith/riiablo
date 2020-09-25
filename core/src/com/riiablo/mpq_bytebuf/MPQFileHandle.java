package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.files.FileHandle;

public final class MPQFileHandle extends FileHandle {
  public final MPQ mpq;
  public final String filename;
  public final short locale;

  final long key;
  final int offset;
  final MPQ.Entry entry;
  final MPQ.Block block;

  public MPQFileHandle(MPQ mpq, String filename) {
    this(mpq, filename, MPQ.DEFAULT_LOCALE);
  }

  public MPQFileHandle(MPQ mpq, String filename, short locale) {
    this(mpq, filename, MPQ.File.key(filename), MPQ.File.offset(filename), locale);
  }

  MPQFileHandle(MPQ mpq, String filename, long key, int offset, short locale) {
    this(mpq, filename, key, offset, locale, mpq.getEntry(key, offset, locale));
  }

  MPQFileHandle(MPQ mpq, String filename, long key, int offset, short locale, MPQ.Entry entry) {
    assert !StringUtils.contains(filename, '/');
    this.mpq = mpq;
    this.filename = filename;
    this.key = key;
    this.offset = offset;
    this.locale = locale;
    this.entry = entry;
    this.block = mpq.getBlock(entry);
  }

  public MPQ mpq() {
    return mpq;
  }

  public short locale() {
    return locale;
  }

  @Override
  public String toString() {
    return mpq + ":" + filename;
  }

  @Override
  public boolean exists() {
    return block != null;
  }

  @Override
  public long length() {
    if (block == null) {
      throw new RuntimeException(new FileNotFoundException("File not found: " + filename));
    }

    return block.FSize;
  }

  @Override
  public byte[] readBytes() {
    ByteBuf bb = null;
    try {
      bb = readByteBuf();
      return ByteBufUtil.getBytes(bb);
    } finally {
      if (bb != null) bb.release();
    }
  }

  public ByteBuf readByteBuf() {
    if (block == null) {
      throw new RuntimeException(new FileNotFoundException("File not found: " + filename));
    }

    return MPQInputStream.readByteBuf(this);
  }

  @Override
  public InputStream read() {
    return read(true);
  }

  public InputStream read(boolean releaseOnClose) {
    try {
      return MPQInputStream.open(this, true, releaseOnClose);
    } catch (IOException t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public File file() {
    throw new UnsupportedOperationException("Not supported by MPQFileHandle.");
  }

  @Override
  public String name() {
    return filename;
  }

  @Override
  public String extension() {
    return FilenameUtils.getExtension(filename);
  }

  @Override
  public String path() {
    return FilenameUtils.getFullPath(filename);
  }

  @Override
  public String pathWithoutExtension() {
    int index = FilenameUtils.indexOfExtension(filename);
    if (index == -1) {
      return filename;
    }

    return filename.substring(0, index);
  }
}
