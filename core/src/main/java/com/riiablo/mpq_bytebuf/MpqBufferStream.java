package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.UnaryPromiseNotifier;
import java.io.InputStream;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

/**
 * InputStream of a mpq file which depends on the backing buffers of the mpq
 * file handle. Benefit includes ability to reuse the buffered data later for
 * things like streaming the contents of a dynamically sized header, where it
 * may not be realistic to pre-buffer the entire needed contents.
 * <p>
 * TODO: improve fix placeholder doc
 */
public final class MpqBufferStream extends InputStream {
  private static final Logger log = LogManager.getLogger(MpqBufferStream.class);

  private static final boolean DEBUG_MODE = !true;

  MpqFileHandle handle;
  final EventExecutor executor;
  // final int bufferSize;
  final boolean releaseOnClose;
  Promise<InputStream> init;

  ByteBuf buffer;
  int bytesRead;
  int limit;

  final int numSectors;
  final int startSector;
  final int endSector;
  int currentSector;

  Promise promise;

  MpqBufferStream(
      MpqFileHandle handle,
      EventExecutor executor,
      int bufferSize,
      boolean releaseOnClose
  ) {
    if (releaseOnClose) throw new UnsupportedOperationException("releaseOnClose not supported yet");
    if (bufferSize != handle.sectorSize()) throw new UnsupportedOperationException("bufferSize(" + bufferSize + ") != handle.sectorSize(" + handle.sectorSize() + ")");
    this.handle = handle;
    this.executor = executor;
    // this.bufferSize = bufferSize;
    this.releaseOnClose = releaseOnClose;

    buffer = Unpooled.EMPTY_BUFFER;
    bytesRead = 0;
    limit = handle.FSize;

    endSector = numSectors = handle.numSectors;
    startSector = currentSector = 0;
  }

  @Override
  public void close() {
    if (releaseOnClose) {
      releaseHandle();
    }
  }

  void releaseHandle() {
    ReferenceCountUtil.safeRelease(handle);
    handle = null;
  }

  public int readableBytes() {
    return limit - bytesRead;
  }

  @Override
  public int available() {
    return buffer.readableBytes();
  }

  @Override
  public int read() {
    if (readableBytes() <= 0) {
      return -1;
    }

    syncInitialization();
    if (!buffer.isReadable()) saturateBuffer().syncUninterruptibly();
    bytesRead++;
    if (DEBUG_MODE) log.trace("Read {} bytes ({} bytes remaining)", 1, readableBytes());
    return buffer.readUnsignedByte();
  }

  @Override
  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) {
    if (len > readableBytes()) {
      throw new IndexOutOfBoundsException(
          String.format(
              "bytesRead(+0x%x) + length(0x%x) exceeds declared limit(0x%x)",
              bytesRead, len, limit));
    }

    syncInitialization();
    final int startPosition = bytesRead;
    final ByteBuf dst = Unpooled.wrappedBuffer(b, off, len).clear();
    while (dst.isWritable()) {
      if (!buffer.isReadable()) saturateBuffer().syncUninterruptibly();
      final int writableBytes = Math.min(dst.writableBytes(), buffer.readableBytes());
      if (DEBUG_MODE) log.trace("Copying {} bytes", writableBytes);
      bytesRead += writableBytes;
      buffer.readBytes(dst, writableBytes);
    }

    assert (bytesRead - startPosition) == len
        : "bytesRead(" + (bytesRead - startPosition) + ") != len(" + len + ")";
    if (DEBUG_MODE) log.trace("Read {} bytes ({} bytes remaining)", len, readableBytes());
    return len;
  }

  @SuppressWarnings("unchecked") // FIXME: doesn't use contained object in future
  public Promise<InputStream> initialize() {
    if (init != null) return init;
    init = executor.newPromise();
    saturateBuffer0().addListener(new FutureListener() {
      @Override
      public void operationComplete(Future future) {
        init.setSuccess(MpqBufferStream.this);
      }
    });
    return init;
  }

  Promise syncInitialization() {
    if (init == null) throw new IllegalStateException("not initialized via MpqBufferStream#initialize()");
    init.syncUninterruptibly();
    return init;
  }

  Promise saturateBuffer() {
    promise.syncUninterruptibly();
    promise = null;
    return saturateBuffer0();
  }

  Promise saturateBuffer0() {
    assert promise == null;
    promise = executor.newPromise();
    final int sectorSize = handle.sectorSize();
    final int bufferOffset = currentSector * sectorSize;
    final int sectorFSize = Math.min(handle.FSize - bufferOffset, sectorSize);
    handle
        .bufferAsync(executor, bufferOffset, sectorFSize)
        .addListener(new FutureListener<ByteBuf>() {
          @Override
          public void operationComplete(Future<ByteBuf> future) {
            if (buffer instanceof EmptyByteBuf) {
              buffer = handle.buffer.slice(0, handle.FSize).clear();
            }

            buffer.setIndex(bufferOffset, bufferOffset + sectorFSize);
            @SuppressWarnings("unchecked") // don't care, unused result anyways
            final Promise<? super ByteBuf> promise0 = (Promise<? super ByteBuf>) promise;
            UnaryPromiseNotifier.cascadeTo(future, promise0);
          }
        });
    currentSector++;
    return promise;
  }
}
