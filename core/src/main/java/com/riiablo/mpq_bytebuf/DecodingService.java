package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.artemis.utils.BitVector;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_COMPRESSED;
import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_ENCRYPTED;
import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_IMPLODE;

public final class DecodingService extends ForkJoinPool {
  private static final Logger log = LogManager.getLogger(DecodingService.class);

  private static final boolean DEBUG_MODE = !true;

  public static final Callback IGNORE = new Callback() {
    @Override
    public void onDecoded(MpqFileHandle handle, int offset, int length, ByteBuf buffer) {
    }

    @Override
    public void onError(MpqFileHandle handle, Throwable throwable) {
    }
  };

  static final ThreadLocal<Decoder> decoders = new ThreadLocal<Decoder>() {
    @Override
    protected Decoder initialValue() {
      return new Decoder();
    }
  };

  final Thread shutdownHook = new Thread(new Runnable() {
    @Override
    public void run() {
      gracefulShutdown();
    }
  });

  DecodingService(int nThreads) {
    super(nThreads);
    try {
      Runtime.getRuntime().addShutdownHook(shutdownHook);
    } catch (Throwable t) {
      log.warn("Problem occurred while trying to add runtime shutdown hook.", t);
    }
  }

  public Future<ByteBuf> submit(DecodingTask task) {
    return super.submit(task);
  }

  public boolean gracefulShutdown() {
    try {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    } catch (IllegalStateException ignored) {
      // called during runtime shutdown -- who cares
    } catch (Throwable t) {
      log.warn("Problem occurred while trying to remove runtime shutdown hook.", t);
    }

    shutdown();
    boolean shutdown;
    try {
      shutdown = awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      if (!shutdown) log.error("Executor did not terminate in the specified time.");
    } catch (InterruptedException t) {
      log.error(ExceptionUtils.getRootCauseMessage(t), t);
      shutdown = false;
    }

    if (!shutdown) {
      List<Runnable> droppedTasks = shutdownNow();
      log.error("Executor was abruptly shut down. {} tasks will not be executed.", droppedTasks.size());
    }

    return shutdown;
  }

  public interface Callback {
    void onDecoded(MpqFileHandle handle, int offset, int length, ByteBuf buffer);
    void onError(MpqFileHandle handle, Throwable throwable);
  }

  static final class ArchiveRead implements Callable<ByteBuf> {
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

    final MpqFileHandle handle;
    final int offset;
    final int length;
    final ByteBuf dst;
    final int dstIndex;
    final Callback callback;

    ArchiveRead(
        MpqFileHandle handle,
        int offset,
        int length,
        ByteBuf dst,
        int dstIndex,
        Callback callback
    ) {
      this.handle = handle;
      this.offset = offset;
      this.length = length;
      this.dst = dst;
      this.dstIndex = dstIndex;
      this.callback = callback;
    }

    /**
     * Only to be used for reading complete uncompressed files
     */
    @Override
    public ByteBuf call() {
      getBytes(handle, offset, length, dst, dstIndex);
      synchronized (handle.decoded) { handle.decoded.unsafeSet(0); }
      callback.onDecoded(handle, offset, length, dst);
      return dst;
    }
  }

  static final class DecodingTask extends RecursiveTask<ByteBuf> {
    final MpqFileHandle handle;
    final int offset;
    final int length;
    final Collection<SectorDecodeTask> sectors;
    final BitVector decoding;
    final Callback callback;

    DecodingTask(
        MpqFileHandle handle,
        int offset,
        int length,
        Collection<SectorDecodeTask> sectors,
        BitVector decoding,
        Callback callback
    ) {
      this.handle = handle;
      this.offset = offset;
      this.length = length;
      this.sectors = sectors;
      this.decoding = decoding;
      this.callback = callback;
    }

    @Override
    protected ByteBuf compute() {
      try {
        if (DEBUG_MODE) log.trace("Decoding {} sectors...", sectors.size());
        invokeAll(sectors);
        if (DEBUG_MODE) log.trace("Decoded {} sectors", sectors.size());
        synchronized (handle.decoded) { handle.decoded.or(decoding); }
        ByteBuf buffer = handle.buffer.slice(offset, length);
        callback.onDecoded(handle, offset, length, buffer);
        return buffer;
      } catch (Throwable t) {
        callback.onError(handle, t);
        throw t;
      }
    }

