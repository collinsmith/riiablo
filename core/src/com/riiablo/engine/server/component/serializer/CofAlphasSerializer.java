package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.net.packet.d2gs.CofAlphasP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;

public class CofAlphasSerializer implements FlatBuffersSerializer<CofAlphas, CofAlphasP> {
  public static final CofAlphasP table = new CofAlphasP();

  @Override
  public byte getDataType() {
    return ComponentP.CofAlphasP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, CofAlphas c) {
    int vectorOffset = CofAlphasP.createAlphaVector(builder, c.alpha);
    return CofAlphasP.createCofAlphasP(builder, vectorOffset);
  }

  @Override
  public CofAlphasP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public CofAlphas getData(EntitySync sync, int j, CofAlphas c) {
    getTable(sync, j);
    float[] alpha = c.alpha;
    for (int i = 0, s = table.alphaLength(); i < s; i++) {
      alpha[i] = table.alpha(i);
    }

    return c;
  }
}
