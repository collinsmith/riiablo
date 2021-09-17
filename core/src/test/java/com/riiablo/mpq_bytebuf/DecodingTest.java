package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.mpq_bytebuf.DecodingService.Callback;

import static com.riiablo.mpq_bytebuf.DecodingService.IGNORE;
import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_LOCALE;

class DecodingTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MpqFileHandle", Level.TRACE);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.DecodingService", Level.TRACE);
  }

  @Nested
  @TestInstance(PER_CLASS)
  class d2char extends MpqTest.NestedMpqTest {
    d2char(TestInfo testInfo) {
      super(testInfo);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
        "DATA\\GLOBAL\\CHARS\\PA\\LA\\PALALITTN1HS.DCC",
    })
    void decode(String in) {
      DecodingService decoder = new DecodingService(4);
      try {
        MpqFileHandle handle = mpq.open(decoder, in, DEFAULT_LOCALE);
        try {
          ByteBuf actual = handle.buffer();

          FileHandle handle_out = testAsset(in);
          ByteBuf expected = Unpooled.wrappedBuffer(handle_out.readBytes());
          assertTrue(ByteBufUtil.equals(expected, actual));
        } finally {
          handle.release();
        }
      } finally {
        decoder.gracefulShutdown();
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
        "DATA\\GLOBAL\\CHARS\\PA\\LA\\PALALITTN1HS.DCC",
    })
    void decode_async(String in) {
      DecodingService decoder = new DecodingService(4);
      try {
      MpqFileHandle handle = mpq.open(decoder, in, DEFAULT_LOCALE);
        try {
          final ByteBuf actual;
          Future<ByteBuf> future = handle.bufferAsync(IGNORE);
          try {
            actual = future.get();
          } catch (InterruptedException | ExecutionException t) {
            fail(t);
            return;
          }

          FileHandle handle_out = testAsset(in);
          ByteBuf expected = Unpooled.wrappedBuffer(handle_out.readBytes());
          assertTrue(ByteBufUtil.equals(expected, actual));
        } finally {
          handle.release();
        }
      } finally {
        decoder.gracefulShutdown();
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
        "DATA\\GLOBAL\\CHARS\\PA\\LA\\PALALITTN1HS.DCC",
    })
    void decode_callback(String in) {
      DecodingService decoder = new DecodingService(4);
      try {
        MpqFileHandle handle = mpq.open(decoder, in, DEFAULT_LOCALE);
        try {
          final AtomicReference<ByteBuf> actual = new AtomicReference<>();
          Future<ByteBuf> future = handle.bufferAsync(new Callback() {
            @Override
            public void onDecoded(MpqFileHandle handle, int offset, int length, ByteBuf buffer) {
              actual.set(buffer);
            }

            @Override
            public void onError(MpqFileHandle handle, Throwable throwable) {
            }
          });
          try {
            ByteBuf futureResult = future.get();
            assertSame(actual.get(), futureResult);
          } catch (InterruptedException | ExecutionException t) {
            fail(t);
            return;
          }

          FileHandle handle_out = testAsset(in);
          ByteBuf expected = Unpooled.wrappedBuffer(handle_out.readBytes());
          assertTrue(ByteBufUtil.equals(expected, actual.get()));
        } finally {
          handle.release();
        }
      } finally {
        decoder.gracefulShutdown();
      }
    }
  }
}
