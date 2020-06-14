package com.riiablo.mpq;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.text.StringEscapeUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import com.riiablo.mpq.util.Decryptor;
import com.riiablo.util.BufferUtils;

public class MPQ {
  private static final String TAG = MPQ.class.getSimpleName();
  private static final boolean DEBUG = !true;

  final FileHandle file;
  final Header     header;
  final HashTable  hashTable;
  final BlockTable blockTable;

  private MPQ(FileHandle file, Header header, HashTable hashTable, BlockTable blockTable) {
    this.file       = file;
    this.header     = header;
    this.hashTable  = hashTable;
    this.blockTable = blockTable;
  }

  @Override
  public String toString() {
    return file.toString();
  }

  public FileHandle file() {
    return file;
  }

  public boolean contains(String fileName) {
    fileName = fileName.replaceAll("/", "\\\\");
    return hashTable.contains(fileName);
  }

  public long length(String fileName) {
    fileName = fileName.replaceAll("/", "\\\\");
    HashTable.Entry entry = hashTable.getEntry(fileName);
    if (entry == null) {
      throw new GdxRuntimeException("File not found: " + fileName);
    }

    BlockTable.Block block = blockTable.get(entry.blockIndex);
    assert block != null;
    return block.FSize;
  }

  public InputStream read(MPQFileHandle file) {
    return read(file.fileName);
  }

  public InputStream read(String fileName) {
    fileName = fileName.replaceAll("/", "\\\\");
    Gdx.app.log(TAG, "Reading " + fileName + "...");
    HashTable.Entry entry = hashTable.getEntry(fileName);
    if (DEBUG) Gdx.app.debug(TAG, "entry = " + Objects.toString(entry));
    if (entry == null) {
      throw new GdxRuntimeException("File not found: " + fileName);
    }

    final BlockTable.Block block = blockTable.get(entry.blockIndex);
    if (DEBUG) Gdx.app.debug(TAG, "block = " + Objects.toString(block));
    assert block != null;

    return new MPQInputStream(this, fileName, block);
  }

  public byte[] readBytes(MPQFileHandle file) {
    return readBytes(file.fileName);
  }

  public byte[] readBytes(String fileName) {
    fileName = fileName.replaceAll("/", "\\\\");
    Gdx.app.log(TAG, "Reading " + fileName + "...");
    HashTable.Entry entry = hashTable.getEntry(fileName);
    if (DEBUG) Gdx.app.debug(TAG, "entry = " + Objects.toString(entry));
    if (entry == null) {
      throw new GdxRuntimeException("File not found: " + fileName);
    }

    final BlockTable.Block block = blockTable.get(entry.blockIndex);
    if (DEBUG) Gdx.app.debug(TAG, "block = " + Objects.toString(block));
    assert block != null;

    return MPQInputStream.readBytes(this, fileName, block);
  }

