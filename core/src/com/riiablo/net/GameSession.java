package com.riiablo.net;

public class GameSession {
  public String name;
  public String password;
  public String desc;

  public GameSession() {}

  public GameSession(com.riiablo.net.packet.mcp.GameSession game) {
    name = game.name();
    desc = game.desc();
  }

  @Override
  public String toString() {
    return name;
  }
}
