package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.VelocityP;

public class VelocitySerializer implements FlatBuffersSerializer<Velocity, VelocityP> {
  public static final VelocityP table = new VelocityP();

  @Override
  public byte getDataType() {
    return ComponentP.VelocityP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Velocity c) {
    Vector2 velocity = c.velocity;
    return VelocityP.createVelocityP(builder, velocity.x, velocity.y);
  }

  @Override
  public VelocityP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Velocity getData(EntitySync sync, int j, Velocity c) {
    getTable(sync, j);
    Vector2 velocity = c.velocity;
    velocity.x = table.x();
    velocity.y = table.y();
    return c;
  }
}
