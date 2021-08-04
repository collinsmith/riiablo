package com.riiablo.map2;

import org.apache.commons.lang3.ArrayUtils;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.Riiablo;
import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.util.DebugUtils;

import static com.riiablo.map2.Orientation.FLOOR;
import static com.riiablo.map2.Orientation.LEFT_END_WALL;
import static com.riiablo.map2.Orientation.LEFT_WALL;
import static com.riiablo.map2.Orientation.LEFT_WALL_DOOR;
import static com.riiablo.map2.Orientation.LOWER_LEFT_WALL;
import static com.riiablo.map2.Orientation.LOWER_NORTH_CORNER_WALL;
import static com.riiablo.map2.Orientation.LOWER_RIGHT_WALL;
import static com.riiablo.map2.Orientation.PILLAR;
import static com.riiablo.map2.Orientation.RIGHT_END_WALL;
import static com.riiablo.map2.Orientation.RIGHT_NORTH_CORNER_WALL;
import static com.riiablo.map2.Orientation.RIGHT_WALL;
import static com.riiablo.map2.Orientation.RIGHT_WALL_DOOR;
import static com.riiablo.map2.Orientation.ROOF;
import static com.riiablo.map2.Orientation.SHADOW;
import static com.riiablo.map2.Orientation.SOUTH_CORNER_WALL;
import static com.riiablo.map2.Orientation.SPECIAL_10;
import static com.riiablo.map2.Orientation.SPECIAL_11;
import static com.riiablo.map2.Orientation.TREE;
import static com.riiablo.map2.Orientation.UNKNOWN_20;

public class DS1Reader {
  private static final Logger log = LogManager.getLogger(DS1Reader.class);

  private static final int WALL_OFFSET = 0;
  private static final int ORIENTATION_OFFSET = WALL_OFFSET + DS1.MAX_WALLS;
  private static final int FLOOR_OFFSET = ORIENTATION_OFFSET + DS1.MAX_WALLS;
  private static final int SHADOW_OFFSET = FLOOR_OFFSET + DS1.MAX_FLOORS;
  private static final int TAG_OFFSET = SHADOW_OFFSET + DS1.MAX_SHADOWS;
  private static final int MAX_LAYERS = TAG_OFFSET + DS1.MAX_TAGS;

  static final int WALL_LAYER_0 = 0;
  static final int WALL_LAYER_1 = 1;
  static final int WALL_LAYER_2 = 2;
  static final int WALL_LAYER_3 = 3;
  static final int ORIENTATION_LAYER_0 = 4;
  static final int ORIENTATION_LAYER_1 = 5;
  static final int ORIENTATION_LAYER_2 = 6;
  static final int ORIENTATION_LAYER_3 = 7;
  static final int FLOOR_LAYER_0 = 8;
  static final int FLOOR_LAYER_1 = 9;
  static final int SHADOW_LAYER_0 = 10;
  static final int TAG_LAYER_0 = 11;

  static String layerToString(int layer) {
    switch (layer) {
      case WALL_LAYER_0: return "WALL_LAYER_0";
      case WALL_LAYER_1: return "WALL_LAYER_1";
      case WALL_LAYER_2: return "WALL_LAYER_2";
      case WALL_LAYER_3: return "WALL_LAYER_3";
      case ORIENTATION_LAYER_0: return "ORIENTATION_LAYER_0";
      case ORIENTATION_LAYER_1: return "ORIENTATION_LAYER_1";
      case ORIENTATION_LAYER_2: return "ORIENTATION_LAYER_2";
      case ORIENTATION_LAYER_3: return "ORIENTATION_LAYER_3";
      case FLOOR_LAYER_0: return "FLOOR_LAYER_0";
      case FLOOR_LAYER_1: return "FLOOR_LAYER_1";
      case SHADOW_LAYER_0: return "SHADOW_LAYER_0";
      case TAG_LAYER_0: return "TAG_LAYER_0";
      default: return "UNKNOWN_LAYER(" + layer + ")";
    }
  }

