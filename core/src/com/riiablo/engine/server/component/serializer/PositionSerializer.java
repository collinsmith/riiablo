package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.server.component.Position;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.ComponentP;

public class PositionSerializer implements FlatBuffersSerializer<Position, PositionP> {
  public static final PositionP table = new PositionP();

  @Override
  public byte getDataType() {
    return ComponentP.PositionP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Position c) {
    Vector2 position = c.position;
    return PositionP.createPositionP(builder, position.x, position.y);
  }

  @Override
  public PositionP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Position getData(EntitySync sync, int j, Position c) {
    getTable(sync, j);
    Vector2 position = c.position;
    position.x = table.x();
    position.y = table.y();
    return c;
  }
}
