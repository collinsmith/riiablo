package com.riiablo.engine.client;

import com.google.flatbuffers.ByteBufferUtil;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.physics.box2d.Body;
import com.riiablo.CharData;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.map.Map;
import com.riiablo.net.packet.d2gs.AngleP;
import com.riiablo.net.packet.d2gs.ClassP;
import com.riiablo.net.packet.d2gs.CofAlphasP;
import com.riiablo.net.packet.d2gs.CofComponentsP;
import com.riiablo.net.packet.d2gs.CofTransformsP;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.DS1ObjectWrapperP;
import com.riiablo.net.packet.d2gs.Disconnect;
import com.riiablo.net.packet.d2gs.PlayerP;
import com.riiablo.net.packet.d2gs.PositionP;
import com.riiablo.net.packet.d2gs.Sync;
import com.riiablo.net.packet.d2gs.SyncData;
import com.riiablo.net.packet.d2gs.VelocityP;
import com.riiablo.util.ArrayUtils;
import com.riiablo.util.DebugUtils;
import com.riiablo.widget.TextArea;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

@All
public class ClientNetworkReceiver extends IntervalSystem {
  private static final String TAG = "ClientNetworkReceiver";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_PACKET  = DEBUG && !true;
  private static final boolean DEBUG_SYNC    = DEBUG && !true;

//  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<Box2DBody> mBox2DBody;

  protected CofManager cofs;
  protected NetworkIdManager syncIds;

  @Wire(name="client.socket")
  protected Socket socket;

  @Wire(name = "factory")
  protected EntityFactory factory;

  @Wire(name = "map")
  protected Map map;

  @Wire(name = "output")
  protected TextArea output;

