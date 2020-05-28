package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.engine.server.component.Player;
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.PlayerP;
import com.riiablo.save.CharData;

public class PlayerSerializer implements FlatBuffersSerializer<Player, PlayerP> {
  public static final PlayerP table = new PlayerP();

  @Override
  public byte getDataType() {
    return ComponentP.PlayerP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Player c) {
    CharData data = c.data;
    int charNameOffset = builder.createString(data.name);
    return PlayerP.createPlayerP(builder, data.charClass, charNameOffset);
  }

  @Override
  public PlayerP getTable(EntitySync sync, int j) {
    sync.component(table, j);
    return table;
  }

  @Override
  public Player getData(EntitySync sync, int j, Player c) {
    throw new UnsupportedOperationException("Not supported!");
  }
}
