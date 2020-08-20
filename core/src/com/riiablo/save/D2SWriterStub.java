package com.riiablo.save;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.io.ByteOutput;
import com.riiablo.log.LogManager;

public class D2SWriterStub {
  private static final Logger log = LogManager.getLogger(D2SWriterStub.class);

  private static final ObjectMap<String, byte[]> saveData = new ObjectMap<>();

  @Deprecated
  static void put(D2S d2s, byte[] data) {
    saveData.put(d2s.name, data);
  }

  static byte[] getBytes(String name) {
    byte[] data = saveData.get(name);
    return ArrayUtils.nullToEmpty(data);
  }

  static void writeD2S(D2S d2s, ByteOutput out) {
    log.trace("Writing d2s...");
    byte[] data = getBytes(d2s.name);
    out.writeBytes(data);
  }
}
