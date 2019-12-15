package com.riiablo.screen;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.physics.box2d.Body;
import com.riiablo.CharData;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.ClientEntityFactory;
import com.riiablo.engine.client.ClientNetworkSyncronizer;
import com.riiablo.engine.client.NetworkIdManager;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.map.Map;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Disconnect;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;
import com.riiablo.util.ArrayUtils;
import com.riiablo.util.DebugUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

public class NetworkedGameScreen extends GameScreen {
  private static final String TAG = "NetworkedGameScreen";
  private static final boolean DEBUG = true;

  private Socket socket;

  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<Player> mPlayer;
  protected NetworkIdManager sync;
  protected CofManager cofs;

  public NetworkedGameScreen(CharData charData, Socket socket) {
    super(charData, socket);
    this.socket = socket;
    sync = engine.getSystem(NetworkIdManager.class);
    cofs = engine.getSystem(CofManager.class);
    mPlayer = engine.getMapper(Player.class);
    mNetworked = engine.getMapper(Networked.class);
  }

  @Override
  protected WorldConfigurationBuilder getWorldConfigurationBuilder() {
    WorldConfigurationBuilder builder = super.getWorldConfigurationBuilder();
    builder.with(new ClientNetworkSyncronizer());
    return builder;
  }

  @Override
  public void render(float delta) {
    InputStream in = socket.getInputStream();
    try {
      if (in.available() > 0) {
        ReadableByteChannel channelIn = Channels.newChannel(in);
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        channelIn.read(buffer);
        buffer.rewind();
        D2GS data = D2GS.getRootAsD2GS(buffer);
        System.out.println("packet type " + D2GSData.name(data.dataType()));
        process(data);
      }
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    super.render(delta);
  }

  @Override
  public void dispose() {
    super.dispose();
    socket.dispose();
  }

  private void process(D2GS packet) {
    switch (packet.dataType()) {
      case D2GSData.Connection:
        Connection(packet);
        break;
      case D2GSData.Disconnect:
        Disconnect(packet);
        break;
      case D2GSData.Sync:
        Synchronize(packet);
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
    }
  }

  private void Connection(D2GS packet) {
    Connection connection = (Connection) packet.data(new Connection());
    String charName = connection.charName();
    int charClass = connection.charClass();

    output.appendText(Riiablo.string.format(3641, charName));
    output.appendText("\n");

    CharData charData = new CharData().createD2S(charName, CharacterClass.get(charClass));

    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
    if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
    Map.Zone zone = map.getZone(origin);
    int entityId = engine.getSystem(ClientEntityFactory.class).createPlayer(map, zone, charData, origin);
    sync.put(connection.entityId(), entityId);
    Entity entity = engine.getEntity(entityId);
    int[] component = entity.getComponent(CofComponents.class).component;
    for (int i = 0; i < 16; i++) component[i] = connection.cofComponents(i);
    float[] alpha = entity.getComponent(CofAlphas.class).alpha;
    for (int i = 0; i < 16; i++) alpha[i] = connection.cofAlphas(i);
    byte[] transform = entity.getComponent(CofTransforms.class).transform;
    for (int i = 0; i < 16; i++) transform[i] = (byte) connection.cofTransforms(i);

    int alphaFlags = Dirty.NONE;
    int transformFlags = Dirty.NONE;
    CofManager cofs = engine.getSystem(CofManager.class);
    cofs.setMode(entityId, Engine.Player.MODE_TN);
    cofs.setWClass(entityId, Engine.WEAPON_1HS); // TODO...
    for (int i = 0; i < 16; i++) {
      cofs.setComponent(entityId, i, connection.cofComponents(i));
    }
    for (int i = 0; i < 16; i++) {
      alphaFlags |= cofs.setAlpha(entityId, i, connection.cofAlphas(i));
      transformFlags |= cofs.setTransform(entityId, i, (byte) connection.cofTransforms(i));
    }

    cofs.updateAlpha(entityId, alphaFlags);
    cofs.updateTransform(entityId, transformFlags);
    cofs.setMode(entityId, Engine.Player.MODE_TN, true);

    System.out.println("  " + DebugUtils.toByteArray(ArrayUtils.toByteArray(component)));
    System.out.println("  " + Arrays.toString(alpha));
    System.out.println("  " + DebugUtils.toByteArray(transform));
  }

  private void Disconnect(D2GS packet) {
    Disconnect disconnect = (Disconnect) packet.data(new Disconnect());
    int serverEntityId = disconnect.entityId();
    System.out.println("serverEntityId=" + serverEntityId);
    int entityId = sync.get(serverEntityId);

    CharData data = mPlayer.get(entityId).data;

    output.appendText(Riiablo.string.format(3642, data.getD2S().header.name));
    output.appendText("\n");

    engine.delete(entityId);
  }

  private void Synchronize(D2GS packet) {
    Sync s = (Sync) packet.data(new Sync());
    int entityId = sync.get(s.entityId());
    if (entityId == Engine.INVALID_ENTITY) return;

    int flags1 = Dirty.NONE;
    int flags2 = Dirty.NONE;
    Gdx.app.log(TAG, "syncing " + entityId);
    for (int i = 0, len = s.dataTypeLength(); i < len; i++) {
      switch (s.dataType(i)) {
        case SyncData.CofComponents: {
          com.riiablo.net.packet.d2gs.CofComponents data = (com.riiablo.net.packet.d2gs.CofComponents) s.data(new com.riiablo.net.packet.d2gs.CofComponents(), i);
          for (int j = 0, s0 = data.componentLength(); j < s0; j++) {
            cofs.setComponent(entityId, j, data.component(j));
          }
          break;
        }
        case SyncData.CofTransforms: {
          com.riiablo.net.packet.d2gs.CofTransforms data = (com.riiablo.net.packet.d2gs.CofTransforms) s.data(new com.riiablo.net.packet.d2gs.CofTransforms(), i);
          for (int j = 0, s0 = data.transformLength(); j < s0; j++) {
            flags1 |= cofs.setTransform(entityId, j, (byte) data.transform(j));
          }
          break;
        }
        case SyncData.CofAlphas: {
          com.riiablo.net.packet.d2gs.CofAlphas data = (com.riiablo.net.packet.d2gs.CofAlphas) s.data(new com.riiablo.net.packet.d2gs.CofAlphas(), i);
          for (int j = 0, s0 = data.alphaLength(); j < s0; j++) {
            flags2 |= cofs.setAlpha(entityId, j, data.alpha(j));
          }
          break;
        }
        case SyncData.Position: {
          Vector2 position = engine.getMapper(Position.class).get(entityId).position;
          com.riiablo.net.packet.d2gs.Position data = (com.riiablo.net.packet.d2gs.Position) s.data(new com.riiablo.net.packet.d2gs.Position(), i);
          position.x = data.x();
          position.y = data.y();
          Body body = engine.getMapper(Box2DBody.class).get(entityId).body;
          body.setTransform(position, body.getAngle());
          Gdx.app.log(TAG, "  " + position);
          break;
        }
        case SyncData.Velocity: {
          Vector2 velocity = engine.getMapper(Velocity.class).get(entityId).velocity;
          com.riiablo.net.packet.d2gs.Velocity data = (com.riiablo.net.packet.d2gs.Velocity) s.data(new com.riiablo.net.packet.d2gs.Velocity(), i);
          velocity.x = data.x();
          velocity.y = data.y();
          Gdx.app.log(TAG, "  " + velocity);
          break;
        }
        case SyncData.Angle: {
          Vector2 angle = engine.getMapper(Angle.class).get(entityId).target;
          com.riiablo.net.packet.d2gs.Angle data = (com.riiablo.net.packet.d2gs.Angle) s.data(new com.riiablo.net.packet.d2gs.Angle(), i);
          angle.x = data.x();
          angle.y = data.y();
          Gdx.app.log(TAG, "  " + angle);
          break;
        }
        default:
          Gdx.app.error(TAG, "Unknown packet type: " + SyncData.name(s.dataType(i)));
      }
    }

    cofs.updateTransform(entityId, flags1);
    cofs.updateAlpha(entityId, flags2);
  }
}
