package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.server.component.Position;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;

public class PositionSerializer implements FlatBuffersSerializer<Position, PositionP> {
  public static final PositionP table = new PositionP();

  @Override
  public byte getDataType() {
    return SyncData.PositionP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Position c) {
    Vector2 position = c.position;
    return PositionP.createPositionP(builder, position.x, position.y);
  }

  @Override
  public PositionP getTable(Sync sync, int j) {
    sync.data(table, j);
    return table;
  }

  @Override
  public Position getData(Sync sync, int j, Position c) {
    getTable(sync, j);
    Vector2 position = c.position;
    position.x = table.x();
    position.y = table.y();
    return c;
  }
}
