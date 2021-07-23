package com.riiablo.map2.util;

public enum DebugMode {
  UNSET,
  CHUNK,
  TILE,
  SUBTILE,
  ;

  public DebugMode next;

  static {
    UNSET.next = CHUNK;
    CHUNK.next = TILE;
    TILE.next = SUBTILE;
    SUBTILE.next = CHUNK;
  }
}
