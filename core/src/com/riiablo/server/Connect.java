package com.riiablo.server;

import com.badlogic.ashley.core.Entity;

public class Connect {

  public int    id;
  public String name;
  public int    classId;
  public byte[] composites;
  public byte[] colors;

  private Connect() {}

  public Connect(String name, int classId, byte[] composites, byte[] colors) {
    this.name       = name;
    this.classId    = classId;
    this.composites = composites;
    this.colors     = colors;
  }

  public Connect(Entity player) {
//    Player.D2SStats stats = (Player.D2SStats) player.stats;
//    this.name       = stats.d2s.header.name;
//    this.classId    = stats.d2s.header.charClass;
//    this.composites = stats.d2s.header.composites;
//    this.colors     = stats.d2s.header.colors;
  }
}
