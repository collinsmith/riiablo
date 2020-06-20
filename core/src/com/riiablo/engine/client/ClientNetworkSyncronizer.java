package com.riiablo.engine.client;

import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.FlatBufferBuilder;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.artemis.ComponentMapper;
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
import com.riiablo.net.packet.d2gs.ComponentP;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.EntitySync;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.VelocityP;
import com.riiablo.save.CharData;
import com.riiablo.util.ArrayUtils;

@All
public class ClientNetworkSyncronizer extends IntervalSystem {
  private static final String TAG = "ClientNetworkSyncronizer";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_PACKET  = DEBUG && !true;
  private static final boolean DEBUG_CONNECT = DEBUG && !true;

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
      CharData charData = Riiablo.charData;

      FlatBufferBuilder builder = new FlatBufferBuilder(8192);
      int charNameOffset = builder.createString(charData.name);

      int entityId = Riiablo.game.player;
      int[] component = mCofComponents.get(entityId).component;
      builder.startVector(1, component.length, 1);
      for (int i = component.length - 1; i >= 0; i--) builder.addByte((byte) component[i]);
      int componentsOffset = builder.endVector();

      byte[] alphas = ArrayUtils.toFixedPoint(mCofAlphas.get(entityId).alpha);
      int alphasOffset = Connection.createCofAlphasVector(builder, alphas);

      byte[] transforms = mCofTransforms.get(entityId).transform;
      int transformsOffset = Connection.createCofTransformsVector(builder, transforms);

      int d2sOffset = Connection.createD2sVector(builder, charData.serialize());

      Connection.startConnection(builder);
      Connection.addCharClass(builder, charData.charClass);
      Connection.addCharName(builder, charNameOffset);
      Connection.addCofComponents(builder, componentsOffset);
      Connection.addCofAlphas(builder, alphasOffset);
      Connection.addCofTransforms(builder, transformsOffset);
      Connection.addD2s(builder, d2sOffset);
      int connectionOffset = Connection.endConnection(builder);
      int offset = D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
      D2GS.finishSizePrefixedD2GSBuffer(builder, offset);

      OutputStream out = socket.getOutputStream();
      WritableByteChannel channelOut = Channels.newChannel(out);
      channelOut.write(builder.dataBuffer());

      // Before we can connect, we need to wait for our connection ack to be received
      boolean connected = false;
      ByteBuffer buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      while (!connected) {
        try {
          buffer.clear();
          ReadableByteChannel channelIn = Channels.newChannel(socket.getInputStream());
          int i = channelIn.read(buffer);
          buffer.rewind().limit(i);
          D2GS d2gs = new D2GS();
          while (buffer.hasRemaining()) {
            int size = ByteBufferUtil.getSizePrefix(buffer);
            D2GS.getRootAsD2GS(ByteBufferUtil.removeSizePrefix(buffer), d2gs);
            if (DEBUG_PACKET) Gdx.app.debug(TAG, "packet type " + D2GSData.name(d2gs.dataType()) + ":" + size + "B");
            connected = d2gs.dataType() == D2GSData.Connection;
            if (!connected) {
              if (DEBUG_CONNECT) Gdx.app.debug(TAG, "dropping... ");
//              System.out.println(buffer.position() + "->" + (buffer.position() + size + 4));
              buffer.position(buffer.position() + size + 4); // advance position passed current packet + size prefix of next packet
              continue;
            }
            Connection connection = (Connection) d2gs.data(new Connection());
            connected = connection.charName() == null;
            if (!connected) {
              if (DEBUG_CONNECT) Gdx.app.debug(TAG, "dropping... ");
//              System.out.println(buffer.position() + "->" + (buffer.position() + size + 4));
              buffer.position(buffer.position() + size + 4); // advance position passed current packet + size prefix of next packet
              continue;
            }

            int serverId = connection.entityId();
            Gdx.app.log(TAG, "assign " + entityId + " to " + serverId);
            idManager.put(connection.entityId(), Riiablo.game.player);
            break;
          }
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
    byte[] alpha = ArrayUtils.toFixedPoint(mCofAlphas.get(entityId).alpha);
    Vector2 position = mPosition.get(entityId).position;
    Vector2 velocity = mVelocity.get(entityId).velocity;
    Vector2 angle = mAngle.get(entityId).target;

    int cofComponents = CofComponentsP.createComponentVector(builder, component);
    int cofTransforms = CofTransformsP.createTransformVector(builder, transform);
    int cofAlphas = CofAlphasP.createAlphaVector(builder, alpha);

    byte[] dataTypes = new byte[6];
    dataTypes[0] = ComponentP.CofComponentsP;
    dataTypes[1] = ComponentP.CofTransformsP;
    dataTypes[2] = ComponentP.CofAlphasP;
    dataTypes[3] = ComponentP.PositionP;
    dataTypes[4] = ComponentP.VelocityP;
    dataTypes[5] = ComponentP.AngleP;
    int dataTypesOffset = EntitySync.createComponentTypeVector(builder, dataTypes);

    int[] data = new int[6];
    data[0] = CofComponentsP.createCofComponentsP(builder, cofComponents);
    data[1] = CofTransformsP.createCofTransformsP(builder, cofTransforms);
    data[2] = CofAlphasP.createCofAlphasP(builder, cofAlphas);
    data[3] = PositionP.createPositionP(builder, position.x, position.y);
    data[4] = VelocityP.createVelocityP(builder, velocity.x, velocity.y);
    data[5] = AngleP.createAngleP(builder, angle.x, angle.y);
    int dataOffset = EntitySync.createComponentVector(builder, data);

    EntitySync.startEntitySync(builder);
    EntitySync.addEntityId(builder, mNetworked.get(entityId).serverId);
    EntitySync.addComponentType(builder, dataTypesOffset);
    EntitySync.addComponent(builder, dataOffset);
    int syncOffset = EntitySync.endEntitySync(builder);
    int root = D2GS.createD2GS(builder, D2GSData.EntitySync, syncOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);

    try {
      OutputStream out = socket.getOutputStream();
      WritableByteChannel channelOut = Channels.newChannel(out);
      channelOut.write(builder.dataBuffer());
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }
}
