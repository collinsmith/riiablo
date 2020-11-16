package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.Monster;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.MonsterP;
import com.riiablo.net.packet.d2gs.ComponentP;

public class MonsterSerializer implements FlatBuffersSerializer<Monster, MonsterP> {
  public static final MonsterP table = new MonsterP();

  @Override
  public byte getDataType() {
    return ComponentP.MonsterP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Monster c) {
    return MonsterP.createMonsterP(builder, c.monstats.hcIdx);
  }

  @Override
  public MonsterP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Monster getData(EntitySync sync, int j, Monster c) {
    throw new UnsupportedOperationException("Not supported!");
  }
}
