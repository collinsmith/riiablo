package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.DS1ObjectWrapper;
import com.riiablo.net.packet.d2gs.DS1ObjectWrapperP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;

public class DS1ObjectWrapperSerializer implements FlatBuffersSerializer<DS1ObjectWrapper, DS1ObjectWrapperP> {
  public static final DS1ObjectWrapperP table = new DS1ObjectWrapperP();

  @Override
  public byte getDataType() {
    return ComponentP.DS1ObjectWrapperP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, DS1ObjectWrapper c) {
    return DS1ObjectWrapperP.createDS1ObjectWrapperP(builder, c.ds1.getAct(), c.object.type, c.object.id);
  }

  @Override
  public DS1ObjectWrapperP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public DS1ObjectWrapper getData(EntitySync sync, int j, DS1ObjectWrapper c) {
    throw new UnsupportedOperationException("Not supported!");
  }
}
