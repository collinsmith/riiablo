package com.riiablo.net;

import com.riiablo.net.packet.mcp.CreateGame;
import com.riiablo.net.packet.mcp.JoinGame;
import com.riiablo.net.packet.msi.StartInstance;

public class GameSession {
  public String name;
  public String password;
  public String desc;
  public int numPlayers;
  public int ip;
  public short port;

  public GameSession() {}

  public GameSession(com.riiablo.net.packet.mcp.GameSession game) {
    name = game.name();
    desc = game.desc();
  }

  public GameSession(CreateGame game) {
    name = game.gameName();
    desc = game.description();
  }

  GameSession(Builder builder) {
    name = builder.name;
    password = builder.password;
    desc = builder.desc;
  }

  public GameSession setConnectInfo(StartInstance info) {
    ip   = info.ip();
    port = info.port();
    return this;
  }

  public GameSession setConnectInfo(JoinGame info) {
    ip   = info.ip();
    port = info.port();
    return this;
  }

  @Override
  public String toString() {
    return name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public String name;
    public String password;
    public String desc;

    public GameSession build() {
      return new GameSession(this);
    }
  }
}
