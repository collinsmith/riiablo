package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;

public class CofTransformsSerializer implements FlatBuffersSerializer<CofTransforms, CofTransformsP> {
  public static final CofTransformsP table = new CofTransformsP();

  @Override
  public byte getDataType() {
    return SyncData.CofTransformsP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, CofTransforms c) {
    int vectorOffset = CofTransformsP.createTransformVector(builder, c.transform);
    return CofTransformsP.createCofTransformsP(builder, vectorOffset);
  }

  @Override
  public CofTransformsP getTable(Sync sync, int i) {
    sync.data(table, i);
    return table;
  }

  @Override
  public CofTransforms getData(Sync sync, int j, CofTransforms c) {
    getTable(sync, j);
    byte[] transform = c.transform;
    for (int i = 0, s = table.transformLength(); i < s; i++) {
      transform[i] = (byte) table.transform(i);
    }

    return c;
  }
}