  public static MPQ loadFromFile(FileHandle file) {
    Gdx.app.log(TAG, "Loading " + file.name() + "...");
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(file.file(), "r");
      FileChannel fc = raf.getChannel();

      Header header = new Header(fc);
      if (DEBUG) Gdx.app.debug(TAG, header.toString());
      if (!header.id.equals(Header.HEADER))
        throw new GdxRuntimeException("Header id: " + header.id);
      if (header.headerSize != Header.SIZE)
        throw new GdxRuntimeException("Header size incorrect: " + header.headerSize);
      if (header.archiveSize != file.length())
        throw new GdxRuntimeException("Archive size does not match file size.");
      if (header.formatVersion != 0)
        throw new GdxRuntimeException("Unsupported version: " + header.formatVersion);
      if (header.hashTableSize <= 0 || (header.hashTableSize & (header.hashTableSize - 1)) != 0)
        throw new GdxRuntimeException("Hash table size must be a power of 2: " + header.hashTableSize);
      if (header.blockTableSize <= 0)
        throw new GdxRuntimeException("Block table size must be positive: " + header.blockTableSize);

      raf.seek(header.hashTableOffset);
      HashTable hashTable = new HashTable(header, fc);

      raf.seek(header.blockTableOffset);
      BlockTable blockTable = new BlockTable(header, fc);
      return new MPQ(file, header, hashTable, blockTable);
    } catch (Exception e) {
      throw new GdxRuntimeException("Couldn't load file: " + file, e);
    } finally {
      StreamUtils.closeQuietly(raf);
    }
  }

  static class Header {
    static final int SIZE = 0x00000020;
    static final int DISK_SECTOR_SIZE = 0x00000200;

    static final String HEADER    = "MPQ\u001A";
    static final String USER_DATA = "MPQ\u001B";

    final String id;
    final long   headerSize;
    final long   archiveSize;
    final int    formatVersion;
    final int    blockSize;
    final int    sectorSize;
    final long   hashTableOffset;
    final long   blockTableOffset;
    final int    hashTableSize;
    final int    blockTableSize;

    Header(FileChannel fc) throws IOException {
      ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.LITTLE_ENDIAN);
      IOUtils.readFully(fc, buffer);
      buffer.flip();

      id               = BufferUtils.readString(buffer, 4);
      headerSize       = BufferUtils.readUnsignedInt(buffer);
      archiveSize      = BufferUtils.readUnsignedInt(buffer);
      formatVersion    = BufferUtils.readUnsignedShort(buffer);
      blockSize        = BufferUtils.readUnsignedShort(buffer);
      sectorSize       = DISK_SECTOR_SIZE << blockSize;
      hashTableOffset  = BufferUtils.readUnsignedInt(buffer);
      blockTableOffset = BufferUtils.readUnsignedInt(buffer);
      hashTableSize    = buffer.getInt();
      blockTableSize   = buffer.getInt();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", StringEscapeUtils.escapeJava(id))
          .append("headerSize", headerSize)
          .append("archiveSize", archiveSize)
          .append("formatVersion", formatVersion)
          .append("blockSize", blockSize)
          .append("sectorSize", sectorSize)
          .append("hashTableOffset", "0x" + Long.toHexString(hashTableOffset))
          .append("blockTableOffset", "0x" + Long.toHexString(blockTableOffset))
          .append("hashTableSize", hashTableSize)
          .append("blockTableSize", blockTableSize)
          .toString();
    }
  }
  static class HashTable {
    final Entry[] entries;

    HashTable(Header header, FileChannel fc) throws IOException {
      int capacity = header.hashTableSize;
      assert capacity > 0 && (capacity & (capacity - 1)) == 0 : "Capacity must be a power of 2";
      ByteBuffer buffer = ByteBuffer.allocate(capacity * Entry.SIZE);
      IOUtils.readFully(fc, buffer);
      buffer.rewind();

      Decryptor.decrypt(Decryptor.HASH_TABLE_KEY, buffer);
      buffer.rewind();

      entries = new Entry[capacity];
      for (int i = 0; i < capacity; i++) {
        entries[i] = new Entry(buffer);
      }
    }

    int getIndex(long key, int offset, short locale) {
      int mask = entries.length - 1;
      int start = offset & mask;
      int bestId = -1;
      loop:
      for (int i = 0; i < entries.length; i++) {
        int index = start + i & mask;
        Entry entry = entries[index];
        switch (entry.blockIndex) {
          case Entry.UNUSED:
            break loop;
          case Entry.DELETED:
            continue loop;
          default:
            if (entry.key == key) {
              if (entry.locale == locale) {
                return index;
              } else if (bestId == -1 || entry.locale == Entry.DEFAULT_LOCALE) {
                bestId = index;
              }
            }
        }
      }

      return bestId;
    }

    Entry getEntry(String file) {
      int index = getIndex(File.key(file), File.offset(file), Entry.DEFAULT_LOCALE);
      return index != -1 ? entries[index] : null;
    }

    public boolean contains(String file) {
      return getIndex(File.key(file), File.offset(file), Entry.DEFAULT_LOCALE) != -1;
    }

    public int getBlockIndex(String file) {
      Entry entry = getEntry(file);
      if (entry == null) {
        throw new GdxRuntimeException("File not found: " + file);
      } else if (entry.blockIndex < 0) {
        throw new GdxRuntimeException("Invalid block index for " + file + ": " + entry.getBlockIndex());
      }

      return entry.blockIndex;
    }

    static class File {
      static long key(String file) {
        int key1 = Decryptor.HASH_TABLE_KEY1.hash(file);
        int key2 = Decryptor.HASH_TABLE_KEY2.hash(file);
        return ((long) key2 << Integer.SIZE) | (key1 & 0xFFFFFFFFL);
      }

      static int offset(String file) {
        return Decryptor.HASH_TABLE_OFFSET.hash(file);
      }
    }
    static class Entry {
      static final int SIZE = 0x00000010;

      static final int UNUSED  = -1;
      static final int DELETED = -2;

      static final short DEFAULT_LOCALE   = 0x0000;
      static final short DEFAULT_PLATFORM = 0x0000;

      final long  key;        // QWORD
      final short locale;     // WORD
      final short platform;   // WORD
      final int   blockIndex; // DWORD

      Entry(ByteBuffer in) {
        key        = in.getLong();
        locale     = in.getShort();
        platform   = in.getShort();
        blockIndex = in.getInt();
      }

      public String getLocale() {
        switch (locale & 0xFFFF) {
          case 0:     return "Neutral / English (American)";
          case 0x404: return "Chinese (Taiwan)";
          case 0x405: return "Czech";
          case 0x407: return "German";
          case 0x409: return "English";
          case 0x40a: return "Spanish";
          case 0x40c: return "French";
          case 0x410: return "Italian";
          case 0x411: return "Japanese";
          case 0x412: return "Korean";
          case 0x415: return "Polish";
          case 0x416: return "Portuguese";
          case 0x419: return "Russian";
          case 0x809: return "English (UK)";
          default:    return "Unknown: 0x" + Integer.toHexString(locale & 0xFFFF);
        }
      }

      public String getPlatform() {
        switch (platform & 0xFFFF) {
          case 0:  return "DEFAULT";
          default: return "Unknown: 0x" + Integer.toHexString(platform & 0xFFFF);
        }
      }

      public String getBlockIndex() {
        switch (blockIndex) {
          case UNUSED:  return "UNUSED";
          case DELETED: return "DELETED";
          default:      return Long.toString(blockIndex);
        }
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("key", "0x" + Long.toHexString(key))
            .append("locale", getLocale())
            .append("platform", getPlatform())
            .append("blockIndex", getBlockIndex())
            .toString();
      }
    }
  }
  static class BlockTable {

    Block[] blocks;

    BlockTable(Header header, FileChannel fc) throws IOException {
      int capacity = header.blockTableSize;
      assert capacity > 0 : "Capacity must be positive.";

      ByteBuffer buffer = ByteBuffer.allocate(capacity * Block.SIZE);
      IOUtils.readFully(fc, buffer);
      buffer.rewind();

      Decryptor.decrypt(Decryptor.BLOCK_TABLE_KEY, buffer);
      buffer.rewind();

      blocks = new Block[capacity];
      for (int i = 0; i < capacity; i++) {
        blocks[i] = new Block(buffer);
      }
    }

    public Block get(int pos) {
      return blocks[pos];
    }

    static class Block {
      static final int SIZE = 0x00000010;

      static final int FLAG_IMPLODE       = 0x00000100;
      static final int FLAG_COMPRESSED    = 0x00000200;
      static final int FLAG_ENCRYPTED     = 0x00010000;
      static final int FLAG_FIX_KEY       = 0x00020000;
      static final int FLAG_PATCH_FILE    = 0x00100000;
      static final int FLAG_SINGLE_UNIT   = 0x01000000;
      static final int FLAG_DELETE_MARKER = 0x02000000;
      static final int FLAG_SECTOR_CRC    = 0x04000000;
      static final int FLAG_EXISTS        = 0x80000000;

      final long filePos;
      final int  CSize;
      final int  FSize;
      final int  flags;

      Block(ByteBuffer in) {
        filePos = BufferUtils.readUnsignedInt(in);
        CSize   = in.getInt();
        FSize   = in.getInt();
        flags   = in.getInt();
      }

      public boolean hasFlag(int flag) {
        return (flags & flag) == flag;
      }

      public String getFlags() {
        StringBuilder builder = new StringBuilder(64);
        if ((flags & FLAG_IMPLODE)       == FLAG_IMPLODE)       builder.append("IMPLODE|");
        if ((flags & FLAG_COMPRESSED)    == FLAG_COMPRESSED)    builder.append("COMPRESSED|");
        if ((flags & FLAG_ENCRYPTED)     == FLAG_ENCRYPTED)     builder.append("ENCRYPTED|");
        if ((flags & FLAG_FIX_KEY)       == FLAG_FIX_KEY)       builder.append("FIX_KEY|");
        if ((flags & FLAG_PATCH_FILE)    == FLAG_PATCH_FILE)    builder.append("PATCH_FILE|");
        if ((flags & FLAG_SINGLE_UNIT)   == FLAG_SINGLE_UNIT)   builder.append("SINGLE_UNIT|");
        if ((flags & FLAG_DELETE_MARKER) == FLAG_DELETE_MARKER) builder.append("DELETE_MARKER|");
        if ((flags & FLAG_SECTOR_CRC)    == FLAG_SECTOR_CRC)    builder.append("SECTOR_CRC|");
        if ((flags & FLAG_EXISTS)        == FLAG_EXISTS)        builder.append("EXISTS|");
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("filePos", "0x" + Long.toHexString(filePos))
            .append("CSize", CSize)
            .append("FSize", FSize)
            .append("flags", getFlags())
            .toString();
      }
    }
  }
}
