package com.riiablo.server.d2gs;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;
import com.riiablo.util.ArrayUtils;
import com.riiablo.util.DebugUtils;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

@All(Networked.class)
public class NetworkSynchronizer extends IteratingSystem {
  private static final String TAG = "NetworkSynchronizer";

  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<CofAlphas> mCofAlphas;

  protected CofManager cofs;

  @Wire(name = "outPackets")
  protected BlockingQueue<com.riiablo.server.d2gs.D2GS.Packet> outPackets;

  @Wire(name = "player")
  protected IntIntMap player;

  @Override
  protected void process(int entityId) {
    com.riiablo.net.packet.d2gs.D2GS sync = sync(entityId);
    int id = player.get(entityId, -1);
    assert id != -1;
    boolean success = outPackets.offer(D2GS.Packet.obtain(~(1 << id), sync));
    assert success;
  }

  public com.riiablo.net.packet.d2gs.D2GS sync(int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder(0);

    int[] component2 = mCofComponents.get(entityId).component;
    byte[] component = new byte[16];
    for (int i = 0; i < 16; i++) component[i] = (byte) component2[i];
    Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(component));

    int componentOffset = com.riiablo.net.packet.d2gs.CofComponents.createComponentVector(builder, component);
    int cofComponents = com.riiablo.net.packet.d2gs.CofComponents.createCofComponents(builder, componentOffset);

    byte[] transform = mCofTransforms.get(entityId).transform;
    int transformOffset = com.riiablo.net.packet.d2gs.CofTransforms.createTransformVector(builder, transform);
    int cofTransforms = com.riiablo.net.packet.d2gs.CofTransforms.createCofTransforms(builder, transformOffset);

    float[] alpha = mCofAlphas.get(entityId).alpha;
    int alphaOffset = com.riiablo.net.packet.d2gs.CofAlphas.createAlphaVector(builder, alpha);
    int cofAlphas = com.riiablo.net.packet.d2gs.CofAlphas.createCofAlphas(builder, alphaOffset);

    byte[] dataTypes = new byte[3];
    dataTypes[0] = SyncData.CofComponents;
    dataTypes[1] = SyncData.CofTransforms;
    dataTypes[2] = SyncData.CofAlphas;
    int dataTypesOffset = Sync.createDataTypeVector(builder, dataTypes);

    int[] data = new int[3];
    data[0] = cofComponents;
    data[1] = cofTransforms;
    data[2] = cofAlphas;
    int dataOffset = Sync.createDataVector(builder, data);

    Sync.startSync(builder);
    Sync.addEntityId(builder, entityId);
    Sync.addDataType(builder, dataTypesOffset);
    Sync.addData(builder, dataOffset);
    int syncOffset = Sync.endSync(builder);
    int root = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.Sync, syncOffset);
    builder.finish(root);
    return com.riiablo.net.packet.d2gs.D2GS.getRootAsD2GS(builder.dataBuffer());
  }

  public void sync(int entityId, Sync sync) {
    Gdx.app.log(TAG, "syncing " + sync.entityId());
    for (int i = 0, len = sync.dataTypeLength(); i < len; i++) {
      System.out.println(SyncData.name(sync.dataType(i)));
      switch (sync.dataType(i)) {
        case SyncData.CofComponents: {
          int[] component = mCofComponents.get(entityId).component;
          com.riiablo.net.packet.d2gs.CofComponents data = (com.riiablo.net.packet.d2gs.CofComponents) sync.data(new com.riiablo.net.packet.d2gs.CofComponents(), i);
          for (int j = 0, s = data.componentLength(); j < s; j++) {
            component[j] = data.component(j);
          }
          Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(ArrayUtils.toByteArray(component)));
          break;
        }
        case SyncData.CofTransforms: {
          byte[] transform = mCofTransforms.get(entityId).transform;
          com.riiablo.net.packet.d2gs.CofTransforms data = (com.riiablo.net.packet.d2gs.CofTransforms) sync.data(new com.riiablo.net.packet.d2gs.CofTransforms(), i);
          for (int j = 0, s = data.transformLength(); j < s; j++) {
            transform[j] = (byte) data.transform(j);
          }
          Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(transform));
          break;
        }
        case SyncData.CofAlphas: {
          float[] alpha = mCofAlphas.get(entityId).alpha;
          com.riiablo.net.packet.d2gs.CofAlphas data = (com.riiablo.net.packet.d2gs.CofAlphas) sync.data(new com.riiablo.net.packet.d2gs.CofAlphas(), i);
          for (int j = 0, s = data.alphaLength(); j < s; j++) {
            alpha[j] = data.alpha(j);
          }
          Gdx.app.log(TAG, "  " + Arrays.toString(alpha));
          break;
        }
      }
    }
  }
}
