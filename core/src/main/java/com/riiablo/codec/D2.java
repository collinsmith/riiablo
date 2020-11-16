package com.riiablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.riiablo.util.BufferUtils;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class D2 {
  private static final String TAG = "D2";
  private static final boolean DEBUG         = !true;
  private static final boolean DEBUG_BLOCKS  = DEBUG && true;
  private static final boolean DEBUG_ENTRIES = DEBUG && true;

  Block blocks[];

  private D2(Block[] blocks) {
    this.blocks = blocks;
  }

  private static int hash(String cof) {
    int hash = 0;
    for (int i = 0; i < cof.length(); i++) {
      hash += Character.toUpperCase(cof.charAt(i));
    }

    return hash & 0xFF;
  }

  public Entry getEntry(String cof) {
    int hash = hash(cof);
    Block block = blocks[hash];
    for (Entry entry : block.entries) {
      if (entry.cof.equalsIgnoreCase(cof)) {
        return entry;
      }
    }

    return null;
  }

  public static D2 loadFromFile(FileHandle handle) {
    return loadFromStream(handle.read());
  }

  public static D2 loadFromStream(InputStream in) {
    try {
      int numBlocks = Block.MAX_VALUE;
      Block[] blocks = new Block[numBlocks];
      for (int i = 0; i < numBlocks; i++) {
        blocks[i] = new Block(in);
        if (DEBUG_BLOCKS) Gdx.app.debug(TAG, i + ": " + blocks[i].toString());
      }

      return new D2(blocks);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load D2 from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class Block {
    static final int MAX_VALUE = 256;

    int   numEntries;
    Entry entries[];

    Block(InputStream in) throws IOException {
      numEntries = EndianUtils.readSwappedInteger(in);
      entries = new Entry[numEntries];
      for (int i = 0; i < numEntries; i++) {
        entries[i] = new Entry(in);
        if (DEBUG_ENTRIES) Gdx.app.debug(TAG, entries[i].toString());
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("numEntries", numEntries)
          .append("entries", entries)
          .build();
    }
  }

  public static class Entry {
    static final int SIZE = 160;

    public String cof;
    public int    framesPerDir;
    public int    speed;
    public byte   data[];

    Entry(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      cof          = BufferUtils.readString2(buffer, 8);
      framesPerDir = buffer.getInt();
      speed        = buffer.getInt();
      data         = BufferUtils.readBytes(buffer, 144);
      assert !buffer.hasRemaining();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("cof", cof)
          .append("framesPerDir", framesPerDir)
          .append("speed", speed)
          //.append("data", data)
          .build();
    }
  }
}
