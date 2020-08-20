package com.riiablo.save;

import org.apache.logging.log4j.Logger;

import com.riiablo.Riiablo;
import com.riiablo.io.ByteOutput;
import com.riiablo.log.LogManager;

public class D2SWriter96 {
  private static final Logger log = LogManager.getLogger(D2SWriter96.class);

  private static final int VERSION = D2S.VERSION_110;

  private static final byte[] SIGNATURE = D2S.SIGNATURE;

  public void writeD2S(D2S d2s, ByteOutput out) {
    log.trace("Writing d2s...");
    out.writeBytes(SIGNATURE);
    out.write32(VERSION);
    writeHeader(d2s, out);
  }

  static void writeHeader(D2S d2s, ByteOutput out) {
    out.write32(d2s.size);
    out.write32(d2s.checksum);
    out.write32(d2s.alternate);
    out.writeString(d2s.name, Riiablo.MAX_NAME_LENGTH + 1);
    out.write32(d2s.flags);
    out.write8(d2s.charClass);
  }
}
