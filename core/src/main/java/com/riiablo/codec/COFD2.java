package com.riiablo.codec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import com.riiablo.util.BufferUtils;

public class COFD2 {
  private static final String TAG = "COFD2";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_ENTRIES = DEBUG && false;

  Array<Entry>      entries;
  Trie<String, COF> trie;

  private COFD2(Array<Entry> entries) {
    this.entries = entries;

    trie = new PatriciaTrie<>();
    for (Entry entry : entries) {
      trie.put(entry.cofName.toLowerCase(), entry.cof);
    }
  }

  public int getNumEntries() {
    return entries.size;
  }

  public COF lookup(String cof) {
    return trie.get(cof.toLowerCase());
  }

  public static COFD2 loadFromFile(FileHandle handle) {
    return loadFromStream(handle.read());
  }

  public static COFD2 loadFromStream(InputStream in) {
    try {
      int i = 0;
      Array<Entry> entries = new Array<>(1024);
      while (in.available() > 0) {
        Entry entry = new Entry(in);
        entries.add(entry);
        if (DEBUG_ENTRIES) Gdx.app.debug(TAG, i++ + ": " + entry.toString());
        if (entry.header1 != -1 || entry.header2 != 0 || entry.header3 != -1) {
          Gdx.app.error(TAG, "Invalid entry headers: " + entry);
        }
      }

      return new COFD2(entries);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load D2 from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class Entry {
    static final int HEADER_SIZE = 24;

    int                  header1;
    int                  header2;
    int                  cofSize;
    String               cofName;
    int                  header3;
    com.riiablo.codec.COF cof;

    Entry(InputStream in) throws IOException {
      ByteBuffer header = ByteBuffer.wrap(IOUtils.readFully(in, HEADER_SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      header1 = header.getInt();
      header2 = header.getInt();
      cofSize = header.getInt();
      cofName = BufferUtils.readString2(header, 8);
      header3 = header.getInt();
      assert !header.hasRemaining();

      // TODO: BoundedInputStream available proxies in.available and not the length of cofSize, but
      //       a marker is needed anyways to tell COF to load special.
      cof = COF.loadFromStream(new BoundedInputStream(in, cofSize), cofSize);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          //.append("header1", header1)
          //.append("header2", header2)
          .append("cofSize", cofSize)
          .append("cofName", cofName)
          //.append("header3", header3)
          .append("cof", cof)
          .build();
    }
  }
}
