package com.riiablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class StringTBL {
  private static final String TAG = "StringTBL";
  private static final boolean DEBUG         = !true;
  private static final boolean DEBUG_ENTRIES = DEBUG && !true;
  private static final boolean DEBUG_LOOKUP  = DEBUG && !true;

  public static final short CLASSIC_OFFSET   = 0;
  public static final short PATCH_OFFSET     = 10000;
  public static final short EXPANSION_OFFSET = 20000;

  final Header      header;
  final short       indexes[];
  final HashTable   hashTable;
  final char        text[];

  private StringTBL(Header header, short[] indexes, HashTable hashTable, char[] text) {
    this.header = header;
    this.indexes = indexes;
    this.hashTable = hashTable;
    this.text = text;
  }
  
  public String lookup(int index) {
    index = indexes[index];
    HashTable.Entry entry = hashTable.entries[index];
    if (entry.strOffset == 0) {
      return null;
    }

    return new String(text, entry.strOffset - header.startIndex, entry.strLen - 1);
  }

  public String getKey(int index) {
    index = indexes[index];
    HashTable.Entry entry = hashTable.entries[index];
    if (entry.keyOffset == 0) {
      return null;
    }

    return new String(text, entry.keyOffset - header.startIndex, entry.strOffset - entry.keyOffset);
  }

  /*
  public CharSequence lookup(int index) {
    final HashTable.Entry entry = hashTable.entries[indexes[index]];
    if (entry.strOffset == 0) {
      return null;
    }

    return new CharSequence() {
      final int offset = entry.strOffset - header.startIndex;

      @Override
      public int length() {
        return entry.strLen - 1;
      }

      @Override
      public char charAt(int index) {
        return text[offset + index];
      }

      @Override
      public CharSequence subSequence(int start, int end) {
        return new String(text, offset + start, end - start);
      }

      @Override
      public String toString() {
        return new String(text, offset, entry.strLen - 1);
      }
    };
  }
  */

  public String lookup(String key) {
    if (key.equalsIgnoreCase("x")) {
      return null;
    }

    int hash = hash(key);
    int hashTries = 0;
    while (hashTries < header.maxTries) {
      HashTable.Entry entry = hashTable.entries[hash];
      if (entry.used != 0) {
        //if (StringUtils.equals(key, new String(text, entry.keyOffset - header.startIndex, entry.strOffset - entry.keyOffset - 1))) {
        KEY.offset = entry.keyOffset - header.startIndex;
        KEY.length = entry.strOffset - entry.keyOffset - 1;
        if (key.contentEquals(KEY)) {
          String value = new String(text, entry.strOffset - header.startIndex, entry.strLen - 1);
          if (DEBUG_LOOKUP) Gdx.app.debug(TAG, key + " took " + hashTries + " : \"" + value + "\"");
          return value;
        }
      } else {
        return null;
      }

      hash++;
      hash %= header.hashTableSize;
      hashTries++;
    }

    return null;
  }

  int lookupHash(String key) {
    if (key.equalsIgnoreCase("x")) {
      return -1;
    }

    int hash = hash(key);
    int hashTries = 0;
    while (hashTries < header.maxTries) {
      HashTable.Entry entry = hashTable.entries[hash];
      if (entry.used != 0) {
        KEY.offset = entry.keyOffset - header.startIndex;
        KEY.length = entry.strOffset - entry.keyOffset - 1;
        if (key.contentEquals(KEY)) {
          return hash;
        }
      } else {
        return -1;
      }

      hash++;
      hash %= header.hashTableSize;
      hashTries++;
    }

    return -1;
  }

  int lookupPtr(int index) {
    return hashTable.entries[indexes[index]].ptr;
  }

  private final InPlaceCharSequence KEY = new InPlaceCharSequence();
  private class InPlaceCharSequence implements CharSequence {
    int offset;
    int length;

    @Override
    public int length() {
      return length;
    }

    @Override
    public char charAt(int index) {
      return text[offset + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      throw new UnsupportedOperationException();
    }
  }

  public void dump() {
    for (int i = 0; i < indexes.length; i++) {
      System.out.format("%5d %s%n", i, lookup(i));
    }
  }

  public int hash(String key) {
    char charValue;
    long hashValue = 0;
    long tempValue;
    for (int i = 0; i < key.length(); i++) {
      charValue = key.charAt(i);
      hashValue <<= 4;
      hashValue += charValue;
      tempValue = (hashValue & 0xF0000000);
      if (tempValue != 0) {
        hashValue &= 0x0FFFFFFF;
        hashValue ^= (tempValue >>> 24);
      }
    }

    return (int) (hashValue % header.hashTableSize);
  }

  public static StringTBL loadFromFile(FileHandle file) {
    return loadFromStream(new ByteArrayInputStream(file.readBytes()));
  }

  public static StringTBL loadFromStream(InputStream in) {
    try {
      Header header = new Header(in);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());
      if (header.version != 1)
        throw new GdxRuntimeException("Unsupported version: " + (header.version & 0xFF));

      short[] indexes = new short[header.numElements];
      ByteBuffer.wrap(IOUtils.readFully(in, header.numElements * 2))
          .order(ByteOrder.LITTLE_ENDIAN)
          .asShortBuffer()
          .get(indexes);
      if (DEBUG) Gdx.app.debug(TAG, "indexes = " + Arrays.toString(indexes));

      HashTable hashTable = new HashTable(header, in);
      if (DEBUG) Gdx.app.debug(TAG, hashTable.toString());

      // TODO: Support other languages
      final int dataSize = header.endIndex - header.startIndex;
      char[] text = new char[dataSize];
      Reader reader = new InputStreamReader(in, "US-ASCII");
      IOUtils.readFully(reader, text);

      return new StringTBL(header, indexes, hashTable, text);
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't load StringTBL from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class Header {
    static final int SIZE = 21;

    short crc;
    short numElements;
    int   hashTableSize;
    byte  version;
    int   startIndex;
    int   maxTries;
    int   endIndex;

    Header(InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, SIZE)).order(ByteOrder.LITTLE_ENDIAN);
      crc           = buffer.getShort();
      numElements   = buffer.getShort();
      hashTableSize = buffer.getInt();
      version       = buffer.get();
      startIndex    = buffer.getInt();
      maxTries      = buffer.getInt();
      endIndex      = buffer.getInt();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("crc", crc & 0xFFFF)
          .append("numElements", numElements & 0xFFFF)
          .append("hashTableSize", hashTableSize)
          .append("version", version & 0xFF)
          .append("startIndex", startIndex)
          .append("maxTries", maxTries)
          .append("endIndex", endIndex)
          .build();
    }
  }
  static class HashTable {
    final Entry[] entries;

    HashTable(Header header, InputStream in) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, header.hashTableSize * Entry.SIZE))
          .order(ByteOrder.LITTLE_ENDIAN);

      entries = new Entry[header.hashTableSize];
      for (int i = 0; i < header.hashTableSize; i++) {
        entries[i] = new Entry(buffer);
        if (DEBUG_ENTRIES) Gdx.app.debug(TAG, entries[i].toString());
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("entries", entries)
          .build();
    }

    static class Entry {
      static final int SIZE = 17;

      byte  used;
      short index;
      int   hashValue;
      int   keyOffset;
      int   strOffset;
      short strLen;
      short ptr = -1;

      Entry(ByteBuffer buffer) {
        used      = buffer.get();
        index     = buffer.getShort();
        hashValue = buffer.getInt();
        keyOffset = buffer.getInt();
        strOffset = buffer.getInt();
        strLen    = buffer.getShort();
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("used", used & 0xFF)
            .append("index", index & 0xFFFF)
            .append("hashValue", hashValue)
            .append("keyOffset", keyOffset)
            .append("strOffset", strOffset)
            .append("strLen", strLen & 0xFFFF)
            .build();
      }
    }
  }
}
