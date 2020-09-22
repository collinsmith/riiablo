package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.text.StringEscapeUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.mpq_bytebuf.util.Decryptor;

public final class MPQ {
  private static final Logger log = LogManager.getLogger(MPQ.class);

  private static final boolean DEBUG_MODE = !true; // disables very expensive debug logging

  private static final String HEADER = "MPQ\u001A";
  private static final String USER_DATA = "MPQ\u001B";
  private static final int DISK_SECTOR_SIZE = 0x00000200;

  public static final short DEFAULT_LOCALE = 0x0000;
  public static final short DEFAULT_PLATFORM = 0x0000;

  private static final ByteBufAllocator ALLOC = new PooledByteBufAllocator(
      PooledByteBufAllocator.defaultNumHeapArena(),
      PooledByteBufAllocator.defaultNumDirectArena(),
      4096,
      PooledByteBufAllocator.defaultMaxOrder());

  ByteBufAllocator alloc() {
    return ALLOC;
  }

  public ByteBuf obtainHeapBuffer() {
    return obtainHeapBuffer(DISK_SECTOR_SIZE);
  }

  public ByteBuf obtainHeapBuffer(final int capacity) {
    return ALLOC.heapBuffer(capacity, sectorSize);
  }

  final FileHandle file;
  final MappedByteBuffer map;
  final ByteBuf buffer;
  final int archiveOffset;
  final int archiveSize;
  final int version;
  final int blockSize;
  final int sectorSize;
  final int hashTableOffset;
  final int blockTableOffset;
  final int hashTableSize;
  final int blockTableSize;
  final Entry[] table;
  final Block[] blocks;

  int searches;
  int misses;

