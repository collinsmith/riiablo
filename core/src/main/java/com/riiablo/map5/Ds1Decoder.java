package com.riiablo.map5;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.Riiablo;
import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.io.UnsafeNarrowing;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.map5.util.BucketPool;
import com.riiablo.util.DebugUtils;

import static com.riiablo.map5.Ds1.SUBST_NONE;
import static com.riiablo.map5.Ds1.SUBST_RANDOM;
import static com.riiablo.map5.TileType.FLOOR;
import static com.riiablo.map5.TileType.LEFT_END_WALL;
import static com.riiablo.map5.TileType.LEFT_WALL;
import static com.riiablo.map5.TileType.LEFT_WALL_DOOR;
import static com.riiablo.map5.TileType.LOWER_LEFT_WALL;
import static com.riiablo.map5.TileType.LOWER_NORTH_CORNER_WALL;
import static com.riiablo.map5.TileType.LOWER_RIGHT_WALL;
import static com.riiablo.map5.TileType.LOWER_SOUTH_CORNER_WALL;
import static com.riiablo.map5.TileType.PILLAR;
import static com.riiablo.map5.TileType.RIGHT_END_WALL;
import static com.riiablo.map5.TileType.RIGHT_NORTH_CORNER_WALL;
import static com.riiablo.map5.TileType.RIGHT_WALL;
import static com.riiablo.map5.TileType.RIGHT_WALL_DOOR;
import static com.riiablo.map5.TileType.ROOF;
import static com.riiablo.map5.TileType.SHADOW;
import static com.riiablo.map5.TileType.SOUTH_CORNER_WALL;
import static com.riiablo.map5.TileType.SPECIAL_10;
import static com.riiablo.map5.TileType.SPECIAL_11;
import static com.riiablo.map5.TileType.TREE;
import static com.riiablo.map5.TileType.UNKNOWN_20;

public class Ds1Decoder {
  private Ds1Decoder() {}

  private static final Logger log = LogManager.getLogger(Ds1Decoder.class);

  static final BucketPool<int[]> pool = BucketPool
      .builder(int[].class)
      .add(5*5)
      .add(9*9)
      .build();

  static int[] obtain(int length) {
    return pool.obtain(length);
  }

  static void free(int[] array) {
    pool.free(array);
  }

  private static final int MIN_VERSION = 1;
  private static final int MAX_VERSION = 18;
  public static boolean supports(int version) {
    return MIN_VERSION <= version && version <= MAX_VERSION;
  }

  static final int MAX_WALLS = 4;
  static final int MAX_FLOORS = 2;
  static final int MAX_SHADOWS = 1;
  static final int MAX_TAGS = 1;

  static final int WALL_OFFSET = 0;
  static final int TILETYPE_OFFSET = WALL_OFFSET + MAX_WALLS;
  static final int FLOOR_OFFSET = TILETYPE_OFFSET + MAX_WALLS;
  static final int SHADOW_OFFSET = FLOOR_OFFSET + MAX_FLOORS;
  static final int TAG_OFFSET = SHADOW_OFFSET + MAX_SHADOWS;
  static final int MAX_LAYERS = TAG_OFFSET + MAX_TAGS;

