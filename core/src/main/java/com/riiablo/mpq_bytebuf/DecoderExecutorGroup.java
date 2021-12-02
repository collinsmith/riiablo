package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.riiablo.concurrent.PromiseCombiner;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_COMPRESSED;
import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_ENCRYPTED;
import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_IMPLODE;

public class DecoderExecutorGroup extends DefaultEventExecutorGroup {
  private static final Logger log = LogManager.getLogger(DecoderExecutorGroup.class);

  private static final boolean DEBUG_MODE = !true;

  static final FastThreadLocal<Decoder> decoders = new FastThreadLocal<Decoder>() {
    @Override
    protected Decoder initialValue() {
      return new Decoder();
    }
  };

  final Thread shutdownHook = new Thread(new Runnable() {
    @Override
    public void run() {
      shutdownGracefully();
    }
  });

  public DecoderExecutorGroup(int nThreads) {
    super(nThreads);
    try {
      Runtime.getRuntime().addShutdownHook(shutdownHook);
    } catch (Throwable t) {
      log.warn("Problem occurred while trying to add runtime shutdown hook.", t);
    }
  }

  @Override
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    try {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    } catch (IllegalStateException ignored) {
      // called during runtime shutdown -- already shutting down
    } catch (Throwable t) {
      log.warn("Problem occurred while trying to remove runtime shutdown hook.", t);
    }

