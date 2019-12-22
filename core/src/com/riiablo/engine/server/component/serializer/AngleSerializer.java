package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.net.packet.d2gs.AngleP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;

public class AngleSerializer implements FlatBuffersSerializer<Angle, AngleP> {
  public static final AngleP table = new AngleP();

  @Override
  public byte getDataType() {
    return SyncData.AngleP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Angle c) {
    Vector2 angle = c.target;
    return AngleP.createAngleP(builder, angle.x, angle.y);
  }

  @Override
  public AngleP getTable(Sync sync, int j) {
    sync.data(table, j);
    return table;
  }

  @Override
  public Angle getData(Sync sync, int j, Angle c) {
    getTable(sync, j);
    Vector2 angle = c.target;
    angle.x = table.x();
    angle.y = table.y();
    return c;
  }
}
