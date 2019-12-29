package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;

public class CofTransformsSerializer implements FlatBuffersSerializer<CofTransforms, CofTransformsP> {
  public static final CofTransformsP table = new CofTransformsP();

  @Override
  public byte getDataType() {
    return ComponentP.CofTransformsP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, CofTransforms c) {
    int vectorOffset = CofTransformsP.createTransformVector(builder, c.transform);
    return CofTransformsP.createCofTransformsP(builder, vectorOffset);
  }

  @Override
  public CofTransformsP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public CofTransforms getData(EntitySync sync, int j, CofTransforms c) {
    getTable(sync, j);
    byte[] transform = c.transform;
    for (int i = 0, s = table.transformLength(); i < s; i++) {
      transform[i] = (byte) table.transform(i);
    }

    return c;
  }
}
