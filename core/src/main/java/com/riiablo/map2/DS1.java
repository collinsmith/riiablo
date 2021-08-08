package com.riiablo.map2;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.map.DT1;

public class DS1 {
  public static final int MAX_WALLS = 4;
  public static final int MAX_FLOORS = 2;
  public static final int MAX_SHADOWS = 1;
  public static final int MAX_TAGS = 1;
  public static final int MAX_LAYERS = MAX_WALLS + MAX_FLOORS + MAX_SHADOWS + MAX_TAGS;

  String fileName;

  int version;
  int width;
  int height;
  int act;
  int tagType;
  byte[] unknown;

  int numDependencies;
  String[] dependencies;

  int numWalls;
  int numFloors;
  int numTags;
  int numShadows;
  int layers;

  int wallRun, wallLen;
  int[] walls;
  int[] orientations;

  int floorRun, floorLen;
  int[] floors;

  int shadowRun, shadowLen;
  int[] shadows;

  int tagRun, tagLen;
  int[] tags;

  int numObjects;
  Ds1Object[] objects;

  int numGroups;
  Group[] groups;

  int numPaths;
  Path[] paths;

  /** Cache of locations of tiles matching {@link Orientation#isSpecial} */
  IntMap<Vector2> specialTiles;

  static final class Cell {
    private Cell() {}

    static final int MAIN_INDEX_OFFSET = 20;
    static final int MAIN_INDEX_BITS = 0x3F;

    static final int SUB_INDEX_OFFSET = 8;
    static final int SUB_INDEX_BITS = 0xFF;

    public static final int MAIN_INDEX_MASK = MAIN_INDEX_BITS << MAIN_INDEX_OFFSET; // 0x03F00000
    public static final int SUB_INDEX_MASK = SUB_INDEX_BITS << SUB_INDEX_OFFSET;  // 0x0000FF00
    public static final int UNWALKABLE_MASK   = 0x00020000;
    public static final int HIDDEN_MASK       = 0x80000000;
    public static final int FLOOR_UNWALK_MASK = 0x000000FF;
    public static final int FLAG_UNK1         = 0x00020000; // on some walls
    public static final int FLAG_UNK2         = 0x000000C2; // on most in layer 0

    static int tileIndex(int cell, int orientation) {
      return DT1.Tile.Index.create(orientation, mainIndex(cell), subIndex(cell));
    }

    static int mainIndex(int cell) {
      return (cell >>> MAIN_INDEX_OFFSET) & MAIN_INDEX_BITS;
    }

    static int subIndex(int cell) {
      return (cell >>> SUB_INDEX_OFFSET) & SUB_INDEX_BITS;
    }
  }

  static final class Ds1Object {
    static final Ds1Object[] EMPTY_OBJECT_ARRAY = new Ds1Object[0];

    public static final class Type {
      public static final int DYNAMIC = 1;
      public static final int STATIC = 2;

      public static String toString(int type) {
        switch (type) {
          case STATIC: return "STATIC";
          case DYNAMIC: return "DYNAMIC";
          default: return Integer.toString(type);
        }
      }
    }

    public boolean valid;
    public int type;
    public int id;
    public Vector2 position;
    public int flags;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("type", Type.toString(type))
          .append("id", id)
          .append("position", position)
          .append("flags", String.format("0x%08x", flags))
          .toString();
    }
  }

  static final class Group {
    static final Group[] EMPTY_GROUP_ARRAY = new Group[0];

    Rectangle bounds;
    int unk;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("bounds", bounds)
          .append("unk", String.format("%08x", unk))
          .toString();
    }
  }

  static final class Path {
    static final Path[] EMPTY_PATH_ARRAY = new Path[0];

    public int numWaypoints;
    public Waypoint[] waypoints;
    public Vector2 position;

    static final class Waypoint extends Vector2 {
      public int action;
    }
  }
}