  static final byte ORIENTATION_TABLE[] = {
      FLOOR,
      LEFT_WALL,
      RIGHT_WALL,
      LEFT_WALL,
      RIGHT_WALL,
      RIGHT_NORTH_CORNER_WALL,
      RIGHT_NORTH_CORNER_WALL,
      LEFT_END_WALL,
      LEFT_END_WALL,
      RIGHT_END_WALL,
      RIGHT_END_WALL,
      SOUTH_CORNER_WALL,
      SOUTH_CORNER_WALL,
      LEFT_WALL_DOOR,
      RIGHT_WALL_DOOR,
      SPECIAL_10,
      SPECIAL_11,
      PILLAR,
      SHADOW,
      TREE,
      ROOF,
      LOWER_LEFT_WALL,
      LOWER_RIGHT_WALL,
      LOWER_NORTH_CORNER_WALL,
      UNKNOWN_20,
  };

  public DS1 readDs1(String fileName, ByteInput in) {
    DS1 ds1 = new DS1();
    ds1.fileName = fileName;
    try {
      MDC.put("ds1", ds1.fileName);
      readDs1(in, ds1);
      if (in.bytesRemaining() > 0) { // FIXME: https://github.com/collinsmith/riiablo/issues/73
        log.warn("{}B remaining in stream! ds1 version: {}", in.bytesRemaining(), ds1.version);
      }
      return ds1;
    } finally {
      MDC.remove("ds1");
    }
  }

