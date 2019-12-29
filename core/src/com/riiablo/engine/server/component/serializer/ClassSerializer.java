package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.Class;
import com.riiablo.net.packet.d2gs.ClassP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;

public class ClassSerializer implements FlatBuffersSerializer<Class, ClassP> {
  public static final ClassP table = new ClassP();

  @Override
  public byte getDataType() {
    return ComponentP.ClassP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Class c) {
    return ClassP.createClassP(builder, c.type.ordinal());
  }

  @Override
  public ClassP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Class getData(EntitySync sync, int j, Class c) {
    getTable(sync, j);
    c.type = Class.Type.valueOf(table.type());
    return c;
  }
}
