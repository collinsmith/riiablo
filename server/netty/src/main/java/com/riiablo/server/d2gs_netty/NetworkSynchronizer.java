package com.riiablo.server.d2gs_netty;

import com.google.flatbuffers.FlatBufferBuilder;
import java.util.concurrent.BlockingQueue;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;

import com.riiablo.engine.server.SerializationManager;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.Flags;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.EntityFlags;
import com.riiablo.net.tcp.D2GSOutboundPacketFactory;
import com.riiablo.net.tcp.OutboundPacket;

@All(Networked.class)
public class NetworkSynchronizer extends BaseEntitySystem {
  private static final String TAG = "NetworkSynchronizer";

  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_SYNC = DEBUG && !true;

  protected SerializationManager serializer;

  @Wire(name = "outPackets")
  protected BlockingQueue<OutboundPacket> outPackets;

  @Wire(name = "player")
  protected IntIntMap players;

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Flags> mFlags;

  @Override
  protected boolean checkProcessing() {
    return players.size > 0;
  }

  // FIXME: this assumes that removing Networked component implies deletion -- may not always be case
  @Override
  protected void removed(int entityId) {
    Class.Type type = mClass.get(entityId).type;
    switch (type) {
      case PLR:
        // TODO: handled by disconnection packet, need to handle here also
        break;
      default:
        mFlags.get(entityId).flags |= EntityFlags.deleted;
        process(entityId);
    }
  }

  @Override
  protected void processSystem() {
    IntBag entities = subscription.getEntities();
    int[] entityIds = entities.getData();
    for (int i = 0, s = entities.size(); i < s; i++) {
      process(entityIds[i]);
    }
  }

  protected void process(int entityId) {
    FlatBufferBuilder builder = sync(new FlatBufferBuilder(0), entityId);
    int id = players.findKey(entityId, -1); // TODO: replace with component referencing player id
    OutboundPacket packet = D2GSOutboundPacketFactory.obtain(id != -1 ? ~(1 << id) : OutboundPacket.BROADCAST, D2GSData.EntitySync, builder.dataBuffer());
    boolean success = outPackets.offer(packet);
    assert success;
  }

  public FlatBufferBuilder sync(FlatBufferBuilder builder, int entityId) {
    int syncOffset = serializer.serialize(builder, entityId);
    int root = D2GS.createD2GS(builder, D2GSData.EntitySync, syncOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);
    return builder;
  }

  public void sync(int entityId, D2GS packet) {
    if (DEBUG_SYNC) Gdx.app.log(TAG, "syncing " + entityId);
    serializer.deserialize(entityId, packet);
  }
}