  DS1 readDs1(ByteInput in, DS1 ds1) {
    log.trace("Reading ds1...");
    int version = ds1.version = in.readSafe32u();
    log.trace("version: {}", ds1.version);
    ds1.width = in.readSafe32u();
    log.trace("width: {}", ds1.width);
    ds1.height = in.readSafe32u();
    log.trace("height: {}", ds1.height);
    ds1.act = version < 8 ? 1 : Math.min(in.readSafe32u() + 1, Riiablo.NUM_ACTS);
    log.trace("act: {}", ds1.act);
    ds1.tagType = version < 10 ? 0 : in.read32();
    log.trace("tagType: {}", ds1.tagType);
    ds1.numDependencies = version < 3 ? 0 : in.readSafe32u();
    ds1.dependencies = ds1.numDependencies == 0
        ? ArrayUtils.EMPTY_STRING_ARRAY
        : new String[ds1.numDependencies];
    try {
      MDC.put("ds1.section", "dependencies");
      for (int i = 0, s = ds1.numDependencies; i < s; i++) {
        String dependency = ds1.dependencies[i] = in.readString();
        log.trace("{}: {}", i, dependency);
      }
    } finally {
      MDC.remove("ds1.section");
    }

    if (9 <= version && version <= 13) {
      // Unused -- I think this indicates some max layer bounds?
      int fileOffset = in.bytesRead();
      ds1.unknown = in.readBytes(8);
      log.warnf("Unknown bytes +%08X: %s", fileOffset, DebugUtils.toByteArray(ds1.unknown));
    } else {
      ds1.unknown = ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    if (version < 4) {
      ds1.numWalls = 1;
      ds1.numFloors = 1;
      ds1.numShadows = 1;
      ds1.numTags = 1;
    } else {
      ds1.numWalls = in.readSafe32u();
      ds1.numFloors = version < 16 ? 1 : in.readSafe32u();
      ds1.numShadows = 1;
      ds1.numTags = version >= 10 && (ds1.tagType == 1 || ds1.tagType == 2) ? 1 : 0;
    }

    log.trace("layers: {} walls (+{} orients) + {} floors + {} shadows + {} tags",
        ds1.numWalls, ds1.numWalls, ds1.numFloors, ds1.numShadows, ds1.numTags);

    ds1.layers |= ((1 << ds1.numWalls) - 1);
    ds1.layers |= ((1 << ds1.numFloors) - 1) << (DS1.MAX_WALLS);
    ds1.layers |= ((1 << ds1.numShadows) - 1) << (DS1.MAX_WALLS + DS1.MAX_FLOORS);
    ds1.layers |= ((1 << ds1.numTags) - 1) << (DS1.MAX_WALLS + DS1.MAX_FLOORS + DS1.MAX_SHADOWS);

    log.tracef("layer flags: %08x", ds1.layers);

    ds1.specialTiles = new IntMap<>(8);

    try {
      MDC.put("ds1.section", "layers");
      setupRuns(ds1);
      readLayers(in, ds1);

      MDC.put("ds1.section", "objects");
      readObjects(in, ds1);

      MDC.put("ds1.section", "groups");
      readGroups(in, ds1);

      MDC.put("ds1.section", "paths");
      readPaths(in, ds1);
    } finally {
      MDC.remove("ds1.section");
    }
    return ds1;
  }

  /**
   * encoding 2D array into 1D:
   *   run -> width
   *   len -> count
   *   height => len / run
   *
   * TODO: make DS1 disposable and pool cell containers
   */
  void setupRuns(DS1 ds1) {
    final int width = ds1.width + 1;
    final int height = ds1.height + 1;

    ds1.wallRun = width * ds1.numWalls;
    ds1.wallLen = ds1.wallRun * height;
    ds1.walls = new int[ds1.wallLen];
    ds1.orientations = new int[ds1.wallLen];

    ds1.floorRun = width * ds1.numFloors;
    ds1.floorLen = ds1.floorRun * height;
    ds1.floors = new int[ds1.floorLen];

    ds1.shadowRun = width * ds1.numFloors;
    ds1.shadowLen = ds1.shadowRun * height;
    ds1.shadows = new int[ds1.shadowLen];

    ds1.tagRun = width * ds1.numTags;
    ds1.tagLen = ds1.tagRun * height;
    ds1.tags = new int[ds1.tagLen];
  }

  void readLayers(ByteInput in, DS1 ds1) {
    int[] wallOffset = new int[ds1.numWalls];
    int[] orientOffset = new int[ds1.numWalls];
    for (int i = 0, s = ds1.numWalls; i < s; i++) wallOffset[i] = orientOffset[i] = i;

    int[] floorOffset = new int[ds1.numFloors];
    for (int i = 0, s = ds1.numFloors; i < s; i++) floorOffset[i] = i;

    int[] shadowOffset = new int[ds1.numShadows];
    for (int i = 0, s = ds1.numShadows; i < s; i++) shadowOffset[i] = i;

    int[] tagOffset = new int[ds1.numTags];
    for (int i = 0, s = ds1.numTags; i < s; i++) tagOffset[i] = i;

    final int width = ds1.width + 1;
    final int height = ds1.height + 1;
    IntArray layers = queueLayers(ds1);
    for (int l = 0, layer, numLayers = layers.size; l < numLayers; l++) {
      layer = layers.get(l);
      log.trace("Reading layer: {}", layerToString(layer));
      if (layer >= MAX_LAYERS || layer < 0) {
        throw new InvalidFormat(in, String.format("Unsupported layer %d (%s)", layer, layerToString(layer)));
      } else if (layer >= TAG_LAYER_0) {
        readCells(in, width, height, ds1.tags, ds1.numTags, tagOffset, layer - TAG_LAYER_0);
      } else if (layer >= SHADOW_LAYER_0) {
        readCells(in, width, height, ds1.shadows, ds1.numShadows, shadowOffset, layer - SHADOW_LAYER_0);
      } else if (layer >= FLOOR_LAYER_0) {
        readCells(in, width, height, ds1.floors, ds1.numFloors, floorOffset, layer - FLOOR_LAYER_0);
      } else if (layer >= ORIENTATION_LAYER_0) {
        readOrientations(in, width, height, ds1.version < 7, ds1.specialTiles, ds1.walls, ds1.orientations, ds1.numWalls, orientOffset, layer - ORIENTATION_LAYER_0);
      } else if (layer >= WALL_LAYER_0) {
        readCells(in, width, height, ds1.walls, ds1.numWalls, wallOffset, layer - WALL_LAYER_0);
      }
    }
  }

  IntArray queueLayers(DS1 ds1) {
    IntArray layers = new IntArray(MAX_LAYERS);
    if (ds1.version < 4) {
      layers.add(WALL_LAYER_0);
      layers.add(FLOOR_LAYER_0); // NOT ORIENTATION!
      layers.add(ORIENTATION_LAYER_0);
      layers.add(TAG_LAYER_0);
      layers.add(SHADOW_LAYER_0);
    } else {
      assert ds1.numWalls < DS1.MAX_WALLS
          : "ds1.numWalls(" + ds1.numWalls + ") >= DS1.MAX_WALLS(" + DS1.MAX_WALLS + ")";
      for (int i = 0; i < ds1.numWalls; i++) {
        layers.add(WALL_LAYER_0 + i);
        layers.add(ORIENTATION_LAYER_0 + i);
      }
      assert ds1.numFloors < DS1.MAX_FLOORS
          : "ds1.numFloors(" + ds1.numFloors + ") >= DS1.MAX_FLOORS(" + DS1.MAX_FLOORS + ")";
      for (int i = 0; i < ds1.numFloors; i++) {
        layers.add(FLOOR_LAYER_0 + i);
      }
      if (ds1.numShadows > 0) {
        layers.add(SHADOW_LAYER_0);
      }
      if (ds1.numTags > 0) {
        layers.add(TAG_LAYER_0);
      }
    }
    return layers;
  }

  void readCells(
      ByteInput in,
      int width,
      int height,
      int[] cells,
      int numCells,
      int[] offsets,
      int layer
  ) {
    int offset = offsets[layer];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        cells[offset] = in.read32();
        offset += numCells;
      }
    }

