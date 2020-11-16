package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.net.packet.d2gs.AngleP;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.EntitySync;

public class AngleSerializer implements FlatBuffersSerializer<Angle, AngleP> {
  public static final AngleP table = new AngleP();

  @Override
  public byte getDataType() {
    return ComponentP.AngleP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Angle c) {
    Vector2 angle = c.target;
    return AngleP.createAngleP(builder, angle.x, angle.y);
  }

  @Override
  public AngleP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Angle getData(EntitySync sync, int j, Angle c) {
    getTable(sync, j);
    Vector2 angle = c.target;
    angle.x = table.x();
    angle.y = table.y();
    return c;
  }
}
