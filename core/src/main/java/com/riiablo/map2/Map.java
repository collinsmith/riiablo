package com.riiablo.map2;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.map2.util.ZoneGraph;

public class Map {
  /**
   * LevelType.Id -> TileGenerator
   * I.e., procedurally generates a random tile using its specifier
   */
  final IntMap<TileGenerator> tileGens = new IntMap<>();

  final ZoneGraph zones = new ZoneGraph();
  final Array<Chunk> chunks = new Array<>();

  void addZone(int x, int y, int width, int height) {
    // Zone zone = zones.claim(x, y, width, height);
  }

  void set(DS1 ds1) {
    // should zone contain ds1s and dt1s?
  }

  // zones have a:
  // collision map (dt1 + ds1 tile flags)
  // tiles map (specific dt1 tiles for rendering)
  // presets map
  // specials list
  // warps list (are entities?)
  // entity list to help with loading/unloading?

  // should zones be chunked
  // assign seed to each preset

  // generate map using seed:
  //   layout / position bbox for zones
  //   assign entrances/exits for adjacent zones
}
