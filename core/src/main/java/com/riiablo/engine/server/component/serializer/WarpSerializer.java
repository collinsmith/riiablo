package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.Warp;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.WarpP;

public class WarpSerializer implements FlatBuffersSerializer<Warp, WarpP> {
  public static final WarpP table = new WarpP();

  @Override
  public byte getDataType() {
    return ComponentP.WarpP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Warp c) {
    return WarpP.createWarpP(builder, c.index);
  }

  @Override
  public WarpP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Warp getData(EntitySync sync, int j, Warp c) {
    throw new UnsupportedOperationException("Not supported!");
  }
}
