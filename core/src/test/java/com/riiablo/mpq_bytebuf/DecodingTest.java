package com.riiablo.mpq_bytebuf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.RiiabloTest;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;

import static com.riiablo.mpq_bytebuf.Mpq.DEFAULT_LOCALE;

class DecodingTest extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.mpq_bytebuf.MpqFileHandle", Level.TRACE);
    LogManager.setLevel("com.riiablo.mpq_bytebuf.DecoderExecutorGroup", Level.TRACE);
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
    void decode_await(String in) {
      DecoderExecutorGroup decoder = new DecoderExecutorGroup(4);
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
        decoder.shutdownGracefully();
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "data\\global\\CHARS\\BA\\LG\\BALGLITTNHTH.DCC",
        "DATA\\GLOBAL\\CHARS\\PA\\LA\\PALALITTN1HS.DCC",
    })
    void decode_future(String in) {
      DecoderExecutorGroup decoder = new DecoderExecutorGroup(4);
      try {
        MpqFileHandle handle = mpq.open(decoder, in, DEFAULT_LOCALE);
        try {
          final EventExecutor executor = ImmediateEventExecutor.INSTANCE;
          Promise<ByteBuf> actual = executor.newPromise();
          Future<ByteBuf> future = handle.bufferAsync(executor);
          future.addListener((FutureListener<ByteBuf>) f -> actual.setSuccess(f.getNow()));
          try {
            ByteBuf futureResult = future.get();
            actual.await();
            assertSame(actual.getNow(), futureResult);
          } catch (InterruptedException | CancellationException | ExecutionException t) {
            fail(t);
            return;
          }

          FileHandle handle_out = testAsset(in);
          ByteBuf expected = Unpooled.wrappedBuffer(handle_out.readBytes());
          assertTrue(ByteBufUtil.equals(expected, actual.getNow()));
        } finally {
          handle.release();
        }
      } finally {
        decoder.shutdownGracefully();
      }
    }
  }
}
