package com.riiablo.mpq_bytebuf.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

import static com.riiablo.mpq_bytebuf.util.Decryptor.BLOCK_TABLE_KEY;
import static com.riiablo.mpq_bytebuf.util.Decryptor.HASH_ENCRYPTION_KEY;
import static com.riiablo.mpq_bytebuf.util.Decryptor.HASH_TABLE_KEY;
import static com.riiablo.mpq_bytebuf.util.Decryptor.SEED2;

public class DecryptorTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.Decryptor", Level.TRACE);
  }
  @Test
  public void HASH_TABLE_KEY() {
    assertEquals(HASH_TABLE_KEY, HASH_ENCRYPTION_KEY.hash("(hash table)"));
  }

  @Test
  public void BLOCK_TABLE_KEY() {
    assertEquals(BLOCK_TABLE_KEY, HASH_ENCRYPTION_KEY.hash("(block table)"));
  }

  @Test
  public void decrypt_hash_table() {
    FileHandle hashtable_in = Gdx.files.internal("test/hashtable_in.bin");
    ByteBuf actual = Unpooled.wrappedBuffer(hashtable_in.readBytes());
    Decryptor.decrypt(HASH_TABLE_KEY, SEED2, actual);

    FileHandle hashtable_out = Gdx.files.internal("test/hashtable_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(hashtable_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }

  @Test
  public void decrypt_block_table() {
    FileHandle blocktable_in = Gdx.files.internal("test/blocktable_in.bin");
    ByteBuf actual = Unpooled.wrappedBuffer(blocktable_in.readBytes());
    Decryptor.decrypt(BLOCK_TABLE_KEY, SEED2, actual);

    FileHandle blocktable_out = Gdx.files.internal("test/blocktable_out.bin");
    ByteBuf expected = Unpooled.wrappedBuffer(blocktable_out.readBytes());
    assertTrue(ByteBufUtil.equals(expected, actual));
  }
}
