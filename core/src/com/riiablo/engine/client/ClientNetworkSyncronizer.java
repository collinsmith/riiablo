package com.riiablo.engine.client;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.riiablo.Riiablo;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@All(Networked.class)
public class ClientNetworkSyncronizer extends IntervalSystem {
  private static final String TAG = "ClientNetworkSyncronizer";

  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<CofAlphas> mCofAlphas;

  protected NetworkIdManager idManager;

  boolean init = false;
  @Wire(name="client.socket") Socket socket;

  public ClientNetworkSyncronizer() {
    super(null, 1 / 60f);
  }

  @Override
  protected void initialize() {
    super.initialize();
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

      ByteBuffer buffer = com.badlogic.gdx.utils.BufferUtils.newByteBuffer(4096);
      buffer.clear();
      buffer.mark();
      ReadableByteChannel channelIn = Channels.newChannel(socket.getInputStream());
      channelIn.read(buffer);
      buffer.limit(buffer.position());
      buffer.reset();
      D2GS response = D2GS.getRootAsD2GS(buffer);
      System.out.println("packet type " + D2GSData.name(response.dataType()));
      assert response.dataType() == D2GSData.Connection;
      Connection connection = (Connection) response.data(new Connection());
      int serverId = connection.entityId();
      System.out.println("assign " + player + " to " + serverId);
      idManager.put(connection.entityId(), Riiablo.game.player);
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

    int cofComponents = com.riiablo.net.packet.d2gs.CofComponents.createComponentVector(builder, component);
    int cofTransforms = com.riiablo.net.packet.d2gs.CofTransforms.createTransformVector(builder, transform);
    int cofAlphas = com.riiablo.net.packet.d2gs.CofAlphas.createAlphaVector(builder, alpha);

    byte[] dataTypes = new byte[3];
    dataTypes[0] = SyncData.CofComponents;
    dataTypes[1] = SyncData.CofTransforms;
    dataTypes[2] = SyncData.CofAlphas;
    int dataTypesOffset = Sync.createDataTypeVector(builder, dataTypes);

    int[] data = new int[3];
    data[0] = com.riiablo.net.packet.d2gs.CofComponents.createCofComponents(builder, cofComponents);
    data[1] = com.riiablo.net.packet.d2gs.CofTransforms.createCofTransforms(builder, cofTransforms);
    data[2] = com.riiablo.net.packet.d2gs.CofAlphas.createCofAlphas(builder, cofAlphas);
    int dataOffset = Sync.createDataVector(builder, data);

    Sync.startSync(builder);
    Sync.addEntityId(builder, mNetworked.get(entityId).serverId);
    Sync.addDataType(builder, dataTypesOffset);
    Sync.addData(builder, dataOffset);
    int syncOffset = Sync.endSync(builder);

    //int syncOffset = Sync.createSync(builder, entityId, dataTypesOffset, dataOffset);
    int root = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.Sync, syncOffset);
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