    static Builder builder(
        MpqFileHandle handle,
        int offset,
        int length,
        int numSectors,
        Callback callback
    ) {
      return new Builder(handle, offset, length, numSectors, callback);
    }

    static ByteBuf decodeSync(
        MpqFileHandle handle,
        int sector,
        int sectorOffset,
        int sectorCSize,
        int sectorFSize,
        ByteBuf buffer,
        int bufferOffset
    ) {
      new DecodingService.SectorDecodeTask(
          handle,
          sector,
          sectorOffset,
          sectorCSize,
          sectorFSize,
          buffer,
          bufferOffset).compute();
      return buffer.setIndex(bufferOffset, bufferOffset + sectorFSize);
    }

    static final class Builder {
      final MpqFileHandle handle;
      final int offset;
      final int length;
      final Collection<SectorDecodeTask> sectors;
      final BitVector decoding;
      final Callback callback;

      Builder(
          MpqFileHandle handle,
          int offset,
          int length,
          int numSectors,
          Callback callback
      ) {
        this.handle = handle;
        this.offset = offset;
        this.length = length;
        this.sectors = new ArrayList<>(numSectors);
        this.decoding = new BitVector(handle.numSectors); // bits must match handle#decoded
        this.callback = callback;
      }

      int size() {
        return sectors.size();
      }

      Builder add(
          int sector,
          int sectorOffset,
          int sectorCSize,
          int sectorFSize,
          ByteBuf buffer,
          int bufferOffset
      ) {
        decoding.unsafeSet(sector);
        sectors.add(new SectorDecodeTask(
            handle,
            sector,
            sectorOffset,
            sectorCSize,
            sectorFSize,
            buffer,
            bufferOffset));
        return this;
      }

      DecodingTask build() {
        return new DecodingTask(handle, offset, length, sectors, decoding, callback);
      }
    }
  }

  static final class SectorDecodeTask extends RecursiveAction {
    final Mpq mpq;
    final MpqFileHandle handle;
    final int sector;
    final int sectorOffset;
    final int sectorCSize;
    final int sectorFSize;
    final ByteBuf buffer;
    final int bufferOffset;

    SectorDecodeTask(
        MpqFileHandle handle,
        int sector,
        int sectorOffset,
        int sectorCSize,
        int sectorFSize,
        ByteBuf buffer,
        int bufferOffset
    ) {
      this.mpq = handle.mpq;
      this.handle = handle;
      this.sector = sector;
      this.sectorOffset = sectorOffset;
      this.sectorCSize = sectorCSize;
      this.sectorFSize = sectorFSize;
      this.buffer = buffer;
      this.bufferOffset = bufferOffset;
    }

    @Override
    protected void compute() {
      // try {
        decode();
      // } catch (Throwable t) {
      //   log.errorf(
      //       "Error decoding sector %s[%d] +0x%08x %d bytes -> %d bytes into %d",
      //       handle, sector, sectorOffset, sectorCSize, sectorFSize, bufferOffset);
      //   log.error("... cont arch " + handle.archive);
      //   log.error("... cont buff " + buffer);
      //   throw t;
      // }
    }

    void decode() {
      synchronized (handle.decoded) { if (handle.decoded.unsafeGet(sector)) return; }
      if (DEBUG_MODE) {
        log.tracef(
            "Decoding sector %s[%d] +0x%08x %d bytes -> %d bytes",
            handle, sector, sectorOffset, sectorCSize, sectorFSize);
      }

      final boolean requiresDecompression = sectorCSize < sectorFSize;
      final int flags = handle.flags;
      if ((flags & FLAG_ENCRYPTED) == 0 && !requiresDecompression) {
        ArchiveRead.getBytes(handle, sectorOffset, sectorFSize, buffer, bufferOffset);
        return;
      }

      final ByteBuf bufferSlice = buffer.slice(bufferOffset, sectorFSize).writerIndex(0);
      final ByteBuf sectorSlice = handle.mpq.sectorBuffer(); // thread-safe
      final ByteBuf scratch = handle.mpq.sectorBuffer(); // thread-safe
      try {
        ArchiveRead
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
          decoder.decode(FLAG_IMPLODE, sectorSlice, bufferSlice, scratch, sectorCSize, sectorFSize);
          if (DEBUG_MODE) log.trace("Exploded {} bytes", sectorFSize);
        }
      } finally {
        scratch.release();
        sectorSlice.release();
      }
    }
  }
}