  public static MPQ load(FileHandle file) {
    log.info("Loading {}...", file.name());

    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(file.file(), "r");
      FileChannel fc = raf.getChannel();
      return new MPQ(file, fc);
    } catch (IOException t) {
      log.error("Failed to load {}", file, t);
      throw new RuntimeException(t); // TODO: gracefully handle this error -- some mpqs may be allowed missing
    } finally {
      IOUtils.closeQuietly(raf);
    }
  }

  static int readSafeUnsignedIntLE(ByteBuf buffer) {
    final long value = buffer.readUnsignedIntLE();
    assert value <= Integer.MAX_VALUE : "value(" + value + ") > " + Integer.MAX_VALUE;
    return (int) value;
  }

  private MPQ(FileHandle file, FileChannel fc) throws IOException {
    assert file.length() == fc.size() : "file.length(" + file.length() + ") != fileChannel.length(" + fc.size() + ")";
    this.file = file;
    this.map = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    map.order(ByteOrder.nativeOrder());
    this.buffer = Unpooled.wrappedBuffer(map);

    try {
      MDC.put("mpq", file.name());
      buffer.readerIndex(0);

      CharSequence signature = buffer.readCharSequence(4, CharsetUtil.US_ASCII);
      if (log.debugEnabled()) log.debug("signature: {}", StringEscapeUtils.escapeJava(signature.toString()));
      if (!StringUtils.equals(signature, HEADER)) {
        throw new InvalidFormat(
            "Invalid format: actual " + StringEscapeUtils.escapeJava(signature.toString())
                + ", expected " + StringEscapeUtils.escapeJava(HEADER));
      }

      archiveOffset = readSafeUnsignedIntLE(buffer);
      log.debugf("archiveOffset: 0x%08x", archiveOffset);
      archiveSize = readSafeUnsignedIntLE(buffer);
      log.debug("archiveSize: {}", FileUtils.byteCountToDisplaySize(archiveSize));
      if (archiveSize != file.length()) log.warn("archiveSize({}) != file.size({})", archiveSize, file.length());
      version = buffer.readUnsignedShortLE();
      log.debug("version: {}", version);
      if (version != 0) log.warn("version({}) != {}", version, 0);
      blockSize = buffer.readUnsignedShortLE();
      sectorSize = DISK_SECTOR_SIZE << blockSize;
      log.debug("sectorSize: {} ({})", blockSize, FileUtils.byteCountToDisplaySize(sectorSize));
      hashTableOffset = readSafeUnsignedIntLE(buffer);
      log.debugf("hashTableOffset: 0x%08x", hashTableOffset);
      blockTableOffset = readSafeUnsignedIntLE(buffer);
      log.debugf("blockTableOffset: 0x%08x", blockTableOffset);
      hashTableSize = readSafeUnsignedIntLE(buffer);
      log.debug("hashTableSize: {} ({})", hashTableSize, FileUtils.byteCountToDisplaySize(hashTableSize * Entry.SIZE));
      blockTableSize = readSafeUnsignedIntLE(buffer);
      log.debug("blockTableSize: {} ({})", blockTableSize, FileUtils.byteCountToDisplaySize(blockTableSize * Block.SIZE));

      table = new Entry[hashTableSize];
      populateHashTable(buffer.readerIndex(hashTableOffset));

      blocks = new Block[blockTableSize];
      populateBlockTable(buffer.readerIndex(blockTableOffset));
    } finally {
      MDC.remove("mpq");
    }
  }

  private void populateHashTable(ByteBuf map) {
    log.debugf("reading hash table...");

    int i = 0;
    int files = 0;
    long encryption = ((long) Decryptor.SEED2 << Integer.SIZE) | (Decryptor.HASH_TABLE_KEY & 0xFFFFFFFFL);
    final Entry[] table = this.table;

    final ByteBuf buffer = obtainHeapBuffer(sectorSize);
    try {
      final int bufferSize = buffer.capacity();
      final int hashTableBytes = hashTableSize * Entry.SIZE;
      for (int remainingBytes = hashTableBytes; remainingBytes > 0; remainingBytes -= bufferSize) {
        final int bufferBytes = Math.min(remainingBytes, bufferSize);
        assert bufferBytes % Entry.SIZE == 0 : "bufferBytes(" + bufferBytes + ") does not evenly divide Entry.SIZE(" + Entry.SIZE + ")";
        int numEntries = bufferBytes / Entry.SIZE;
        assert map.readerIndex() == hashTableOffset + hashTableBytes - remainingBytes;
        map.readBytes(buffer.setIndex(0, 0), bufferBytes);
        log.tracef(
            "decrypting hash table block %x+%x (%x)",
            hashTableOffset,
            hashTableBytes - remainingBytes,
            bufferBytes);
        final int encryptionKey = (int) (encryption & 0xFFFFFFFFL);
        final int encryptionSeed = (int) (encryption >>> Integer.SIZE);
        encryption = Decryptor.decrypt(encryptionKey, encryptionSeed, buffer);
        for (; --numEntries >= 0; i++) {
          final long key = buffer.readLongLE();
          final int locale = buffer.readUnsignedShortLE();
          final int platform = buffer.readUnsignedShortLE();
          final long block = buffer.readUnsignedIntLE();
          if (DEBUG_MODE) log.tracef("%04x %016x %04x %04x %08x", i, key, locale, platform, block);

          if (block >= archiveSize) {
            if (DEBUG_MODE && key != Entry.NULL_KEY) log.warnf(
                "bad hash table entry %08x %016x %04x %04x %08x",
                i, key, locale, platform, block);
            table[i] = block == Entry.BLOCK_UNUSED ? Entry.UNUSED : Entry.DELETED;
          } else {
            if (DEBUG_MODE) assert key != Entry.NULL_KEY : "key(" + key + ") != " + Entry.NULL_KEY;
            if (DEBUG_MODE) assert locale <= Short.MAX_VALUE : "locale(" + locale + ") > " + Short.MAX_VALUE;
            if (DEBUG_MODE) assert platform <= Short.MAX_VALUE : "platform(" + platform + ") > " + Short.MAX_VALUE;
            if (DEBUG_MODE) assert block <= Integer.MAX_VALUE : "block(" + block + ") > " + Integer.MAX_VALUE + ": key(" + key + ")";
            table[i] = new Entry(key, (short) locale, (short) platform, (int) block);
            if (DEBUG_MODE) log.debug(table[i].toString());
            files++;
          }
        }
      }
    } finally {
      buffer.release();
    }

    log.debug("hash table: {} files in {} entries", files, i);
  }

  private void populateBlockTable(ByteBuf map) {
    log.debugf("reading block table...");

    int i = 0;
    long encryption = ((long) Decryptor.SEED2 << Integer.SIZE) | (Decryptor.BLOCK_TABLE_KEY & 0xFFFFFFFFL);
    final Block[] blocks = this.blocks;

    final ByteBuf buffer = obtainHeapBuffer(sectorSize);
    try {
      final int bufferSize = buffer.capacity();
      final int blockTableBytes = blockTableSize * Block.SIZE;
      for (int remainingBytes = blockTableBytes; remainingBytes > 0; remainingBytes -= bufferSize) {
        final int bufferBytes = Math.min(remainingBytes, bufferSize);
        assert bufferBytes % Block.SIZE == 0 : "bufferBytes(" + bufferBytes + ") does not evenly divide Block.SIZE(" + Block.SIZE + ")";
        int numBlocks = bufferBytes / Block.SIZE;
        assert map.readerIndex() == blockTableOffset + blockTableBytes - remainingBytes;
        map.readBytes(buffer.setIndex(0, 0), bufferBytes);
        log.tracef(
            "decrypting block table block %x+%x (%x)",
            blockTableOffset,
            blockTableBytes - remainingBytes,
            bufferBytes);
        final int encryptionKey = (int) (encryption & 0xFFFFFFFFL);
        final int encryptionSeed = (int) (encryption >>> Integer.SIZE);
        encryption = Decryptor.decrypt(encryptionKey, encryptionSeed, buffer);
        for (; --numBlocks >= 0; i++) {
          final long offset = buffer.readUnsignedIntLE();
          if (DEBUG_MODE) assert offset <= Integer.MAX_VALUE : "offset(" + offset + ") > " + Integer.MAX_VALUE;
          final long CSize = buffer.readUnsignedIntLE();
          if (DEBUG_MODE) assert CSize <= Integer.MAX_VALUE : "CSize(" + CSize + ") > " + Integer.MAX_VALUE;
          final long FSize = buffer.readUnsignedIntLE();
          if (DEBUG_MODE) assert FSize <= Integer.MAX_VALUE : "FSize(" + FSize + ") > " + Integer.MAX_VALUE;
          final int flags = buffer.readIntLE();
          if (DEBUG_MODE) log.tracef("%04x %08x %08x %08x %08x", i, offset, CSize, FSize, flags);
          final Block block = blocks[i] = new Block((int) offset, (int) CSize, (int) FSize, flags);
          if (DEBUG_MODE) log.trace(block.toString());
        }
      }
    } finally {
      buffer.release();
    }

    log.debug("block table: {} blocks", i);
  }

  @Override
  public String toString() {
    return file.name();
  }

  public FileHandle file() {
    return file;
  }

  MappedByteBuffer map() {
    return map;
  }

  ByteBuf buffer(int offset) {
    return buffer.readerIndex(offset);
  }

  boolean contains(final long key, final int offset, final short locale) {
    return getIndex(key, offset, locale) >= 0;
  }

  int getIndex(final long key, final int offset, final short locale) {
    searches++;
    int bestId = -1;
    final Entry[] table = this.table;
    for (int i = offset & (table.length - 1);; i++, misses++) {
      final Entry entry = table[i];
      if (entry.block == Entry.BLOCK_UNUSED) {
        return bestId;
      } else if (entry.block != Entry.BLOCK_DELETED) {
        if (entry.key == key) {
          if (entry.locale == locale) {
            return i;
          } else if (bestId == -1 || entry.locale == DEFAULT_LOCALE) {
            bestId = i;
          }
        }
      }
    }
  }

  Entry getEntry(final long key, final int offset, final short locale) {
    final int index = getIndex(key, offset, locale);
    return index >= 0 ? table[index] : null;
  }

  Block getBlock(final Entry entry) {
    if (entry == null) return null;
    return blocks[entry.block];
  }

  static final class File {
    static long key(final String file) {
      int key1 = Decryptor.HASH_TABLE_KEY1.hash(file);
      int key2 = Decryptor.HASH_TABLE_KEY2.hash(file);
      return ((long) key2 << Integer.SIZE) | (key1 & 0xFFFFFFFFL);
    }

    static int offset(String file) {
      return Decryptor.HASH_TABLE_OFFSET.hash(file);
    }
  }

  static final class Entry {
    static final int SIZE = 8 + 2 + 2 + 4; // key + locale + platform + block

    static final long NULL_KEY = 0xFFFF_FFFF_FFFF_FFFFL;
    static final int NULL_LOCALE = 0xFFFF;
    static final int NULL_PLATFORM = 0xFFFF;

    static final int BLOCK_UNUSED = -1;
    static final int BLOCK_DELETED = -2;

    static final Entry UNUSED = new Entry(NULL_KEY, (short) NULL_LOCALE, (short) NULL_PLATFORM, BLOCK_UNUSED);
    static final Entry DELETED = new Entry(NULL_KEY, (short) NULL_LOCALE, (short) NULL_PLATFORM, BLOCK_DELETED);

    final long key;
    final short locale;
    final short platform;
    final int block;

    Entry(long key, short locale, short platform, int block) {
      this.key = key;
      this.locale = locale;
      this.platform = platform;
      this.block = block;
    }

    public String getLocaleString() {
      switch (locale) {
        case 0: return "Neutral / English (American)";
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
        default: return Integer.toHexString(locale);
      }
    }

    public String getPlatformString() {
      switch (platform) {
        case 0: return "DEFAULT";
        default: return Integer.toHexString(platform);
      }
    }

    public String getBlockString() {
      switch (block) {
        case BLOCK_UNUSED: return "UNUSED";
        case BLOCK_DELETED: return "DELETED";
        default: return Integer.toHexString(block);
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("key", StringUtils.leftPad(Long.toHexString(key), 16, '0'))
          .append("locale", getLocaleString())
          .append("platform", getPlatformString())
          .append("block", getBlockString())
          .build();
    }
  }

  static final class Block {
    static final int SIZE = 4 + 4 + 4 + 4; // offset + CSize + FSize + flags

    static final int FLAG_IMPLODE       = 0x00000100;
    static final int FLAG_COMPRESSED    = 0x00000200;
    static final int FLAG_ENCRYPTED     = 0x00010000;
    static final int FLAG_FIX_KEY       = 0x00020000;
    static final int FLAG_PATCH_FILE    = 0x00100000;
    static final int FLAG_SINGLE_UNIT   = 0x01000000;
    static final int FLAG_DELETE_MARKER = 0x02000000;
    static final int FLAG_SECTOR_CRC    = 0x04000000;
    static final int FLAG_EXISTS        = 0x80000000;

    final int offset;
    final int CSize;
    final int FSize;
    final int flags;

    Block(int offset, int CSize, int FSize, int flags) {
      this.offset = offset;
      this.CSize = CSize;
      this.FSize = FSize;
      this.flags = flags;
    }

    public String getFlagsString() {
      if (flags == 0) return "0";
      StringBuilder builder = new StringBuilder(64);
      builder.append(StringUtils.leftPad(Integer.toHexString(flags), 8, '0'));
      builder.append(" (");
      final int startingLen = builder.length();
      if ((flags & FLAG_EXISTS) == FLAG_EXISTS) builder.append("EXISTS|");
      if ((flags & FLAG_SECTOR_CRC) == FLAG_SECTOR_CRC) builder.append("SECTOR_CRC|");
      if ((flags & FLAG_DELETE_MARKER) == FLAG_DELETE_MARKER) builder.append("DELETE_MARKER|");
      if ((flags & FLAG_SINGLE_UNIT) == FLAG_SINGLE_UNIT) builder.append("SINGLE_UNIT|");
      if ((flags & FLAG_PATCH_FILE) == FLAG_PATCH_FILE) builder.append("PATCH_FILE|");
      if ((flags & FLAG_FIX_KEY) == FLAG_FIX_KEY) builder.append("FIX_KEY|");
      if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) builder.append("ENCRYPTED|");
      if ((flags & FLAG_COMPRESSED) == FLAG_COMPRESSED) builder.append("COMPRESSED|");
      if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE) builder.append("IMPLODE|");
      if (builder.length() > startingLen) {
        builder.setCharAt(builder.length() - 1, ')');
      } else {
        builder.append(')');
      }

      return builder.toString();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("offset", Integer.toHexString(offset))
          .append("CSize", FileUtils.byteCountToDisplaySize(CSize))
          .append("FSize", FileUtils.byteCountToDisplaySize(FSize))
          .append("flags", getFlagsString())
          .build();
    }
  }
}
