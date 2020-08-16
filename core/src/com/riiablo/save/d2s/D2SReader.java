package com.riiablo.save.d2s;

import org.apache.logging.log4j.Logger;

import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.io.UnsafeNarrowing;
import com.riiablo.item.ItemReader;
import com.riiablo.log.Log;
import com.riiablo.log.LogManager;

public class D2SReader {
  private static final Logger log = LogManager.getLogger(D2SReader.class);

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
      }
    } finally {
      Log.remove("d2s.version");
    }
    return d2s;
  }
}
