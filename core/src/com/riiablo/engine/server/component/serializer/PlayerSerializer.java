package com.riiablo.engine.server.component.serializer;

import com.google.flatbuffers.FlatBufferBuilder;

import com.riiablo.save.D2S;
import com.riiablo.engine.server.component.Player;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.PlayerP;
import com.riiablo.net.packet.d2gs.ComponentP;

public class PlayerSerializer implements FlatBuffersSerializer<Player, PlayerP> {
  public static final PlayerP table = new PlayerP();

  @Override
  public byte getDataType() {
    return ComponentP.PlayerP;
  }

  @Override
  public int putData(FlatBufferBuilder builder, Player c) {
    D2S.Header d2sHeader = c.data.getD2S().header;
    int charNameOffset = builder.createString(d2sHeader.name);
    return PlayerP.createPlayerP(builder, d2sHeader.charClass, charNameOffset);
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
