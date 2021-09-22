package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

import static com.riiablo.mpq_bytebuf.Decrypter.BLOCK_TABLE_KEY;
import static com.riiablo.mpq_bytebuf.Decrypter.HASH_TABLE_KEY;
import static com.riiablo.mpq_bytebuf.Decrypter.SEED2;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

public final class Mpq implements Disposable {
  private static final Logger log = LogManager.getLogger(Mpq.class);

  private static final boolean DEBUG_MODE = !true;

  static final byte[] SIGNATURE = { 'M', 'P', 'Q', 0x1A };

  static final int DISK_SECTOR_SIZE = 0x200; // 512 B

  public static final short DEFAULT_LOCALE = 0x0000;
  public static final short DEFAULT_PLATFORM = 0x0000;

  /**
   * TODO: sector size of all D2 mpq archives are 4 KB, but technically this can
   *       change per-mpq. Should look into efficient way to share allocators
   *       for same sector size mpqs.
   */
  static final ByteBufAllocator ALLOC = new PooledByteBufAllocator(
      PooledByteBufAllocator.defaultNumHeapArena(),
      PooledByteBufAllocator.defaultNumDirectArena(),
      0x1000, // 4 KB (mpq sector size)
      PooledByteBufAllocator.defaultMaxOrder());

  public static String localeToString(short locale) {
    switch (locale) {
      case 0: return "en";
      case 0x404: return "zh-TW";
      case 0x405: return "cs-CZ";
      case 0x407: return "de-DE";
      case 0x409: return "en-US";
      case 0x40a: return "es-ES";
      case 0x40c: return "fr-FR";
      case 0x410: return "it-IT";
      case 0x411: return "ja-JP";
      case 0x412: return "ko-KR";
      case 0x415: return "pl-PL";
      case 0x416: return "pt-BR";
      case 0x419: return "ru-RU";
      case 0x809: return "en-GB";
      default: return String.format("0x%x", locale);
    }
  }

  public static String platformToString(short platform) {
    switch (platform) {
      case 0: return "win32";
      default: return String.format("0x%x", platform);
    }
  }

  ByteBufAllocator alloc() {
    return ALLOC;
  }

  ByteBuf sectorBuffer() {
    return sectorBuffer(sectorSize);
  }

  ByteBuf sectorBuffer(int initialCapacity) {
    return ALLOC.heapBuffer(initialCapacity, sectorSize);
  }

  ByteBuf fileBuffer(int size) {
    return ALLOC.heapBuffer(size, size);
  }

