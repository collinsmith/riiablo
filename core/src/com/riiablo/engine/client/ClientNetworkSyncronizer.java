package com.riiablo.engine.client;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.riiablo.Riiablo;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.net.packet.d2gs.AngleP;
import com.riiablo.net.packet.d2gs.CofAlphasP;
import com.riiablo.net.packet.d2gs.CofComponentsP;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;
import com.riiablo.net.packet.d2gs.VelocityP;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@All
public class ClientNetworkSyncronizer extends IntervalSystem {
  private static final String TAG = "ClientNetworkSyncronizer";

  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;

  protected NetworkIdManager idManager;
  protected ClientNetworkReceiver receiver;

  boolean init = false;
  @Wire(name="client.socket") Socket socket;

  public ClientNetworkSyncronizer() {
    super(null, 1 / 60f);
  }

  @Override
  protected void initialize() {
    receiver.setEnabled(false);
  }

  @Override
  protected void begin() {
    if (socket == null) return;
    if (init) return;
    init = true;

    try {
      FlatBufferBuilder builder = new FlatBufferBuilder();
      int charNameOffset = builder.createString(Riiablo.charData.getD2S().header.name);

      Entity player = world.getEntity(Riiablo.game.player);
      int[] component = player.getComponent(CofComponents.class).component;
      builder.startVector(1, component.length, 1);
      for (int i = component.length - 1; i >= 0; i--) builder.addByte((byte) component[i]);
      int componentsOffset = builder.endVector();

      float[] alphas = player.getComponent(CofAlphas.class).alpha;
      int alphasOffset = Connection.createCofAlphasVector(builder, alphas);

      byte[] transforms = player.getComponent(CofTransforms.class).transform;
      int transformsOffset = Connection.createCofTransformsVector(builder, transforms);

      Connection.startConnection(builder);
      Connection.addCharClass(builder, Riiablo.charData.getD2S().header.charClass);
      Connection.addCharName(builder, charNameOffset);
      Connection.addCofComponents(builder, componentsOffset);
      Connection.addCofAlphas(builder, alphasOffset);
      Connection.addCofTransforms(builder, transformsOffset);
      int connectionOffset = Connection.endConnection(builder);
      int offset = D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
      builder.finish(offset);
      ByteBuffer data = builder.dataBuffer();

      OutputStream out = socket.getOutputStream();
      WritableByteChannel channelOut = Channels.newChannel(out);
      channelOut.write(data);

      boolean connected = false;
      ByteBuffer buffer = ByteBuffer.allocate(4096);
      while (!connected) {
        try {
          buffer.clear();
          ReadableByteChannel channelIn = Channels.newChannel(socket.getInputStream());
          int i = channelIn.read(buffer);
          System.out.println("read " + i + ": " + buffer.position());
          buffer.rewind();
          D2GS response = D2GS.getRootAsD2GS(buffer);
          System.out.println("packet type " + D2GSData.name(response.dataType()));
          connected = response.dataType() == D2GSData.Connection;
          if (!connected) {
            System.out.println("dropping...");
            continue;
          }
          Connection connection = (Connection) response.data(new Connection());
          connected = connection.charName() == null;
          if (!connected) {
            System.out.println("dropping...");
            continue;
          }
          int serverId = connection.entityId();
          System.out.println("assign " + player + " to " + serverId);
          idManager.put(connection.entityId(), Riiablo.game.player);
        } catch (Throwable t) {
          Gdx.app.error(TAG, t.getMessage(), t);
        }
      }
      receiver.setEnabled(true);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }

  @Override
  protected void processSystem() {
    int entityId = Riiablo.game.player;

    FlatBufferBuilder builder = new FlatBufferBuilder(0);

    int[] component2 = mCofComponents.get(entityId).component;
    byte[] component = new byte[16];
    for (int i = 0; i < 16; i++) component[i] = (byte) component2[i];

    byte[] transform = mCofTransforms.get(entityId).transform;
    float[] alpha = mCofAlphas.get(entityId).alpha;
    Vector2 position = mPosition.get(entityId).position;
    Vector2 velocity = mVelocity.get(entityId).velocity;
    Vector2 angle = mAngle.get(entityId).target;

    int cofComponents = CofComponentsP.createComponentVector(builder, component);
    int cofTransforms = CofTransformsP.createTransformVector(builder, transform);
    int cofAlphas = CofAlphasP.createAlphaVector(builder, alpha);

    byte[] dataTypes = new byte[6];
    dataTypes[0] = SyncData.CofComponentsP;
    dataTypes[1] = SyncData.CofTransformsP;
    dataTypes[2] = SyncData.CofAlphasP;
    dataTypes[3] = SyncData.PositionP;
    dataTypes[4] = SyncData.VelocityP;
    dataTypes[5] = SyncData.AngleP;
    int dataTypesOffset = Sync.createDataTypeVector(builder, dataTypes);

    int[] data = new int[6];
    data[0] = CofComponentsP.createCofComponentsP(builder, cofComponents);
    data[1] = CofTransformsP.createCofTransformsP(builder, cofTransforms);
    data[2] = CofAlphasP.createCofAlphasP(builder, cofAlphas);
    data[3] = PositionP.createPositionP(builder, position.x, position.y);
    data[4] = VelocityP.createVelocityP(builder, velocity.x, velocity.y);
    data[5] = AngleP.createAngleP(builder, angle.x, angle.y);
    int dataOffset = Sync.createDataVector(builder, data);

    Sync.startSync(builder);
    Sync.addEntityId(builder, mNetworked.get(entityId).serverId);
    Sync.addDataType(builder, dataTypesOffset);
    Sync.addData(builder, dataOffset);
    int syncOffset = Sync.endSync(builder);

    //int syncOffset = Sync.createSync(builder, entityId, dataTypesOffset, dataOffset);
    int root = D2GS.createD2GS(builder, D2GSData.Sync, syncOffset);
    builder.finish(root);

    try {
      OutputStream out = socket.getOutputStream();
      WritableByteChannel channelOut = Channels.newChannel(out);
      channelOut.write(builder.dataBuffer());
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }
}