  private final ByteBuffer buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);

  public ClientNetworkReceiver() {
    super(null, 1 / 60f);
  }

  @Override
  protected void processSystem() {
    InputStream in = socket.getInputStream();
    try {
      if (in.available() > 0) {
        ReadableByteChannel channel = Channels.newChannel(in);
        buffer.clear();
        int i = channel.read(buffer);
        buffer.rewind().limit(i);
        D2GS d2gs = new D2GS();
        int p = 0;
        while (buffer.hasRemaining()) {
          int size = ByteBufferUtil.getSizePrefix(buffer);
          D2GS.getRootAsD2GS(ByteBufferUtil.removeSizePrefix(buffer), d2gs);
          if (DEBUG_PACKET) Gdx.app.debug(TAG, p++ + " packet type " + D2GSData.name(d2gs.dataType()) + ":" + size + "B");
          process(d2gs);
//          System.out.println(buffer.position() + "->" + (buffer.position() + size + 4));
          buffer.position(buffer.position() + size + 4);
        }
      }
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
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
    int entityId = factory.createPlayer(map, zone, charData, origin);
    syncIds.put(connection.entityId(), entityId);
    int[] component = mCofComponents.get(entityId).component;
    for (int i = 0; i < 16; i++) component[i] = connection.cofComponents(i);
    float[] alpha = mCofAlphas.get(entityId).alpha;
    for (int i = 0; i < 16; i++) alpha[i] = connection.cofAlphas(i);
    byte[] transform = mCofTransforms.get(entityId).transform;
    for (int i = 0; i < 16; i++) transform[i] = (byte) connection.cofTransforms(i);

    int alphaFlags = Dirty.NONE;
    int transformFlags = Dirty.NONE;
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
    cofs.setWClass(entityId, Engine.WEAPON_1HS); // TODO...

    System.out.println("  " + DebugUtils.toByteArray(ArrayUtils.toByteArray(component)));
    System.out.println("  " + Arrays.toString(alpha));
    System.out.println("  " + DebugUtils.toByteArray(transform));
  }

  private void Disconnect(D2GS packet) {
    Disconnect disconnect = (Disconnect) packet.data(new Disconnect());
    int serverEntityId = disconnect.entityId();
    System.out.println("serverEntityId=" + serverEntityId);
    int entityId = syncIds.get(serverEntityId);

    CharData data = mPlayer.get(entityId).data;

    output.appendText(Riiablo.string.format(3642, data.getD2S().header.name));
    output.appendText("\n");

    world.delete(entityId);
    Body body = mBox2DBody.get(entityId).body;
    if (body != null) ;
  }

  private int findType(Sync s) {
    for (int i = 0, len = s.dataTypeLength(); i < len; i++) {
      if (s.dataType(i) == SyncData.ClassP) {
        return ((ClassP) s.data(new ClassP(), i)).type();
      }
    }

    return -1;
  }

  private DS1ObjectWrapperP findDS1ObjectWrapper(Sync s) {
    for (int i = 0, len = s.dataTypeLength(); i < len; i++) {
      if (s.dataType(i) == SyncData.DS1ObjectWrapperP) {
        return (DS1ObjectWrapperP) s.data(new DS1ObjectWrapperP(), i);
      }
    }

    return null;
  }

  private PlayerP findPlayer(Sync s) {
    for (int i = 0, len = s.dataTypeLength(); i < len; i++) {
      if (s.dataType(i) == SyncData.PlayerP) {
        return (PlayerP) s.data(new PlayerP(), i);
      }
    }

    return null;
  }

  private int createEntity(Sync sync) {
    assert syncIds.get(sync.entityId()) == Engine.INVALID_ENTITY;
    Class.Type type = Class.Type.valueOf(findType(sync));
    switch (type) {
      case OBJ:
        return Engine.INVALID_ENTITY;
      case MON:
        DS1ObjectWrapperP ds1ObjectWrapper = findDS1ObjectWrapper(sync);
        if (ds1ObjectWrapper != null) {
          Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
          if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
          if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
          Map.Zone zone = map.getZone(origin);
          String objectType = Riiablo.files.MonPreset.getPlace(ds1ObjectWrapper.act(), ds1ObjectWrapper.id());
          MonStats.Entry monstats = Riiablo.files.monstats.get(objectType);
          int entityId = factory.createMonster(map, zone, monstats, 0, 0);
//          syncIds.put(sync.entityId(), entityId);
          System.out.println("testa creating monster " + monstats.Code);
          return entityId;
        }

        return Engine.INVALID_ENTITY;
      case PLR: {
        PlayerP player = findPlayer(sync);
        CharData charData = new CharData().createD2S(player.charName(), CharacterClass.get(player.charClass()));

        // TODO: assert entity id is player
        // TODO: add support for other entity types
        Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
        if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
        if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
        Map.Zone zone = map.getZone(origin);
        int entityId = factory.createPlayer(map, zone, charData, origin);
//        syncIds.put(sync.entityId(), entityId);

        cofs.setMode(entityId, Engine.Player.MODE_TN);
        cofs.setWClass(entityId, Engine.WEAPON_1HS); // TODO...
        return entityId;
      }
      default:
        return Engine.INVALID_ENTITY;
    }
  }

  private void Synchronize(D2GS packet) {
    Sync sync = (Sync) packet.data(new Sync());
    int entityId = syncIds.get(sync.entityId());
    if (entityId == Engine.INVALID_ENTITY) {
      syncIds.put(sync.entityId(), entityId = createEntity(sync));
    }

    int tFlags = Dirty.NONE;
    int aFlags = Dirty.NONE;
    if (DEBUG_SYNC) Gdx.app.debug(TAG, "syncing " + entityId);
    for (int i = 0, len = sync.dataTypeLength(); i < len; i++) {
      switch (sync.dataType(i)) {
        case SyncData.ClassP:
        case SyncData.PlayerP:
        case SyncData.DS1ObjectWrapperP:
          break;
        case SyncData.CofComponentsP: {
          CofComponentsP data = (CofComponentsP) sync.data(new CofComponentsP(), i);
          for (int j = 0, s0 = data.componentLength(); j < s0; j++) {
            cofs.setComponent(entityId, j, data.component(j));
          }
          break;
        }
        case SyncData.CofTransformsP: {
          CofTransformsP data = (CofTransformsP) sync.data(new CofTransformsP(), i);
          for (int j = 0, s0 = data.transformLength(); j < s0; j++) {
            tFlags |= cofs.setTransform(entityId, j, (byte) data.transform(j));
          }
          break;
        }
        case SyncData.CofAlphasP: {
          CofAlphasP data = (CofAlphasP) sync.data(new CofAlphasP(), i);
          for (int j = 0, s0 = data.alphaLength(); j < s0; j++) {
            aFlags |= cofs.setAlpha(entityId, j, data.alpha(j));
          }
          break;
        }
        case SyncData.PositionP: {
          Vector2 position = mPosition.get(entityId).position;
          PositionP data = (PositionP) sync.data(new PositionP(), i);
          position.x = data.x();
          position.y = data.y();
          if (mBox2DBody.has(entityId)) {
            Body body = mBox2DBody.get(entityId).body;
            if (body != null) body.setTransform(position, body.getAngle());
          }
          //Gdx.app.log(TAG, "  " + position);
          break;
        }
        case SyncData.VelocityP: {
          Vector2 velocity = mVelocity.get(entityId).velocity;
          VelocityP data = (VelocityP) sync.data(new VelocityP(), i);
          velocity.x = data.x();
          velocity.y = data.y();
          //Gdx.app.log(TAG, "  " + velocity);
          break;
        }
        case SyncData.AngleP: {
          Vector2 angle = mAngle.get(entityId).target;
          AngleP data = (AngleP) sync.data(new AngleP(), i);
          angle.x = data.x();
          angle.y = data.y();
          //Gdx.app.log(TAG, "  " + angle);
          break;
        }
        default:
          Gdx.app.error(TAG, "Unknown packet type: " + SyncData.name(sync.dataType(i)));
      }
    }

    cofs.updateTransform(entityId, tFlags);
    cofs.updateAlpha(entityId, aFlags);
  }
}