  static Mpq open(FileHandle handle) {
    log.info("Loading {}...", handle.name());
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(handle.file(), "r");
      FileChannel fc = raf.getChannel();
      return open(handle, fc);
    } catch (IOException t) {
      log.error("Failed to load {}", handle, t);
      throw new RuntimeException(t); // TODO: gracefully handle this error -- some mpqs may be allowed missing
    } finally {
      IOUtils.closeQuietly(raf);
    }
  }

  static Mpq open(FileHandle handle, FileChannel fc) throws IOException {
    assert handle.length() == fc.size() : "handle.length(" + handle.length() + ") != fileChannel.length(" + fc.size() + ")";
    try {
      MDC.put("mpq", handle.name());
      ByteBuf map = Unpooled
          .wrappedBuffer(fc
              .map(READ_ONLY, 0, fc.size())
              .order(ByteOrder.nativeOrder()));
      return open(handle, map);
    } finally {
      MDC.remove("mpq");
    }
  }

  static Mpq open(FileHandle handle, ByteBuf buffer) {
    Mpq mpq = readHeader(handle, buffer);
    if (mpq.archiveSize != buffer.capacity()) {
      log.warn(
          "mpq.archiveSize({}) != buffer.capacity({})",
          mpq.archiveSize, buffer.capacity());
    }

    mpq.readHashTable();
    mpq.readBlockTable();
    return mpq;
  }

  static Mpq readHeader(FileHandle handle, ByteBuf buffer) {
    ByteInput in = ByteInput.wrap(buffer);
    in.readSignature(SIGNATURE);
    int archiveOffset = in.readSafe32u();
    int archiveSize = in.readSafe32u();
    int version = in.readSafe16u();
    if (version != 0) log.warn("version({}) != {}", version, 0);
    int blockSize = in.readSafe16u();
    int sectorSize = DISK_SECTOR_SIZE << blockSize;
    int hashTableOffset = in.readSafe32u();
    int blockTableOffset = in.readSafe32u();
    int hashTableSize = in.readSafe32u();
    int blockTableSize = in.readSafe32u();
    Mpq mpq = new Mpq(
        handle, // nullable
        buffer,
        archiveOffset,
        archiveSize,
        version,
        blockSize,
        sectorSize,
        hashTableOffset,
        hashTableSize,
        blockTableOffset,
        blockTableSize
    );
    return mpq;
  }

  final FileHandle handle;
  final String name;
  ByteBuf map; // owned, mapped, read-only, unreleasable
  final int archiveOffset;
  final int archiveSize;
  final int version;
  final int blockSize;
  final int sectorSize;
  final int hashTableSize;
  final int hashTableOffset;
  final int blockTableSize;
  final int blockTableOffset;

  HashTable hashTable;
  Block[] blockTable;

  Mpq(
      FileHandle handle,
      ByteBuf map,
      int archiveOffset,
      int archiveSize,
      int version,
      int blockSize,
      int sectorSize,
      int hashTableOffset,
      int hashTableSize,
      int blockTableOffset,
      int blockTableSize
  ) {
    this.handle = handle;
    this.name = handle == null ? null : handle.nameWithoutExtension();
    this.map = Unpooled.unreleasableBuffer(map);
    this.archiveOffset = archiveOffset;
    this.archiveSize = archiveSize;
    this.version = version;
    this.blockSize = blockSize;
    this.sectorSize = sectorSize;
    this.hashTableOffset = hashTableOffset;
    this.hashTableSize = hashTableSize;
    this.blockTableOffset = blockTableOffset;
    this.blockTableSize = blockTableSize;
  }

  Object lock() {
    return this;
  }

  @Override
  public void dispose() {
    map.unwrap().release();
    map = null;

    /**
     * mpq mapped byte buffer has been released, but some mpq files may have
     * been leaked and referenced somewhere in code. Below will wipe the
     * retained handles from the hash table, dispose their slices of the mpq
     * mapped byte buffer, and log an error for each file which still has
     * non-zero number of references.
     */
    final MpqFileHandle[] handles = hashTable.handle;
    for (int i = 0, s = hashTableSize; i < s; i++) {
      dispose(handles, i);
    }
  }

  /**
   * disposes the handle given by its hash table index and returns whether the
   * handle was leaked (disposed with non-zero number of remaining references).
   */
  boolean dispose(final int index) {
    return dispose(hashTable.handle, index);
  }

  /**
   * disposes the handle given by its hash table index and returns whether the
   * handle was leaked (disposed with non-zero number of remaining references).
   */
  boolean dispose(final MpqFileHandle[] handles, final int index) {
    final MpqFileHandle handle = handles[index];
    if (handle == null) return false;
    try {
      handle.dispose();
      final boolean leaked = handle.refCnt() != 0;
      if (leaked) log.error("{} was disposed but reference(s) will outlive its mpq", handle);
      return leaked;
    } finally {
      handles[index] = null;
    }
  }

  private void readHashTable() {
    if (hashTable != null) throw new IllegalStateException("hash table already initialized");
    final int hashTableBytes = hashTableSize * HashTable.ENTRY_SIZE;
    final ByteBuf in = map.slice(hashTableOffset, hashTableBytes);
    try {
      hashTable = readHashTable(in, hashTableSize, alloc(), sectorSize);
    } finally {
      in.release();
    }
  }

  static HashTable readHashTable(ByteBuf in, int size, ByteBufAllocator alloc, int sectorSize) {
    log.debug("Reading hash table...");

    int i = 0;
    int files = 0;
    long state = Decrypter.state(HASH_TABLE_KEY, SEED2);

    final int hashTableBytes = size * HashTable.ENTRY_SIZE;
    assert in.readableBytes() >= hashTableBytes : "in.readableBytes(" + in.readableBytes() + ") < hashTableBytes(" + hashTableBytes + ")";
    final HashTable hashTable = new HashTable(size);
    final ByteBuf buffer = alloc.heapBuffer(sectorSize, sectorSize);
    try {
      for (int remainingBytes = hashTableBytes; remainingBytes > 0; remainingBytes -= sectorSize) {
        final int bufferBytes = Math.min(remainingBytes, sectorSize);
        assert bufferBytes % HashTable.ENTRY_SIZE == 0 : "bufferBytes(" + bufferBytes + ") does not evenly divide HashTable.ENTRY_SIZE(" + HashTable.ENTRY_SIZE + ")";
        int numEntries = bufferBytes / HashTable.ENTRY_SIZE;
        assert in.readerIndex() == hashTableBytes - remainingBytes;
        in.readBytes(buffer.clear(), bufferBytes);
        log.tracef("decrypting hash table entry +%x (%x)", hashTableBytes - remainingBytes, bufferBytes);
        state = Decrypter.decrypt(state, buffer);
        for (; --numEntries >= 0; i++) {
          final long key = buffer.readLongLE();
          final short locale = buffer.readShortLE();
          final short platform = buffer.readShortLE();
          final int blockId = buffer.readIntLE();
          if (DEBUG_MODE) log.tracef("%04x %08x %08x %04x %04x %08x", i, key & 0xFFFF_FFFFL, key >>> 32, locale, platform, blockId);
          switch (blockId) {
            case HashTable.BLOCK_DELETED:
            case HashTable.BLOCK_UNUSED:
              hashTable.remove(i, blockId);
              break;
            default:
              hashTable.put(i, key, locale, platform, blockId);
              files++;
          }
        }
      }
    } finally {
      buffer.release();
    }

    log.debug("hash table: {} files in {} entries", files, i);
    return hashTable;
  }

  private void readBlockTable() {
    if (blockTable != null) throw new IllegalStateException("block table already initialized");
    final int blockTableBytes = blockTableSize * Block.SIZE;
    final ByteBuf in = map.slice(blockTableOffset, blockTableBytes);
    try {
      blockTable = readBlockTable(in, blockTableSize, alloc(), sectorSize);
    } finally {
      in.release();
    }
  }

  static Block[] readBlockTable(ByteBuf in, int size, ByteBufAllocator alloc, int sectorSize) {
    log.debug("Reading block table...");

    int i = 0;
    long state = Decrypter.state(BLOCK_TABLE_KEY, SEED2);

    final int blockTableBytes = size * Block.SIZE;
    assert in.readableBytes() >= blockTableBytes : "in.readableBytes(" + in.readableBytes() + ") < blockTableBytes(" + blockTableBytes + ")";
    final Block[] blockTable = new Block[size];
    final ByteBuf buffer = alloc.heapBuffer(sectorSize, sectorSize);
    try {
      for (int remainingBytes = blockTableBytes; remainingBytes > 0; remainingBytes -= sectorSize) {
        final int bufferBytes = Math.min(remainingBytes, sectorSize);
        assert bufferBytes % Block.SIZE == 0 : "bufferBytes(" + bufferBytes + ") does not evenly divide Block.SIZE(" + Block.SIZE + ")";
        int numEntries = bufferBytes / Block.SIZE;
        assert in.readerIndex() == blockTableBytes - remainingBytes;
        in.readBytes(buffer.clear(), bufferBytes);
        log.tracef("decrypting block table entry +%x (%x)", blockTableBytes - remainingBytes, bufferBytes);
        state = Decrypter.decrypt(state, buffer);
        for (; --numEntries >= 0; i++) {
          final int offset = buffer.readIntLE();
          final int CSize = buffer.readIntLE();
          final int FSize = buffer.readIntLE();
          final int flags = buffer.readIntLE();
          if (DEBUG_MODE) log.tracef("%04x %08x %08x %08x %08x", i, offset, CSize, FSize, flags);
          blockTable[i] = new Block(offset, CSize, FSize, flags);
        }
      }
    } finally {
      buffer.release();
    }

    log.debug("block table: {} blocks", i);
    return blockTable;
  }

  @Override
  public String toString() {
    return name();
  }

  public String name() {
    return name;
  }

  public FileHandle handle() {
    return handle;
  }

  public ByteBuf map() {
    return map;
  }

  /** @deprecated for use in tests, use MpqFileResolver instead */
  @Deprecated
  boolean contains(final String filename, final short locale) {
    final long key = HashTable.key(filename);
    final int hash = HashTable.hash(filename);
    return contains(key, hash, locale);
  }

  boolean contains(final long key, final int hash, final short locale) {
    return hashTable.get(key, hash, locale) >= 0;
  }

  /** @deprecated for use in tests, use MpqFileResolver instead */
  @Deprecated
  int get(final CharSequence filename, final short locale) {
    final long key = HashTable.key(filename);
    final int hash = HashTable.hash(filename);
    return get(key, hash, locale);
  }

  int get(final long key, final int hash, final short locale) {
    return hashTable.get(key, hash, locale);
  }

  /** @deprecated for use in tests, use MpqFileResolver instead */
  @Deprecated
  MpqFileHandle open(final DecoderExecutorGroup decoder, final CharSequence filename, final short locale) {
    final int index = get(filename, locale);
    return open(decoder, index, filename);
  }

  MpqFileHandle open(final DecoderExecutorGroup decoder, final int index, final CharSequence filename) {
    return hashTable.open(decoder, this, index, filename);
  }

  static int encryptionKey(final String filename, final int flags, final int offset, final int FSize) {
    if ((flags & Block.FLAG_ENCRYPTED) == Block.FLAG_ENCRYPTED) {
      final String basename = FilenameUtils.getName(filename);
      final int encryptionKey = Decrypter.HASH_ENCRYPTION_KEY.hash(basename);
      return (flags & Block.FLAG_FIX_KEY) == Block.FLAG_FIX_KEY
          ? (encryptionKey + offset) ^ FSize
          : encryptionKey;
    }

    return 0;
  }

  static final class HashTable {
    static final int ENTRY_SIZE = 0x10; // key1 + key2 + locale + platform + sector

    static final long NULL_KEY = 0xFFFF_FFFF_FFFF_FFFFL;
    static final short NULL_LOCALE = (short) 0xFFFF;
    static final short NULL_PLATFORM = (short) 0xFFFF;

    static final int BLOCK_UNUSED = -1;
    static final int BLOCK_DELETED = -2;

    static long key(final CharSequence filename) {
      int key1 = Decrypter.HASH_TABLE_KEY1.hash(filename);
      int key2 = Decrypter.HASH_TABLE_KEY2.hash(filename);
      return ((long) key2 << Integer.SIZE) | (key1 & 0xFFFFFFFFL);
    }

    static int hash(final CharSequence filename) {
      return Decrypter.HASH_TABLE_OFFSET.hash(filename);
    }

    final int size;
    final long[] key;
    final short[] locale;
    final short[] platform;
    final int[] blockId;
    final MpqFileHandle[] handle;

    int searches;
    int misses;

    HashTable(int size) {
      this.size = size;
      this.key = new long[size];
      this.locale = new short[size];
      this.platform = new short[size];
      this.blockId = new int[size];
      this.handle = new MpqFileHandle[size];
    }

    void put(final int i, final long key, final short locale, final short platform, final int blockId) {
      this.key[i] = key;
      this.locale[i] = locale;
      this.platform[i] = platform;
      this.blockId[i] = blockId;
    }

    void remove(final int i, final int blockId) {
      assert blockId < 0 : "removed blockIds should be <0, was: " + blockId;
      this.key[i] = NULL_KEY;
      this.locale[i] = NULL_LOCALE;
      this.platform[i] = NULL_PLATFORM;
      this.blockId[i] = blockId;
    }

    int get(final long key, final int hash, final short locale) {
      // FIXME: misses not incremented if first i in tables[i] is unused
      searches++;
      int bestId = -1;
      final long[] keys = this.key;
      final short[] locales = this.locale;
      final short[] platforms = this.platform;
      final int[] blockIds = this.blockId;
      short tLocale;
      for (int i = hash & (size - 1), s = size; i < s; i++, misses++) {
        final int blockId = blockIds[i];
        switch (blockId) {
          case BLOCK_DELETED:
            continue;
          case BLOCK_UNUSED:
            return bestId;
          default:
            if (keys[i] != key) continue;
            tLocale = locales[i];
            if (tLocale == locale) {
              return i;
            } else if (bestId == -1 || tLocale == DEFAULT_LOCALE) {
              bestId = i;
            }
        }
      }

      return bestId;
    }

    MpqFileHandle open(final DecoderExecutorGroup decoder, final Mpq mpq, final int index, final CharSequence filename) {
      MpqFileHandle handle = this.handle[index];
      if (handle == null) {
        final int blockId = this.blockId[index];
        final Block block = mpq.blockTable[blockId];
        return this.handle[index] = new MpqFileHandle(
            decoder,
            mpq,
            index,
            filename.toString(),
            block.offset,
            block.CSize,
            block.FSize,
            block.flags);
      }
      return handle.retain();
    }
  }

  static final class Block {
    static final int SIZE = 0x10; // offset + CSize + FSize + flags

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
      assert offset >= 0 : "offset(" + offset + ") < " + 0;
      assert CSize >= 0 : "CSize(" + CSize + ") < " + 0;
      assert FSize >= 0 : "FSize(" + FSize + ") < " + 0;
      this.offset = offset;
      this.CSize = CSize;
      this.FSize = FSize;
      this.flags = flags;
    }
  }
}
