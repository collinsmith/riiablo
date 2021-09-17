package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;

import static com.riiablo.mpq_bytebuf.Decrypter.BLOCK_TABLE_KEY;
import static com.riiablo.mpq_bytebuf.Decrypter.HASH_ENCRYPTION_KEY;
import static com.riiablo.mpq_bytebuf.Decrypter.HASH_TABLE_KEY;
import static com.riiablo.mpq_bytebuf.Decrypter.NUM_TABLES;
import static com.riiablo.mpq_bytebuf.Decrypter.NUM_VALUES;
import static com.riiablo.mpq_bytebuf.Decrypter.SEED2;

class DecrypterTest extends RiiabloTest {
  @Test
  @Disabled
  void generate_tables() {
    final int[][] TABLES = new int[NUM_TABLES][NUM_VALUES];

    int seed = 0x00100001;
    for (int value = 0; value < NUM_VALUES; value++) {
      for (int table = 0; table < NUM_TABLES; table++) {
        final short seed1 = (short) (seed = (seed * 125 + 3) % 0x2AAAAB);
        final short seed2 = (short) (seed = (seed * 125 + 3) % 0x2AAAAB);
        TABLES[table][value] = (int) seed1 << 16 | ((int) seed2) & 0xFFFF;
      }
    }

    for (int i = 0; i < NUM_TABLES; i++) {
      System.out.printf("{ // Table %d (+0x%03x)%n", i, i << 8);
      int k = 0;
      for (int j : TABLES[i]) {
        System.out.printf("0x%08x, ", j);
        if (++k % 8 == 0) {
          System.out.println();
        }
      }
      System.out.println("},");
    }
  }

  @Test
  @Disabled
  void generate_charMap() {
    final byte[] charMap = new byte[0x100];
    for (int i = 0; i < 0x100; i++) {
      charMap[i] = (byte) i;
    }

    for (int i = 'a'; i <= 'z'; i++) {
      charMap[i] &= 0xdf; // 'a' -> 'A'
    }

    charMap['/'] = '\\';

    final int len = 8;
    for (int i = 0, s = charMap.length; i < s; i++) {
      if (i > Byte.MAX_VALUE) {
        System.out.print("(byte) ");
      }

      System.out.printf("0x%02x, ", charMap[i]);
      if (i % len == (len - 1)) {
        System.out.println();
      }
    }
  }

  @ParameterizedTest
  @Disabled
  @ValueSource(strings = {
      "(hash table)",
      "(block table)",
  })
  void generate_hash_table_keys(String str) {
    final int hash = HASH_ENCRYPTION_KEY.hash(str);
    System.out.printf("%s=0x%08x", str, hash);
  }

  @Test
  void hash_table_key() {
    final int hash = HASH_ENCRYPTION_KEY.hash("(hash table)");
    assertEquals(0xc3af3770, hash);
  }

  @Test
  void block_table_key() {
    final int hash = HASH_ENCRYPTION_KEY.hash("(block table)");
    assertEquals(0xec83b3a3, hash);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "(hash table),0xc3af3770",
      "(block table),0xec83b3a3",
  }, delimiter = ',')
  void hash_encryption_keys(String str, String expectedStr) {
    final int actual = HASH_ENCRYPTION_KEY.hash(str);
    final int expected = Integer.parseUnsignedInt(StringUtils.removeStart(expectedStr, "0x"), 16);
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "DATA\\GLOBAL\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
  })
  void fix_equal(String expected) {
    final String actual = Decrypter.fix(expected);
    assertSame(expected, actual);
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "dATA/GLOBAL\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC,DATA\\GLOBAL\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
      "data/global/chars\\ba\\lg\\BALGlittnhth.dcc,DATA\\GLOBAL\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
  }, delimiter = ',')
  void fix_not_equal(String key, String expected) {
    final String actual = Decrypter.fix(key);
    assertNotSame(key, actual);
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/hashtable_in.bin,test/hashtable_out.bin",
  })
  void decrypt_hash_table(String in, String out) {
    FileHandle handle_in = Gdx.files.internal(in);
    ByteBuf actual = Unpooled.wrappedBuffer(handle_in.readBytes());
    Decrypter.decrypt(HASH_TABLE_KEY, SEED2, actual);

    FileHandle handle_out = Gdx.files.internal(out);
    ByteBuf expected = Unpooled.wrappedBuffer(handle_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "test/blocktable_in.bin,test/blocktable_out.bin",
  })
  void decrypt_block_table(String in, String out) {
    FileHandle handle_in = Gdx.files.internal(in);
    ByteBuf actual = Unpooled.wrappedBuffer(handle_in.readBytes());
    Decrypter.decrypt(BLOCK_TABLE_KEY, SEED2, actual);

    FileHandle handle_out = Gdx.files.internal(out);
    ByteBuf expected = Unpooled.wrappedBuffer(handle_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }
}
