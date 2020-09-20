package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.io.FileNotFoundException;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.files.FileHandle;

public class MPQFileHandle extends FileHandle {
  public final MPQ mpq;
  public final String filename;
  public final long key;
  public final int offset;
  public final short locale;

  public MPQFileHandle(MPQ mpq, String filename) {
    this(mpq, filename, MPQ.DEFAULT_LOCALE);
  }

  public MPQFileHandle(MPQ mpq, String filename, short locale) {
    assert !StringUtils.contains(filename, '/');
    this.mpq = mpq;
    this.filename = filename;
    this.locale = locale;
    key = MPQ.File.key(filename);
    offset = MPQ.File.offset(filename);
  }

  @Override
  public boolean exists() {
    return mpq.contains(key, offset, locale);
  }

  @Override
  public long length() {
    try {
      return mpq.length(key, offset, locale);
    } catch (FileNotFoundException t) {
      throw new RuntimeException("File not found: " + filename, t);
    }
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
    try {
      return mpq.readByteBuf(filename, key, offset, locale);
    } catch (FileNotFoundException t) {
      throw new RuntimeException("File not found: " + filename, t);
    }
  }
}
