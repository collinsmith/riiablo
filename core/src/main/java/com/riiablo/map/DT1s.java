package com.riiablo.map;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;

public class DT1s {
  // TODO: tiles and prob are both keyed with tile ID, can speed up if using one map to Pair<prob, tiles>
  ObjectSet<DT1>          dt1s  = new ObjectSet<>();
  IntMap<Array<DT1.Tile>> tiles = new IntMap<>();
  IntIntMap               prob  = new IntIntMap();

  void add(DT1.Tile tile) {
    //if (tile.rarity == 0) return;
    Array<DT1.Tile> tiles = this.tiles.get(tile.id);
    if (tiles == null) this.tiles.put(tile.id, tiles = new Array<>());
    tiles.add(tile);
    prob.getAndIncrement(tile.id, 0, tile.rarity);
  }

  void remove(DT1.Tile tile) {
    //if (tile.rarity == 0) return;
    Array<DT1.Tile> tiles = this.tiles.get(tile.id);
    if (tiles == null) return;
    tiles.removeValue(tile, true);
    prob.getAndIncrement(tile.id, 0, -tile.rarity);
  }

  public boolean add(DT1 dt1) {
    if (!dt1s.add(dt1)) return false;
    for (DT1.Tile tile : dt1.tiles) add(tile);
    return true;
  }

  public boolean remove(DT1 dt1) {
    if (!dt1s.remove(dt1)) return false;
    for (DT1.Tile tile : dt1.tiles) remove(tile);
    return true;
  }

  public DT1.Tile get(int orientation, int mainIndex, int subIndex) {
    int id = DT1.Tile.Index.create(orientation, mainIndex, subIndex);
    Array<DT1.Tile> tiles = this.tiles.get(id);
    return next(id, tiles);
  }

  public DT1.Tile get(DS1.Cell cell) {
    Array<DT1.Tile> tiles = this.tiles.get(cell.id);
    return next(cell.id, tiles);
  }

  public DT1.Tile get(int id) {
    Array<DT1.Tile> tiles = this.tiles.get(id);
    return next(id, tiles);
  }

  private DT1.Tile next(int id, Array<DT1.Tile> tiles) {
    if (tiles == null) return null;
    int sum = prob.get(id, 0);
    int random = sum == 0 ? 0 : MathUtils.random(sum - 1);
    for (DT1.Tile tile : tiles) {
      random -= tile.rarity;
      if (random <= 0) {
        return tile;
      }
    }

    return null;
  }

  public void clear() {
    dt1s.clear();
    tiles.clear();
    prob.clear();
  }
}
