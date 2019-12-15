package com.riiablo.server.d2gs;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.CharData;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
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
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Player> mPlayer;

  protected CofManager cofs;

  @Wire(name = "outPackets")
  protected BlockingQueue<com.riiablo.server.d2gs.D2GS.Packet> outPackets;

  @Wire(name = "player")
  protected IntIntMap player;

  @Override
  protected void process(int entityId) {
    com.riiablo.net.packet.d2gs.D2GS sync = sync(entityId);
    int id = player.findKey(entityId, -1);
    assert id != -1;
    boolean success = outPackets.offer(D2GS.Packet.obtain(~(1 << id), sync));
    assert success;
  }

  public com.riiablo.net.packet.d2gs.D2GS sync(int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder(0);

    int[] component2 = mCofComponents.get(entityId).component;
    byte[] component = new byte[16];
    for (int i = 0; i < 16; i++) component[i] = (byte) component2[i];

    int componentOffset = com.riiablo.net.packet.d2gs.CofComponents.createComponentVector(builder, component);
    int cofComponents = com.riiablo.net.packet.d2gs.CofComponents.createCofComponents(builder, componentOffset);

    byte[] transform = mCofTransforms.get(entityId).transform;
    int transformOffset = com.riiablo.net.packet.d2gs.CofTransforms.createTransformVector(builder, transform);
    int cofTransforms = com.riiablo.net.packet.d2gs.CofTransforms.createCofTransforms(builder, transformOffset);

    float[] alpha = mCofAlphas.get(entityId).alpha;
    int alphaOffset = com.riiablo.net.packet.d2gs.CofAlphas.createAlphaVector(builder, alpha);
    int cofAlphas = com.riiablo.net.packet.d2gs.CofAlphas.createCofAlphas(builder, alphaOffset);

    Vector2 position = mPosition.get(entityId).position;
    Vector2 velocity = mVelocity.get(entityId).velocity;
    Vector2 angle = mAngle.get(entityId).target;

    CharData charData = mPlayer.get(entityId).data;
    int charNameOffset = builder.createString(charData.getD2S().header.name);

    byte[] dataTypes = new byte[8];
    dataTypes[0] = SyncData.Class;
    dataTypes[1] = SyncData.CofComponents;
    dataTypes[2] = SyncData.CofTransforms;
    dataTypes[3] = SyncData.CofAlphas;
    dataTypes[4] = SyncData.Position;
    dataTypes[5] = SyncData.Velocity;
    dataTypes[6] = SyncData.Angle;
    dataTypes[7] = SyncData.Player;
    int dataTypesOffset = Sync.createDataTypeVector(builder, dataTypes);

    int[] data = new int[8];
    data[0] = com.riiablo.net.packet.d2gs.Class.createClass(builder, mClass.get(entityId).type.ordinal());
    data[1] = cofComponents;
    data[2] = cofTransforms;
    data[3] = cofAlphas;
    data[4] = com.riiablo.net.packet.d2gs.Position.createPosition(builder, position.x, position.y);
    data[5] = com.riiablo.net.packet.d2gs.Velocity.createVelocity(builder, velocity.x, velocity.y);
    data[6] = com.riiablo.net.packet.d2gs.Angle.createAngle(builder, angle.x, angle.y);
    data[7] = com.riiablo.net.packet.d2gs.Player.createPlayer(builder, charData.getD2S().header.charClass, charNameOffset);
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
    Gdx.app.log(TAG, "syncing " + entityId);
    for (int i = 0, len = sync.dataTypeLength(); i < len; i++) {
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
        case SyncData.Position: {
          Vector2 position = mPosition.get(entityId).position;
          com.riiablo.net.packet.d2gs.Position data = (com.riiablo.net.packet.d2gs.Position) sync.data(new com.riiablo.net.packet.d2gs.Position(), i);
          position.x = data.x();
          position.y = data.y();
          Gdx.app.log(TAG, "  " + position);
          break;
        }
        case SyncData.Velocity: {
          Vector2 velocity = mVelocity.get(entityId).velocity;
          com.riiablo.net.packet.d2gs.Velocity data = (com.riiablo.net.packet.d2gs.Velocity) sync.data(new com.riiablo.net.packet.d2gs.Velocity(), i);
          velocity.x = data.x();
          velocity.y = data.y();
          Gdx.app.log(TAG, "  " + velocity);
          break;
        }
        case SyncData.Angle: {
          Vector2 angle = mAngle.get(entityId).target;
          com.riiablo.net.packet.d2gs.Angle data = (com.riiablo.net.packet.d2gs.Angle) sync.data(new com.riiablo.net.packet.d2gs.Angle(), i);
          angle.x = data.x();
          angle.y = data.y();
          Gdx.app.log(TAG, "  " + angle);
          break;
        }
        default:
          Gdx.app.error(TAG, "Unknown packet type: " + SyncData.name(sync.dataType(i)));
      }
    }
  }
}
