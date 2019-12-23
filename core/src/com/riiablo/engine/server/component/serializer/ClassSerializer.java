package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.Class;
import com.riiablo.net.packet.d2gs.ClassP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;

public class ClassSerializer implements FlatBuffersSerializer<Class, ClassP> {
  public static final ClassP table = new ClassP();

  @Override
  public byte getDataType() {
    return SyncData.ClassP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Class c) {
    return ClassP.createClassP(builder, c.type.ordinal());
  }

  @Override
  public ClassP getTable(Sync sync, int j) {
    sync.data(table, j);
    return table;
  }

  @Override
  public Class getData(Sync sync, int j, Class c) {
    getTable(sync, j);
    c.type = Class.Type.valueOf(table.type());
    return c;
  }
}
