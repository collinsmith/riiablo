package com.riiablo.map;

import com.badlogic.gdx.utils.Pool;

public class TileArrayPool extends Pool<DT1.Tile[]> {
  final int size;

  public TileArrayPool(int size) {
    this.size = size;
  }

  @Override
  protected DT1.Tile[] newObject() {
    return new DT1.Tile[size];
  }
}
