package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

  /**
   * @deprecated use {@link #readByteBuf()} instead
   */
  @Override
  @Deprecated
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
    try {
      return MPQInputStream.open(this, true, true);
    } catch (IOException t) {
      throw new RuntimeException(t);
    }
  }

  public InputStream read(boolean releaseOnClose) {
    try {
      return MPQInputStream.open(this, true, releaseOnClose);
    } catch (IOException t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @deprecated use {@link #read()} instead
   */
  @Override
  @Deprecated
  public BufferedInputStream read(int bufferSize) {
    return super.read(bufferSize);
  }

  @Override
  public String name() {
    return filename;
  }
}
