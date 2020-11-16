package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import com.artemis.Component;
import com.riiablo.net.packet.d2gs.EntitySync;

public interface FlatBuffersSerializer<T extends Component, S extends Table> {
  byte getDataType();
  int putData(FlatBufferBuilder builder, T c);
  S getTable(EntitySync sync, int j);
  T getData(EntitySync sync, int j, T c);
}
