package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.Monster;
import com.riiablo.net.packet.d2gs.MonsterP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;

public class MonsterSerializer implements FlatBuffersSerializer<Monster, MonsterP> {
  public static final MonsterP table = new MonsterP();

  @Override
  public byte getDataType() {
    return SyncData.MonsterP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Monster c) {
    return MonsterP.createMonsterP(builder, c.monstats.hcIdx);
  }

  @Override
  public MonsterP getTable(Sync sync, int j) {
    sync.data(table, j);
    return table;
  }

  @Override
  public Monster getData(Sync sync, int j, Monster c) {
    throw new UnsupportedOperationException("Not supported!");
  }
}
