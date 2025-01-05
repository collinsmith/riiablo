package com.riiablo.map5;

import java.io.InputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteInputStream;
import com.riiablo.io.InvalidFormat;
import com.riiablo.io.UnsafeNarrowing;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class Dt1Decoder {
  private Dt1Decoder() {}

  private static final Logger log = LogManager.getLogger(Dt1Decoder.class);

  @Deprecated
  public static Dt1 decode(FileHandle handle) {
    return decode(handle, handle.read());
  }

  public static Dt1 decode(FileHandle handle, InputStream in) {
    return decode(handle, ByteInputStream.wrap(in, 0, (int) handle.length()));
  }

  static Dt1 decode(FileHandle handle, ByteInputStream in) {
    Dt1 dt1 = Dt1.obtain(handle);
    try {
      return decode(dt1, in);
    } catch (Throwable t) {
      dt1.dispose();
      return ExceptionUtils.rethrow(t);
    }
  }

  static Dt1 decode(Dt1 dt1, ByteInputStream in) {
    try {
      MDC.put("dt1", dt1.handle.path());
      dt1.version = in.readSafe32u();
      log.trace("dt1.version: {}", dt1.version);
      switch (dt1.version) {
        case Dt1Decoder7.VERSION:
          return Dt1Decoder7.readHeaders(dt1, in);
        default:
          throw new InvalidFormat(
              in,
              String.format("Unsupported dt1 version: 0x%08x", dt1.version));
      }
    } catch (UnsafeNarrowing t) {
      throw new InvalidFormat(in, t);
    } finally {
      MDC.remove("dt1");
    }
  }

  public static Dt1 decodeTile(Dt1 dt1, ByteInput in, int tileId) {
    try {
      MDC.put("dt1", dt1.handle.path());
      switch (dt1.version) {
        case Dt1Decoder7.VERSION:
          Dt1Decoder7.decodeBlocks(dt1, tileId, in);
          return null;
        default:
          throw new InvalidFormat(
              in,
              String.format("Unsupported dt1 version: 0x%08x", dt1.version));
      }
    } finally {
      MDC.remove("dt1");
    }
  }

  public static Block[] readBlockHeaders(Dt1 dt1, int tile, ByteInput in) {
    try {
      MDC.put("dt1", dt1.handle.path());
      try {
        MDC.put("tile", tile);
        Tile t = dt1.tiles[tile];
        Block[] blocks = Block.newArray(t.numBlocks);
        switch (dt1.version) {
          case Dt1Decoder7.VERSION:
            Dt1Decoder7.readBlockHeaders(blocks, blocks.length, in);
            return blocks;
          default:
            throw new InvalidFormat(
                in,
                String.format("Unsupported dt1 version: 0x%08x", dt1.version));
        }
      } finally {
        MDC.remove("tile");
      }
    } finally {
      MDC.remove("dt1");
    }
  }
}
