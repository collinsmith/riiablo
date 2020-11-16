package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.net.packet.d2gs.CofComponentsP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;

public class CofComponentsSerializer implements FlatBuffersSerializer<CofComponents, CofComponentsP> {
  public static final CofComponentsP table = new CofComponentsP();

  @Override
  public byte getDataType() {
    return ComponentP.CofComponentsP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, CofComponents c) {
    int[] component = c.component;
    CofComponentsP.startComponentVector(builder, component.length);
    for (int i = component.length - 1; i >= 0; i--) {
      builder.addByte((byte) component[i]);
    }

    int vectorOffset = builder.endVector();
    return CofComponentsP.createCofComponentsP(builder, vectorOffset);
  }

  @Override
  public CofComponentsP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public CofComponents getData(EntitySync sync, int j, CofComponents c) {
    getTable(sync, j);
    int[] component = c.component;
    for (int i = 0, s = table.componentLength(); i < s; i++) {
      component[i] = table.component(i);
    }

    return c;
  }
}
