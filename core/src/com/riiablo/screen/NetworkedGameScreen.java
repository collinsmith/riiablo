package com.riiablo.screen;

import com.artemis.Entity;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.riiablo.CharData;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.ClientEntityFactory;
import com.riiablo.engine.client.ClientNetworkSyncronizer;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.map.Map;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
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

  public NetworkedGameScreen(CharData charData, Socket socket) {
    super(charData, socket);
    this.socket = socket;
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

  private void process(D2GS packet) {
    switch (packet.dataType()) {
      case D2GSData.Connection:
        Connection(packet);
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

    System.out.println(Arrays.toString(component));
    System.out.println(Arrays.toString(alpha));
    System.out.println(DebugUtils.toByteArray(transform));
  }
}
