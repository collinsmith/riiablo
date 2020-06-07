package com.riiablo.screen;

import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.net.Socket;
import com.riiablo.engine.client.ClientNetworkReceiver;
import com.riiablo.engine.client.ClientNetworkSyncronizer;
import com.riiablo.engine.client.Pinger;
import com.riiablo.save.CharData;

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
    builder.with(WorldConfigurationBuilder.Priority.HIGH, new ClientNetworkReceiver());
    builder.with(new ClientNetworkSyncronizer());
    builder.with(new Pinger());
    return builder;
  }

  @Override
  public void dispose() {
    super.dispose();
    socket.dispose();
  }
}