  static final int WALL_LAYER_0 = 0;
  static final int WALL_LAYER_1 = 1;
  static final int WALL_LAYER_2 = 2;
  static final int WALL_LAYER_3 = 3;
  static final int TILETYPE_LAYER_0 = 4;
  static final int TILETYPE_LAYER_1 = 5;
  static final int TILETYPE_LAYER_2 = 6;
  static final int TILETYPE_LAYER_3 = 7;
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
      case TILETYPE_LAYER_0: return "TILETYPE_LAYER_0";
      case TILETYPE_LAYER_1: return "TILETYPE_LAYER_1";
      case TILETYPE_LAYER_2: return "TILETYPE_LAYER_2";
      case TILETYPE_LAYER_3: return "TILETYPE_LAYER_3";
      case FLOOR_LAYER_0: return "FLOOR_LAYER_0";
      case FLOOR_LAYER_1: return "FLOOR_LAYER_1";
      case SHADOW_LAYER_0: return "SHADOW_LAYER_0";
      case TAG_LAYER_0: return "TAG_LAYER_0";
      default: return "UNKNOWN_LAYER(" + layer + ")";
    }
  }

  static final byte TILETYPE_TABLE[] = {
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
      FLOOR,
      FLOOR,
      LEFT_WALL,
      RIGHT_WALL,
      LEFT_END_WALL,
      LEFT_END_WALL,
      SOUTH_CORNER_WALL,
      RIGHT_WALL_DOOR,
      SPECIAL_11,
      SHADOW,
      TREE,
      ROOF,
      LOWER_LEFT_WALL,
      LOWER_RIGHT_WALL,
      LOWER_NORTH_CORNER_WALL,
      LOWER_SOUTH_CORNER_WALL,
      UNKNOWN_20,
};

  // @Deprecated
  // public static Ds1 decode(FileHandle handle) {
  //   return decode(handle, handle.read());
  // }

  // public static Ds1 decode(FileHandle handle, InputStream in) {
  //   return decode(handle, ByteInputStream.wrap(in, 0, (int) handle.length()));
  // }

  public static Ds1 decode(FileHandle handle, ByteInput in) {
    Ds1 ds1 = Ds1.obtain(handle);
    try {
      return decode(ds1, in);
    } catch (Throwable t) {
      ds1.dispose();
      return ExceptionUtils.rethrow(t);
    }
  }

  static Ds1 decode(Ds1 ds1, ByteInput in) {
    try {
      MDC.put("ds1", ds1.handle.path());
      final int version = ds1.version = in.readSafe32u();
      log.trace("ds1.version: {}", ds1.version);
      if (supports(version)) {
        readHeader(ds1, in);
        try {
          MDC.put("ds1.section", "layers");
          setupRuns(ds1);
          readLayers(ds1, in);

          MDC.put("ds1.section", "spawners");
          readSpawners(ds1, in);

          MDC.put("ds1.section", "groups");
          readGroups(ds1, in);

          MDC.put("ds1.section", "paths");
          readPaths(ds1, in);
        } finally {
          MDC.remove("ds1.section");
        }

        if (in.bytesRemaining() > 0) {
          log.warn("{}B remaining in stream for handle {}: {}", in.bytesRemaining(), ds1.handle, DebugUtils.toByteArray(in.readBytes(in.bytesRemaining())));
        }

        return ds1;
      } else {
        throw new InvalidFormat(
            in,
            String.format("Unsupported ds1 version: 0x%08x", ds1.version));
      }
    } catch (UnsafeNarrowing t) {
      throw new InvalidFormat(in, t);
    } finally {
      MDC.remove("ds1");
    }
  }

  static void readHeader(Ds1 ds1, ByteInput in) {
    final int version = ds1.version;
    ds1.width = in.readSafe32u();
    ds1.height = in.readSafe32u();
    ds1.act = version < 8 ? Riiablo.ACT1 : Math.min(in.readSafe32u(), Riiablo.ACT5);
    ds1.substMethod = version < 10 ? 0 : in.read32();
    final int numDependencies = version < 3 ? 0 : in.readSafe32u();
    try {
      MDC.put("ds1.section", "dependencies");
      for (int i = 0, s = numDependencies; i < s; i++) {
        String dependency = in.readString();
        ds1.dependencies.add(dependency);
      }
    } finally {
      MDC.remove("ds1.section");
    }

    if (9 <= version && version <= 13) {
      if (log.traceEnabled()) {
        byte[] unk = in.readBytes(8);
        log.tracef("Unknown bytes: %s", DebugUtils.toByteArray(unk));
      } else {
        in.skipBytes(8); // unknown
      }
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
      ds1.numTags = (version >= 10 && SUBST_NONE < ds1.substMethod && ds1.substMethod <= SUBST_RANDOM) ? 1 : 0;
    }
  }

  static void setupRuns(Ds1 ds1) {
    final int width = ds1.width + 1;
    final int height = ds1.height + 1;

    ds1.wallRun = width * ds1.numWalls;
    ds1.wallLen = ds1.wallRun * height;
    ds1.walls = obtain(ds1.wallLen);
    ds1.types = obtain(ds1.wallLen);

    ds1.floorRun = width * ds1.numFloors;
    ds1.floorLen = ds1.floorRun * height;
    ds1.floors = obtain(ds1.floorLen);

    ds1.shadowRun = width * ds1.numFloors;
    ds1.shadowLen = ds1.shadowRun * height;
    ds1.shadows = obtain(ds1.shadowLen);

    ds1.tagRun = width * ds1.numTags;
    ds1.tagLen = ds1.tagRun * height;
    ds1.tags = obtain(ds1.tagLen);
  }

  static void readLayers(Ds1 ds1, ByteInput in) {
    int[] wallOffset = new int[ds1.numWalls];
    int[] typeOffset = new int[ds1.numWalls];
    for (int i = 0, s = ds1.numWalls; i < s; i++) wallOffset[i] = typeOffset[i] = i;

    int[] floorOffset = new int[ds1.numFloors];
    for (int i = 0, s = ds1.numFloors; i < s; i++) floorOffset[i] = i;

    int[] shadowOffset = new int[ds1.numShadows];
    for (int i = 0, s = ds1.numShadows; i < s; i++) shadowOffset[i] = i;

    int[] tagOffset = new int[ds1.numTags];
    for (int i = 0, s = ds1.numTags; i < s; i++) tagOffset[i] = i;

    final int width = ds1.width + 1;
    final int height = ds1.height + 1;
    IntArray layers = queueLayers(ds1, in);
    final boolean useTypesTable = ds1.version < 7;
    try {
      for (int l = 0, layer, numLayers = layers.size; l < numLayers; l++) {
        layer = layers.get(l);
        MDC.put("ds1.layer", layerToString(layer));
        if (layer >= MAX_LAYERS || layer < 0) {
          throw new InvalidFormat(in, String.format(
              "Unsupported layer %d (%s)", layer, layerToString(layer)));
        } else if (layer >= TAG_LAYER_0) {
          readCells(in, width, height, ds1.tags, ds1.numTags, tagOffset, layer - TAG_LAYER_0);
        } else if (layer >= SHADOW_LAYER_0) {
          readCells(in, width, height, ds1.shadows, ds1.numShadows, shadowOffset, layer - SHADOW_LAYER_0);
        } else if (layer >= FLOOR_LAYER_0) {
          readCells(in, width, height, ds1.floors, ds1.numFloors, floorOffset, layer - FLOOR_LAYER_0);
        } else if (layer >= TILETYPE_LAYER_0) {
          readTileTypes(in, width, height, useTypesTable, ds1.specials, ds1.walls, ds1.types, ds1.numWalls, typeOffset, layer - TILETYPE_LAYER_0);
        } else if (layer >= WALL_LAYER_0) {
          readCells(in, width, height, ds1.walls, ds1.numWalls, wallOffset, layer - WALL_LAYER_0);
        }
      }
    } finally {
      MDC.remove("ds1.layer");
    }
  }

  static IntArray queueLayers(Ds1 ds1, ByteInput in) {
    IntArray layers = new IntArray();
    if (ds1.version < 4) {
      layers.add(WALL_LAYER_0);
      layers.add(FLOOR_LAYER_0); // NOT TILETYPE!
      layers.add(TILETYPE_LAYER_0);
      layers.add(TAG_LAYER_0);
      layers.add(SHADOW_LAYER_0);
    } else {
      if (ds1.numWalls >= MAX_WALLS) {
        throw new InvalidFormat(in, String.format(
            "ds1.numWalls(%d) >= MAX_WALLS(%d)", ds1.numWalls, MAX_WALLS));
      }
      for (int i = 0; i < ds1.numWalls; i++) {
        layers.add(WALL_LAYER_0 + i);
        layers.add(TILETYPE_LAYER_0 + i);
      }
      if (ds1.numFloors >= MAX_FLOORS) {
        throw new InvalidFormat(in, String.format(
            "ds1.numFloors(%d) >= MAX_FLOORS(%d)", ds1.numFloors, MAX_FLOORS));
      }
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

  static void readCells(
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

  static void readTileTypes(
      ByteInput in,
      int width,
      int height,
      boolean useTable,
      IntMap<Vector2> specials,
      int[] walls,
      int[] types,
      int numCells,
      int[] offsets,
      int layer
  ) {
    int offset = offsets[layer];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final int i = in.readSafe32u();
        final int tileType = types[offset] = useTable
            ? TILETYPE_TABLE[i]
            : i;
        if (TileType.isSpecial(tileType)) {
          final int cell = walls[offset];
          final int mainIndex = Cell.mainIndex(cell);
          final int subIndex = Cell.subIndex(cell);
          specials.put(
              Tile.Index.create(tileType, mainIndex, subIndex),
              new Vector2(x, y));
        }
        offset += numCells;
      }
    }

    offsets[layer] = offset;
  }

  static void readSpawners(Ds1 ds1, ByteInput in) {
    final int numSpawners = ds1.version < 2 ? 0 : in.readSafe32u();
    final Array<Ds1.Spawner> spawners = ds1.spawners;
    try {
      for (int i = 0, s = numSpawners; i < s; i++) {
        MDC.put("ds1.spawner", i);
        Ds1.Spawner spawner = readSpawner(ds1, in);
        spawners.add(spawner);
      }
    } finally {
      MDC.remove("ds1.spawner");
    }

    // tests if spawner position is outside the bounds of the ds1 preset space
    if (log.warnEnabled() && numSpawners > 0) {
      Rectangle ds1Bounds = new Rectangle(0f, 0f, ds1.width * Tile.SUBTILE_SIZE, ds1.height * Tile.SUBTILE_SIZE);
      Object[] items = spawners.items;
      for (int i = 0, s = numSpawners; i < s; i++) {
        Ds1.Spawner spawner = (Ds1.Spawner) items[i];
        boolean contains = ds1Bounds.contains(spawner.position);
        if (!contains) log.warn("spawner outside of ds1 bounds: {} (bounds: {})", spawner, ds1Bounds);
      }
    }
  }

  static Ds1.Spawner readSpawner(Ds1 ds1, ByteInput in) {
    Ds1.Spawner spawner = new Ds1.Spawner();
    spawner.type = in.readSafe32u();
    spawner.id = in.readSafe32u();
    spawner.position.set(in.readSafe32u(), in.readSafe32u());
    spawner.flags = ds1.version < 6 ? 0 : in.read32();
    return spawner;
  }

  static void readGroups(Ds1 ds1, ByteInput in) {
    final int version = ds1.version;
    if (version >= 12 && (SUBST_NONE < ds1.substMethod && ds1.substMethod <= SUBST_RANDOM)) {
      if (version >= 18) in.skipBytes(4);
      try {
        final int numGroups = in.readSafe32u();
        final Array<Ds1.Group> groups = ds1.groups;
        for (int i = 0, s = numGroups; i < s; i++) {
          MDC.put("ds1.group", i);
          Ds1.Group group = readGroup(ds1, in);
          groups.add(group);
        }
      } finally {
        MDC.remove("ds1.group");
      }
    }
  }

  static Ds1.Group readGroup(Ds1 ds1, ByteInput in) {
    Ds1.Group group = new Ds1.Group();
    group.bounds = new Rectangle(
        in.readSafe32u(),
        in.readSafe32u(),
        in.readSafe32u(),
        in.readSafe32u());
    group.unk = ds1.version < 13 ? 0 : in.read32();
    return group;
  }

  static void readPaths(Ds1 ds1, ByteInput in) {
    final int version = ds1.version;
    if (version >= 14) {
      try {
        final int numPaths = in.readSafe32u();
        final Array<Ds1.Path> paths = ds1.paths;
        for (int i = 0, s = numPaths; i < s; i++) {
          MDC.put("ds1.path", i);
          Ds1.Path path = readPath(ds1, in);
          paths.add(path);
        }
      } finally {
        MDC.remove("ds1.path");
      }
    }
  }

  static Ds1.Path readPath(Ds1 ds1, ByteInput in) {
    Ds1.Path path = new Ds1.Path();
    final int numNodes = in.readSafe32u();
    final Array<Ds1.Path.Node> nodes = path.nodes;
    path.position.set(in.readSafe32u(), in.readSafe32u());
    try {
      for (int i = 0, s = numNodes; i < s; i++) {
        MDC.put("ds1.path.node", i);
        Ds1.Path.Node node = readNode(ds1, in);
        nodes.add(node);
      }
    } finally {
      MDC.remove("ds1.path.node");
    }
    return path;
  }

  static Ds1.Path.Node readNode(Ds1 ds1, ByteInput in) {
    Ds1.Path.Node node = new Ds1.Path.Node();
    node.set(in.readSafe32u(), in.readSafe32u());
    node.action = ds1.version < 15 ? 1 : in.readSafe32u();
    return node;
  }

  static final class Cell {
    private Cell() {}

    enum Flag {
      // If not wall nor floor, then it is a roof.
      IsWall         (0),
      IsFloor        (1),
      LOS            (2), // code-generated; added on edges
      Enclosed       (3), // seems to delimit an enclosure inside another area, or trees
      Exit           (4), // arch or doorway or gateway in a wall
      Unk0x00000020  (5),
      LayerBelow     (6), // only floor
      LayerAbove     (7), // wall and floor
      SubIndex       (8, 16),
      FillLOS        (16), // all tiles will get wall collision
      Unwalkable     (17),
      WallLayer      (18, 20),
      OverlappedLayer(19),
      MainIndex      (20, 26),
      RevealHidden   (26), // looks like an upper wall brought to a layer in front
      Shadow         (27), // this layer is a shadow layer | Lectem's note: seems to be roof instead ? Or are shadow tiles interpreted as roof tiles ?
      Linkage        (28), // near wp, lvl links, paths // will never get hidden
      ObjectWall     (29), // wall tiles with props; may be block reverb / other sounds (crates, barrels, tables etc.)
      Unk0x40000000  (30),
      Hidden         (31),
      ;

      final int offset;
      final int mask;

      Flag(int offset) {
        this.offset = offset;
        mask = (1 << offset);
      }

      Flag(int startingBit, int endingBit) {
        this.offset = startingBit;
        mask = ((1 << offset) - 1) << offset;
      }
    }

    static int mainIndex(int flags) {
      return geti(flags, Flag.MainIndex);
    }

    static int subIndex(int flags) {
      return geti(flags, Flag.SubIndex);
    }

    static boolean get(int flags, Flag flag) {
      return (flags & flag.mask) != 0;
    }

    static int geti(int flags, Flag flag) {
      return (flags & flag.mask) >>> flag.offset;
    }

    static int set(int flags, Flag flag) {
      return flags | flag.mask;
    }

    static int set(int flags, Flag flag, int value) {
      flags &= ~flag.mask;
      return flags | ((value << flag.offset) & flag.mask);
    }

    static int unset(int flags, Flag flag) {
      return flags & ~flag.mask;
    }

    final int MAPTILE_FLAGS_NONE        = 0;
    final int MAPTILE_UNK_0x1           = 0x000001;
    final int MAPTILE_WALL_EXIT         = 0x000002; // warps, door exit,
    final int MAPTILE_TREES             = 0x000004; // Could also be delimiting an enclosure inside another area. Probably misnamed because not only trees ?
    final int MAPTILE_HIDDEN            = 0x000008; // used by warps & others // aka skip automap
    final int MAPTILE_UNK_0x10          = 0x000010;
    final int MAPTILE_HASPRESETUNITS    = 0x000020; // used for tile types 8-9; spawn doors & towers etc
    final int MAPTILE_UNWALKABLE        = 0x000040;
    final int MAPTILE_FILL_LOS          = 0x000080; // all subtiles will get wall collision;
    final int MAPTILE_FLOOR_LINKER_PATH = 0x000100; // the floor is near a wp or forms a path within level or to another level
    final int MAPTILE_UNK_0x200         = 0x000200; // Reveal hidden ?
    final int MAPTILE_PITCH_BLACK       = 0x000400; // the lighting changed, R&B=0
    final int MAPTILE_OBJECT_WALL       = 0x000800; // wall tile made of crops: barrels / crates / benches / tables (material flag 0x04)
    final int MAPTILE_UNK_0x001000      = 0x001000;
    final int MAPTILE_LOS               = 0x002000;
    final int MAPTILE_WALL_LAYER_BIT    = 14;
    final int MAPTILE_WALL_LAYER_MASK   = 0b111 << MAPTILE_WALL_LAYER_BIT; // 0x1C000: 3bits value indicating the wall layer + 1 (0 indicates no wall?)
  }
}
