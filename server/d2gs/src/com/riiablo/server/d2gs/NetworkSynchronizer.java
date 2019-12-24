package com.riiablo.server.d2gs;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.engine.server.SerializationManager;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

@All(Networked.class)
public class NetworkSynchronizer extends IteratingSystem {
  private static final String TAG = "NetworkSynchronizer";

  protected SerializationManager serializer;

  @Wire(name = "outPackets")
  protected BlockingQueue<com.riiablo.server.d2gs.D2GS.Packet> outPackets;

  @Wire(name = "player")
  protected IntIntMap players;

  @Override
  protected boolean checkProcessing() {
    return players.size > 0;
  }

  @Override
  protected void process(int entityId) {
    ByteBuffer sync = sync(entityId);
    int id = players.findKey(entityId, -1);
    boolean success = outPackets.offer(com.riiablo.server.d2gs.D2GS.Packet
        .obtain(id != -1 ? ~(1 << id) : 0xFFFFFFFF, sync));
    assert success;
  }

  public ByteBuffer sync(int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder(0);
    int syncOffset = serializer.serialize(builder, entityId);
    int root = D2GS.createD2GS(builder, D2GSData.Sync, syncOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);
    return builder.dataBuffer();
  }

  public void sync(int entityId, D2GS packet) {
    Gdx.app.log(TAG, "syncing " + entityId);
    serializer.deserialize(entityId, packet);
  }
}
