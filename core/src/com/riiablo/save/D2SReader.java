package com.riiablo.save;

import io.netty.util.ByteProcessor;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.io.UnsafeNarrowing;
import com.riiablo.item.ItemReader;
import com.riiablo.log.Log;
import com.riiablo.log.LogManager;

public enum D2SReader {
  INSTANCE;

  private static final Logger log = LogManager.getLogger(D2SReader.class);

  // set the value of it in the d2s data to be zero and iterate through all the bytes
  public int calculateChecksum(ByteInput in) {
    ChecksumCalculator checksumCalculator = new ChecksumCalculator();
    in.buffer().forEachByte(checksumCalculator);
    return checksumCalculator.checksum;
  }

  private static class ChecksumCalculator implements ByteProcessor {
    int checksum = 0;

    @Override
    public boolean process(byte value) {
      checksum = (checksum << 1) + value;
      return true;
    }
  }

  // TODO: rewrite this function without stubbing serialization
  public D2S readD2S(FileHandle handle) {
    byte[] bytes = handle.readBytes();
    D2S d2s = readD2S(ByteInput.wrap(bytes));
    D2SWriterStub.put(d2s, bytes);
    return d2s;
  }

  public D2S readD2S(ByteInput in) {
    log.trace("Reading d2s...");
    log.trace("Validating d2s signature");
    in.readSignature(D2S.SIGNATURE);
    try {
      D2S d2s = new D2S();
      return readHeader(in, d2s);
    } catch (UnsafeNarrowing t) {
      throw new InvalidFormat(in, t);
    }
  }

  static D2S readHeader(ByteInput in, D2S d2s) {
    d2s.version = in.readSafe32u();
    log.debug("version: {} ({})", d2s.version, D2S.getVersionString(d2s.version));
    try {
      Log.put("d2s.version", d2s.version);
      switch (d2s.version) {
        case D2S.VERSION_110:
          return D2SReader96.readHeader(in, d2s);
        case D2S.VERSION_100:
        case D2S.VERSION_107:
        case D2S.VERSION_108:
        case D2S.VERSION_109:
        default:
          log.error("Unsupported d2s version: " + D2S.getVersionString(d2s.version));
          return d2s;
      }
    } finally {
      Log.remove("d2s.version");
    }
  }

  public D2S readRemaining(D2S d2s, ByteInput in, ItemReader itemReader) {
    try {
      Log.put("d2s.version", d2s.version);
      switch (d2s.version) {
        case D2S.VERSION_110:
          return D2SReader96.readRemaining(d2s, in, itemReader);
        case D2S.VERSION_100:
        case D2S.VERSION_107:
        case D2S.VERSION_108:
        case D2S.VERSION_109:
        default:
          log.error("Unsupported d2s version: " + D2S.getVersionString(d2s.version));
          return d2s;
      }
    } finally {
      Log.remove("d2s.version");
    }
  }

  CharData copyTo(D2S d2s, CharData data) {
    try {
      Log.put("d2s.version", d2s.version);
      switch (d2s.version) {
        case D2S.VERSION_110:
          return D2SReader96.copyTo(d2s, data);
        case D2S.VERSION_100:
        case D2S.VERSION_107:
        case D2S.VERSION_108:
        case D2S.VERSION_109:
        default:
          log.error("Unsupported d2s version: " + D2S.getVersionString(d2s.version));
          return data;
      }
    } finally {
      Log.remove("d2s.version");
    }
  }
}
