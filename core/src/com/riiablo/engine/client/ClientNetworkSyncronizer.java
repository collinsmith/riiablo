package com.riiablo.engine.client;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.BaseEntitySystem;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
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

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@All(Networked.class)
public class ClientNetworkSyncronizer extends BaseEntitySystem {
  private static final String TAG = "ClientNetworkSyncronizer";

  boolean init = false;
  @Wire(name="client.socket") Socket socket;

  @Override
  protected void processSystem() {}

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
      player.getComponent(Networked.class).serverId = serverId;
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }
}
