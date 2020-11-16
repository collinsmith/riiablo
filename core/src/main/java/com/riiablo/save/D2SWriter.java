package com.riiablo.save;

import com.riiablo.io.ByteOutput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public enum D2SWriter {
  INSTANCE;

  private static final Logger log = LogManager.getLogger(D2SWriter.class);

  private static final int VERSION = D2S.VERSION_110;

  private static final byte[] SIGNATURE = D2S.SIGNATURE;

  public void writeD2S(D2S d2s, ByteOutput out) {
    log.trace("Writing d2s...");
    D2SWriterStub.writeD2S(d2s, out);
    if (true) return; // stubbed!

    log.debug("version: {} ({})", d2s.version, D2S.getVersionString(d2s.version));
    out.writeBytes(SIGNATURE);
    out.write32(d2s.version);
    try {
      MDC.put("d2s.version", d2s.version);
      switch (d2s.version) {
        case D2S.VERSION_110:
          D2SWriter96.writeHeader(d2s, out);
          return;
        case D2S.VERSION_100:
        case D2S.VERSION_107:
        case D2S.VERSION_108:
        case D2S.VERSION_109:
        default:
          log.error("Unsupported d2s version: " + D2S.getVersionString(d2s.version));
          return;
      }
    } finally {
      MDC.remove("d2s.version");
    }
  }
}
