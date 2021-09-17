package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_LOCALE;
import static com.riiablo.mpq_bytebuf.Mpq.HashTable.BLOCK_UNUSED;
import static com.riiablo.mpq_bytebuf.Mpq.HashTable.NULL_KEY;
import static com.riiablo.mpq_bytebuf.Mpq.HashTable.NULL_LOCALE;
import static com.riiablo.mpq_bytebuf.Mpq.HashTable.NULL_PLATFORM;

class MpqTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf", Level.TRACE);
  }

  @Test
  @DisplayName("v1.14 d2data header")
  void version_0_header() {
    ByteBuf in = Unpooled.wrappedBuffer(RiiabloTest.toByteArray(new short[] {
        0x4D, 0x50, 0x51, 0x1A, 0x20, 0x00, 0x00, 0x00,
        0xBF, 0x89, 0x0B, 0x10, 0x00, 0x00, 0x03, 0x00,
        0xCF, 0xE1, 0x04, 0x10, 0xCF, 0xE1, 0x08, 0x10,
        0x00, 0x40, 0x00, 0x00, 0x7F, 0x2A, 0x00, 0x00,
    }));
    Mpq mpq = Mpq.readHeader(null, in);
    assertEquals(0x00000020, mpq.archiveOffset);
    assertEquals(0x100B89BF, mpq.archiveSize);
    assertEquals(0x0000, mpq.version);
    assertEquals(0x0003, mpq.blockSize);
    assertEquals(0x1000, mpq.sectorSize);
    assertEquals(0x1004E1CF, mpq.hashTableOffset);
    assertEquals(0x00004000, mpq.hashTableSize);
    assertEquals(0x1008E1CF, mpq.blockTableOffset);
    assertEquals(0x00002A7F, mpq.blockTableSize);
  }

  private static void assertHashTableEntryEquals(
      Mpq.HashTable hashTable,
      int i,
      long key,
      short locale,
      short platform,
      int blockId
  ) {
    assertEquals(key, hashTable.key[i]);
    assertEquals(locale, hashTable.locale[i]);
    assertEquals(platform, hashTable.platform[i]);
    assertEquals(blockId, hashTable.blockId[i]);
  }

  @Test
  @DisplayName("v1.14 d2data hash table")
  void read_hash_table() {
    final int sectorSize = 0x1000;
    FileHandle mpq_hashtable_in = Gdx.files.internal("test/mpq_hashtable_in.bin");
    ByteBuf in = Unpooled.wrappedBuffer(mpq_hashtable_in.readBytes());
    Mpq.HashTable hashTable = Mpq.readHashTable(in, 8, UnpooledByteBufAllocator.DEFAULT, sectorSize);
    assertHashTableEntryEquals(hashTable, 0x0, NULL_KEY, NULL_LOCALE, NULL_PLATFORM, BLOCK_UNUSED);
    assertHashTableEntryEquals(hashTable, 0x1, NULL_KEY, NULL_LOCALE, NULL_PLATFORM, BLOCK_UNUSED);
    assertHashTableEntryEquals(hashTable, 0x2, NULL_KEY, NULL_LOCALE, NULL_PLATFORM, BLOCK_UNUSED);
    assertHashTableEntryEquals(hashTable, 0x3, 0x198ec89e_331aa514L, (short) 0, (short) 0, 0x1abb);
    assertHashTableEntryEquals(hashTable, 0x4, 0x8fba4e91_501a2383L, (short) 0, (short) 0, 0x1fe8);
    assertHashTableEntryEquals(hashTable, 0x5, 0x3c6d6a14_6f3a61d6L, (short) 0, (short) 0, 0x02e9);
    assertHashTableEntryEquals(hashTable, 0x6, 0x9c047a76_43d1f9e5L, (short) 0, (short) 0, 0x10d5);
    assertHashTableEntryEquals(hashTable, 0x7, 0x34e0e69e_65854515L, (short) 0, (short) 0, 0x0c69);
  }

  private static void assertBlockTableEntryEquals(
      Mpq.Block[] blockTable,
      int i,
      int offset,
      int CSize,
      int FSize,
      int flags
  ) {
    Mpq.Block block = blockTable[i];
    assertEquals(offset, block.offset);
    assertEquals(CSize, block.CSize);
    assertEquals(FSize, block.FSize);
    assertEquals(flags, block.flags);
  }

  @Test
  @DisplayName("v1.14 d2data block table")
  void read_block_table() {
    final int sectorSize = 0x1000;
    FileHandle mpq_blocktable_in = Gdx.files.internal("test/mpq_blocktable_in.bin");
    ByteBuf in = Unpooled.wrappedBuffer(mpq_blocktable_in.readBytes());
    Mpq.Block[] blockTable = Mpq.readBlockTable(in, 8, UnpooledByteBufAllocator.DEFAULT, sectorSize);
    assertBlockTableEntryEquals(blockTable, 0x0, 0x0020, 0x092a, 0x1cbf, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x1, 0x094a, 0x005c, 0x0084, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x2, 0x09a6, 0x18e6, 0x802a, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x3, 0x228c, 0x0035, 0x002d, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x4, 0x22c1, 0x5e6a, 0x1dca6, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x5, 0x812b, 0x01a9, 0x086a, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x6, 0x82d4, 0x00c1, 0x010b, 0x80000200);
    assertBlockTableEntryEquals(blockTable, 0x7, 0x8395, 0x0247, 0x05f0, 0x80000200);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "patch_d2.mpq",
      "d2exp.mpq",
      "d2xmusic.mpq",
      "d2xtalk.mpq",
      "d2xvideo.mpq",
      "d2data.mpq",
      "d2char.mpq",
      "d2sfx.mpq",
      "d2music.mpq",
      "d2speech.mpq",
      "d2video.mpq",
  })
  void open(String filename) {
    FileHandle handle = Riiablo.home.child(filename);
    Mpq mpq = Mpq.open(handle);
    assertNotNull(mpq);
    mpq.dispose();
  }

  @Test
  @DisplayName("dispose without MpqFileHandle reference leaks")
  void dispose_without_leaks() {
    FileHandle handle = Riiablo.home.child("d2data.mpq");
    Mpq mpq = Mpq.open(handle);
    MpqFileHandle h = mpq.open(null, "data\\global\\excel\\monstats.txt", DEFAULT_LOCALE);
    h.release();
    mpq.dispose();
  }

  @Test
  @DisplayName("dispose with leaked MpqFileHandle references")
  void dispose_with_leaks() {
    FileHandle handle = Riiablo.home.child("d2data.mpq");
    Mpq mpq = Mpq.open(handle);
    MpqFileHandle h = mpq.open(null, "data\\global\\excel\\monstats.txt", DEFAULT_LOCALE);
    try {
      mpq.dispose();
    } finally {
      h.release();
    }
  }

  static class NestedMpqTest {
    final String name;
    Mpq mpq;

    NestedMpqTest(TestInfo testInfo) {
      this(testInfo.getDisplayName() + ".mpq");
    }

    NestedMpqTest(String name) {
      this.name = name;
    }

    @BeforeAll
    void beforeAll() {
      FileHandle handle = Riiablo.home.child(name);
      mpq = Mpq.open(handle);
    }

    @AfterAll
    void afterAll() {
      mpq.dispose();
    }
  }

  @Nested
  @TestInstance(PER_CLASS)
  class d2data extends NestedMpqTest {
    d2data(TestInfo testInfo) {
      super(testInfo);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\excel\\armor.txt",
        "data\\global\\excel\\monstats.txt",
        "data\\global\\items\\flpbuc.DC6",
        "data\\global\\missiles\\IceBolt.dcc",
        "data\\global\\monsters\\DC\\COF\\DCDDHTH.COF",
        "data\\global\\tiles\\ACT1\\TOWN\\townN1.ds1",
        "data\\local\\font\\LATIN\\font42.DC6",
    })
    void contains(String filename) {
      assertTrue(mpq.contains(filename, DEFAULT_LOCALE));
    }
  }

  @Nested
  @TestInstance(PER_CLASS)
  class d2char extends NestedMpqTest {
    d2char(TestInfo testInfo) {
      super(testInfo);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\CHARS\\AM\\COF\\ama11hs.cof",
        "data\\global\\CHARS\\AM\\S2\\AMS2HVYA21HT.dcc",
        "data\\global\\CHARS\\BA\\RH\\baRHAXEsc1HS.dcc",
        "data\\global\\CHARS\\BA\\LG\\BALGHVYKKHTH.dcc",
        "data\\global\\CHARS\\BA\\COF\\BANU2HT.COF",
        "data\\global\\CHARS\\NE\\S1\\NES1HVYNUHTH.dcc",
        "data\\global\\CHARS\\NE\\TR\\NETRLITA12HS.dcc",
        "data\\global\\CHARS\\PA\\HD\\PAHDBHMGH1HS.dcc",
        "data\\global\\CHARS\\SO\\SH\\SOSHBSHKKHTH.dcc",
    })
    void contains(String filename) {
      assertTrue(mpq.contains(filename, DEFAULT_LOCALE));
    }
  }
}