    return super.shutdownGracefully(quietPeriod, timeout, unit);
  }

  public DecodingTask newDecodingTask(
      EventExecutor executor,
      MpqFileHandle handle,
      int offset,
      int length
  ) {
    return new DecodingTask(this, executor, handle, offset, length);
  }

  public static final class DecodingTask {
    final EventExecutorGroup group;
    final EventExecutor executor;
    final MpqFileHandle handle;
    final int offset;
    final int length;
    int numTasks;
    final PromiseCombiner combiner;

    DecodingTask(
        EventExecutorGroup group,
        EventExecutor executor,
        MpqFileHandle handle,
        int offset,
        int length
    ) {
      this.group = group;
      this.executor = executor;
      this.handle = handle;
      this.offset = offset;
      this.length = length;
      this.numTasks = 0;
      this.combiner = new PromiseCombiner(executor);
    }

    int numTasks() {
      return numTasks;
    }

    Future<?> submit(
        int sector,
        int sectorOffset,
        int sectorCSize,
        int sectorFSize,
        ByteBuf dst,
        int dstOffset
    ) {
      final Runnable task = new SectorDecodeTask(
          handle,
          sector,
          sectorOffset,
          sectorCSize,
          sectorFSize,
          dst,
          dstOffset);
      final Future<?> future = group.submit(task);
      combiner.add(future);
      return future;
    }

    Promise<Void> combine(Promise<Void> aggregatePromise) {
      combiner.finish(aggregatePromise);
      return aggregatePromise;
    }
  }

  static final class SectorDecodeTask implements Runnable {
    final Mpq mpq;
    final MpqFileHandle handle;
    final int sector;
    final int sectorOffset;
    final int sectorCSize;
    final int sectorFSize;
    final ByteBuf dst;
    final int dstOffset;

    SectorDecodeTask(
        MpqFileHandle handle,
        int sector,
        int sectorOffset,
        int sectorCSize,
        int sectorFSize,
        ByteBuf dst,
        int dstOffset
    ) {
      this.handle = handle;
      this.mpq = handle.mpq;
      this.sector = sector;
      this.sectorOffset = sectorOffset;
      this.sectorCSize = sectorCSize;
      this.sectorFSize = sectorFSize;
      this.dst = dst;
      this.dstOffset = dstOffset;
    }

    @Override
    public void run() {
      if (handle.decoded(sector, dst)) return;
      if (DEBUG_MODE) {
        log.tracef(
            "Decoding sector %s[%d] +0x%08x %d bytes -> %d bytes",
            handle, sector, sectorOffset, sectorCSize, sectorFSize);
      }

      final boolean requiresDecompression = sectorCSize < sectorFSize;
      final int flags = handle.flags;
      if ((flags & FLAG_ENCRYPTED) == 0 && !requiresDecompression) {
        ArchiveReadTask.getBytes(handle, sectorOffset, sectorFSize, dst, dstOffset);
        return;
      }

      final ByteBuf bufferSlice = dst.slice(dstOffset, sectorFSize).writerIndex(0);
      if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED && !requiresDecompression) {
        // rare case -- copy decrypted data directly into buffer
        ArchiveReadTask
            .getBytes(handle, sectorOffset, sectorCSize, bufferSlice, 0)
            .writerIndex(sectorCSize);
        if (DEBUG_MODE) log.trace("Decrypting sector...");
        Decrypter.decrypt(handle.encryptionKey() + sector, bufferSlice);
        if (DEBUG_MODE) log.trace("Decrypted {} bytes", sectorFSize);
        return;
      }

      final ByteBuf sectorSlice = handle.mpq.sectorBuffer(); // thread-safe
      final ByteBuf scratch = handle.mpq.sectorBuffer(); // thread-safe
      try {
        ArchiveReadTask
            .getBytes(handle, sectorOffset, sectorCSize, sectorSlice, 0)
            .writerIndex(sectorCSize);
        if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
          if (DEBUG_MODE) log.trace("Decrypting sector...");
          Decrypter.decrypt(handle.encryptionKey() + sector, sectorSlice);
          if (DEBUG_MODE) log.trace("Decrypted {} bytes", sectorFSize);
        }

        final Decoder decoder = decoders.get();
        if ((flags & FLAG_COMPRESSED) == FLAG_COMPRESSED && requiresDecompression) {
          if (DEBUG_MODE) log.trace("Decompressing sector...");
          decoder.decode(sectorSlice, bufferSlice, scratch, sectorCSize, sectorFSize);
          if (DEBUG_MODE) log.trace("Decompressed {} bytes", sectorFSize);
        }

        if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE && requiresDecompression) {
          if (DEBUG_MODE) log.trace("Exploding sector...");
          decoder.decode(Decoder.FLAG_IMPLODE, sectorSlice, bufferSlice, scratch, sectorCSize, sectorFSize);
          if (DEBUG_MODE) log.trace("Exploded {} bytes", sectorFSize);
        }
      } finally {
        scratch.release();
        sectorSlice.release();
      }
    }

    /** decodes the specified sector in the callers thread */
    static ByteBuf decodeSync(
        MpqFileHandle handle,
        int sector,
        int sectorOffset,
        int sectorCSize,
        int sectorFSize,
        ByteBuf dst,
        int dstOffset
    ) {
      new SectorDecodeTask(
          handle,
          sector,
          sectorOffset,
          sectorCSize,
          sectorFSize,
          dst,
          dstOffset).run();
      return dst.setIndex(dstOffset, dstOffset + sectorFSize);
    }
  }

  public ArchiveReadTask newArchiveReadTask(
      EventExecutor executor,
      MpqFileHandle handle,
      int offset,
      int length,
      ByteBuf dst,
      int dstIndex
  ) {
    return new ArchiveReadTask(this, executor, handle, offset, length, dst, dstIndex);
  }

  static final class ArchiveReadTask implements Callable<ByteBuf> {
    static ByteBuf getBytes(
        MpqFileHandle handle,
        int offset,
        int length,
        ByteBuf dst,
        int dstIndex
    ) {
      synchronized (handle.mpq.lock()) {
        return dst.setBytes(dstIndex, handle.archive, offset, length);
      }
    }

    final EventExecutorGroup group;
    final EventExecutor executor;
    final MpqFileHandle handle;
    final int offset;
    final int length;
    final ByteBuf dst;
    final int dstIndex;

    ArchiveReadTask(
        EventExecutorGroup group,
        EventExecutor executor,
        MpqFileHandle handle,
        int offset,
        int length,
        ByteBuf dst,
        int dstIndex
    ) {
      this.group = group;
      this.executor = executor;
      this.handle = handle;
      this.offset = offset;
      this.length = length;
      this.dst = dst;
      this.dstIndex = dstIndex;
    }

    /**
     * Only to be used for reading complete uncompressed files
     */
    @Override
    public ByteBuf call() {
      return getBytes(handle, offset, length, dst, dstIndex);
    }

    Future<ByteBuf> submit() {
      return group.submit(this);
    }
  }
}