    offsets[layer] = offset;
  }

  void readOrientations(
      ByteInput in,
      int width,
      int height,
      boolean useTable,
      IntMap<Vector2> specialTiles,
      int[] walls,
      int[] orientations,
      int numCells,
      int[] offsets,
      int layer
  ) {
    int offset = offsets[layer];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final int orientation = orientations[offset] = useTable
            ? ORIENTATION_TABLE[in.readSafe32u()]
            : in.readSafe32u();
        if (Orientation.isSpecial(orientation)) {
          final int cell = walls[offset];
          specialTiles.put(
              DS1.Cell.tileIndex(cell, orientation),
              new Vector2(x, y));
          if (log.debugEnabled()) {
            final int mainIndex = DS1.Cell.mainIndex(cell);
            final int subIndex = DS1.Cell.subIndex(cell);
            final int first = (cell & 0xFC000000) >>> 26;
            final int second = (cell & 0x000F0000) >>> 16;
            final int third = (cell & 0x000000FF);
            //           O  L (  X,  Y) CELL      M,  O,  S       ?    M  ?    S    ?
            log.debugf("%s %s (%2d,%2d) %08X    %2d,%2d,%2d    %02X %02X %X %02X %02X",
                Orientation.toString(orientation), layerToString(layer), x, y, cell,
                mainIndex, orientation, subIndex,
                first, mainIndex, second, subIndex, third);
          }
        }
        offset += numCells;
      }
    }

    offsets[layer] = offset;
  }

  void readObjects(ByteInput in, DS1 ds1) {
    ds1.numObjects = ds1.version < 2 ? 0 : in.readSafe32u();
    ds1.objects = ds1.numObjects == 0 ? DS1.Ds1Object.EMPTY_OBJECT_ARRAY : new DS1.Ds1Object[ds1.numObjects];
    Rectangle ds1Bounds = ds1.numObjects <= 0 ? null : new Rectangle(
        0f,
        0f,
        ds1.width * DT1.Tile.SUBTILE_SIZE,
        ds1.height * DT1.Tile.SUBTILE_SIZE);
    for (int i = 0, s = ds1.numObjects; i < s; i++) {
      try {
        MDC.put("object", i);
        DS1.Ds1Object object = ds1.objects[i] = readObject(in, ds1, ds1Bounds);
        if (!object.valid) {
          log.warn("object outside of DS1 bounds: {} (bounds: {})", object, ds1Bounds);
        }
      } finally {
        MDC.remove("object");
      }
    }
  }

  DS1.Ds1Object readObject(ByteInput in, DS1 ds1, Rectangle ds1Bounds) {
    DS1.Ds1Object object = new DS1.Ds1Object();
    object.type = in.readSafe32u();
    log.trace("object.type: {}", DS1.Ds1Object.Type.toString(object.type));
    object.id = in.readSafe32u();
    log.trace("object.id: {}", object.id);
    object.position = new Vector2(in.readSafe32u(), in.readSafe32u());
    object.valid = ds1Bounds.contains(object.position);
    log.trace("object.position: {} (valid: {})", object.position, object.valid);
    object.flags = ds1.version < 6 ? 0 : in.read32();
    log.tracef("object.flags: %08x", object.flags);
    return object;
  }

  void readGroups(ByteInput in, DS1 ds1) {
    int version = ds1.version;
    if (version >= 12 && (ds1.tagType == 1 || ds1.tagType == 2)) {
      if (version >= 18) in.skipBytes(4);
      ds1.numGroups = in.readSafe32u();
      ds1.groups = ds1.numGroups == 0 ? DS1.Group.EMPTY_GROUP_ARRAY : new DS1.Group[ds1.numGroups];
      for (int i = 0, s = ds1.numGroups; i < s; i++) {
        try {
          MDC.put("group", i);
          ds1.groups[i] = readGroup(in, ds1);
        } catch (Throwable t) {
          log.warn("Invalid group {}", i, t);
        } finally {
          MDC.remove("group");
        }
      }
    } else {
      ds1.numGroups = 0;
      ds1.groups = DS1.Group.EMPTY_GROUP_ARRAY;
    }
  }

  DS1.Group readGroup(ByteInput in, DS1 ds1) {
    DS1.Group group = new DS1.Group();
    group.bounds = new Rectangle(
        in.readSafe32u(),
        in.readSafe32u(),
        in.readSafe32u(),
        in.readSafe32u()
    );
    log.trace("group.bounds: {}", group.bounds);
    if (ds1.version >= 13) {
      group.unk = in.read32();
      log.tracef("group.unk: %08x", group.unk);
    }
    return group;
  }

  void readPaths(ByteInput in, DS1 ds1) {
    int version = ds1.version;
    if (version >= 14) {
      ds1.numPaths = in.readSafe32u();
      ds1.paths = ds1.numPaths == 0 ? DS1.Path.EMPTY_PATH_ARRAY : new DS1.Path[ds1.numPaths];
      for (int i = 0, s = ds1.numPaths; i < s; i++) {
        try {
          MDC.put("path", i);
          ds1.paths[i] = readPath(in, ds1);
        } catch (Throwable t) {
          log.warn("Invalid path {}", i, t);
        } finally {
          MDC.remove("path");
        }
      }
    } else {
      ds1.numPaths = 0;
      ds1.paths = DS1.Path.EMPTY_PATH_ARRAY;
    }
  }

  DS1.Path readPath(ByteInput in, DS1 ds1) {
    DS1.Path path = new DS1.Path();
    path.numWaypoints = in.readSafe32u();
    log.trace("path.numWaypoints: {}", path.numWaypoints);
    path.waypoints = new DS1.Path.Waypoint[path.numWaypoints];
    path.position = new Vector2(
        in.readSafe32u(),
        in.readSafe32u());
    log.trace("path.position: {}", path.position);
    for (int wp = 0, s = path.numWaypoints; wp < s; wp++) {
      try {
        MDC.put("waypoint", wp);
        path.waypoints[wp] = readWaypoint(in, ds1);
      } finally {
        MDC.remove("waypoint");
      }
    }

    return path;
  }

  DS1.Path.Waypoint readWaypoint(ByteInput in, DS1 ds1) {
    DS1.Path.Waypoint waypoint = new DS1.Path.Waypoint();
    waypoint.set(in.readSafe32u(), in.readSafe32u());
    log.trace("waypoint.position: {}", waypoint);
    waypoint.action = ds1.version < 15 ? 1 : in.readSafe32u();
    log.trace("waypoint.action: {}", waypoint.action);
    return waypoint;
  }
}
