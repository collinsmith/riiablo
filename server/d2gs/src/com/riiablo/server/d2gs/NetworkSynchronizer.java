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
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.AngleP;
import com.riiablo.net.packet.d2gs.ClassP;
import com.riiablo.net.packet.d2gs.CofAlphasP;
import com.riiablo.net.packet.d2gs.CofComponentsP;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.PlayerP;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;
import com.riiablo.net.packet.d2gs.VelocityP;
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
  protected IntIntMap players;

  @Override
  protected void process(int entityId) {
    D2GS sync = sync(entityId);
    int id = players.findKey(entityId, -1);
    assert id != -1;
    boolean success = outPackets.offer(com.riiablo.server.d2gs.D2GS.Packet.obtain(~(1 << id), sync));
    assert success;
  }

  public D2GS sync(int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder(0);

    int[] component2 = mCofComponents.get(entityId).component;
    byte[] component = new byte[16];
    for (int i = 0; i < 16; i++) component[i] = (byte) component2[i];

    int componentOffset = CofComponentsP.createComponentVector(builder, component);
    int cofComponents = CofComponentsP.createCofComponentsP(builder, componentOffset);

    byte[] transform = mCofTransforms.get(entityId).transform;
    int transformOffset = CofTransformsP.createTransformVector(builder, transform);
    int cofTransforms = CofTransformsP.createCofTransformsP(builder, transformOffset);

    float[] alpha = mCofAlphas.get(entityId).alpha;
    int alphaOffset = CofAlphasP.createAlphaVector(builder, alpha);
    int cofAlphas = CofAlphasP.createCofAlphasP(builder, alphaOffset);

    Vector2 position = mPosition.get(entityId).position;
    Vector2 velocity = mVelocity.get(entityId).velocity;
    Vector2 angle = mAngle.get(entityId).target;

    CharData charData = mPlayer.get(entityId).data;
    int charNameOffset = builder.createString(charData.getD2S().header.name);

    byte[] dataTypes = new byte[8];
    dataTypes[0] = SyncData.ClassP;
    dataTypes[1] = SyncData.CofComponentsP;
    dataTypes[2] = SyncData.CofTransformsP;
    dataTypes[3] = SyncData.CofAlphasP;
    dataTypes[4] = SyncData.PositionP;
    dataTypes[5] = SyncData.VelocityP;
    dataTypes[6] = SyncData.AngleP;
    dataTypes[7] = SyncData.PlayerP;
    int dataTypesOffset = Sync.createDataTypeVector(builder, dataTypes);

    int[] data = new int[8];
    data[0] = ClassP.createClassP(builder, mClass.get(entityId).type.ordinal());
    data[1] = cofComponents;
    data[2] = cofTransforms;
    data[3] = cofAlphas;
    data[4] = PositionP.createPositionP(builder, position.x, position.y);
    data[5] = VelocityP.createVelocityP(builder, velocity.x, velocity.y);
    data[6] = AngleP.createAngleP(builder, angle.x, angle.y);
    data[7] = PlayerP.createPlayerP(builder, charData.getD2S().header.charClass, charNameOffset);
    int dataOffset = Sync.createDataVector(builder, data);

    Sync.startSync(builder);
    Sync.addEntityId(builder, entityId);
    Sync.addDataType(builder, dataTypesOffset);
    Sync.addData(builder, dataOffset);
    int syncOffset = Sync.endSync(builder);
    int root = D2GS.createD2GS(builder, D2GSData.Sync, syncOffset);
    builder.finish(root);
    return D2GS.getRootAsD2GS(builder.dataBuffer());
  }

  public void sync(int entityId, Sync sync) {
    Gdx.app.log(TAG, "syncing " + entityId);
    for (int i = 0, len = sync.dataTypeLength(); i < len; i++) {
      switch (sync.dataType(i)) {
        case SyncData.CofComponentsP: {
          int[] component = mCofComponents.get(entityId).component;
          CofComponentsP data = (CofComponentsP) sync.data(new CofComponentsP(), i);
          for (int j = 0, s = data.componentLength(); j < s; j++) {
            component[j] = data.component(j);
          }
          Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(ArrayUtils.toByteArray(component)));
          break;
        }
        case SyncData.CofTransformsP: {
          byte[] transform = mCofTransforms.get(entityId).transform;
          CofTransformsP data = (CofTransformsP) sync.data(new CofTransformsP(), i);
          for (int j = 0, s = data.transformLength(); j < s; j++) {
            transform[j] = (byte) data.transform(j);
          }
          Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(transform));
          break;
        }
        case SyncData.CofAlphasP: {
          float[] alpha = mCofAlphas.get(entityId).alpha;
          CofAlphasP data = (CofAlphasP) sync.data(new CofAlphasP(), i);
          for (int j = 0, s = data.alphaLength(); j < s; j++) {
            alpha[j] = data.alpha(j);
          }
          Gdx.app.log(TAG, "  " + Arrays.toString(alpha));
          break;
        }
        case SyncData.PositionP: {
          Vector2 position = mPosition.get(entityId).position;
          PositionP data = (PositionP) sync.data(new PositionP(), i);
          position.x = data.x();
          position.y = data.y();
          Gdx.app.log(TAG, "  " + position);
          break;
        }
        case SyncData.VelocityP: {
          Vector2 velocity = mVelocity.get(entityId).velocity;
          VelocityP data = (VelocityP) sync.data(new VelocityP(), i);
          velocity.x = data.x();
          velocity.y = data.y();
          Gdx.app.log(TAG, "  " + velocity);
          break;
        }
        case SyncData.AngleP: {
          Vector2 angle = mAngle.get(entityId).target;
          AngleP data = (AngleP) sync.data(new AngleP(), i);
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
