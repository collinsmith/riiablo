package com.riiablo.net;

import com.riiablo.net.packet.mcp.CreateGame;

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
