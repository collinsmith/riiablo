package gdx.diablo.map2;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;

import gdx.diablo.Diablo;
import gdx.diablo.codec.excel.Levels;
import gdx.diablo.codec.excel.LvlPrest;
import gdx.diablo.codec.excel.LvlTypes;

public class Map {
  private static final String TAG = "Map";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_BUILD = DEBUG && true;

  public static final String TILES_PATH = "data/global/tiles/";

  public static final int OUTDOOR_GRID_X = 8;
  public static final int OUTDOOR_GRID_Y = 8;

  private static final int DIRECTION_NORTH = 0;
  private static final int DIRECTION_EAST  = 1;
  private static final int DIRECTION_SOUTH = 2;
  private static final int DIRECTION_WEST  = 3;

  private static final int[] ACT_DEF = new int[]{1, 301, 529, 797, 863};

  public static final int ID_MAP_ENTRY       = DT1.Tile.Index.create(Orientation.SPECIAL_TILE_11, 30, 0);
  public static final int ID_TOWN_ENTRY_1    = DT1.Tile.Index.create(Orientation.SPECIAL_TILE_10, 30, 0);
  public static final int ID_TOWN_ENTRY_2    = DT1.Tile.Index.create(Orientation.SPECIAL_TILE_10, 31, 0);
  public static final int ID_CORPSE_LOCATION = DT1.Tile.Index.create(Orientation.SPECIAL_TILE_10, 32, 0);
  public static final int ID_TP_LOCATION     = DT1.Tile.Index.create(Orientation.SPECIAL_TILE_10, 33, 0);
  static IntMap<String> ID_TO_NAME;
  static {
    ID_TO_NAME = new IntMap<>();
    ID_TO_NAME.put(ID_MAP_ENTRY,       "ID_MAP_ENTRY");
    ID_TO_NAME.put(ID_TOWN_ENTRY_1,    "ID_TOWN_ENTRY_1");
    ID_TO_NAME.put(ID_TOWN_ENTRY_2,    "ID_TOWN_ENTRY_2");
    ID_TO_NAME.put(ID_CORPSE_LOCATION, "ID_CORPSE_LOCATION");
    ID_TO_NAME.put(ID_TP_LOCATION,     "ID_TP_LOCATION");
  }

  public int width;
  public int height;

  public static Map build(int seed, int act, int diff) {
    MathUtils.random.setSeed(seed);

    Map map = new Map();

    int def = ACT_DEF[act];
    LvlPrest.Entry preset = Diablo.files.LvlPrest.get(def);
    Levels.Entry   level  = Diablo.files.Levels.get(preset.LevelId);
    if (DEBUG_BUILD) Gdx.app.debug(TAG, level.LevelName);

    int fileId[] = new int[6];
    int numFiles = getPresets(preset, fileId);
    int select = MathUtils.random(numFiles - 1);
    String fileName = preset.File[select];
    if (DEBUG_BUILD) Gdx.app.debug(TAG, "Select " + fileName);

    Zone zone = map.addZone(level, diff, preset, select);
    map.width  = zone.width;
    map.height = zone.height;
    Zone prev = zone;

    level = Diablo.files.Levels.get(2);
    zone = map.addZone(level, diff, 8, 8);
    zone.x = prev.width - zone.width;
    zone.y = -zone.height;

    Preset SB = Preset.of(Diablo.files.LvlPrest.get(4), 0);
    Preset EB = Preset.of(Diablo.files.LvlPrest.get(5), 0);
    Preset NB = Preset.of(Diablo.files.LvlPrest.get(6), 0);
    Preset WB = Preset.of(Diablo.files.LvlPrest.get(7), 0);
    Preset NWB = Preset.of(Diablo.files.LvlPrest.get(9), 0);
    Preset LRC = Preset.of(Diablo.files.LvlPrest.get(27), 0);
    Preset UR = Preset.of(Diablo.files.LvlPrest.get(26), 3);
    Preset URNB = Preset.of(Diablo.files.LvlPrest.get(26), 1);
    Preset SWB = Preset.of(Diablo.files.LvlPrest.get(8), 0);
    Preset LB = Preset.of(Diablo.files.LvlPrest.get(12), 0);
    Preset FILL[] = Preset.of(Diablo.files.LvlPrest.get(29));

    for (int x = 0; x < zone.gridsX; x++) {
      for (int y = 0; y < zone.gridsY; y++) {
        zone.presets[x][y] = FILL[MathUtils.random(FILL.length - 1)];
      }
    }
    for (int y = 0; y < zone.gridsY; y++) {
      zone.presets[0][y] = EB;
      zone.presets[zone.gridsX - 2][y] = UR;
      zone.presets[zone.gridsX - 1][y] = LRC;
    }
    for (int x = 1; x < zone.gridsX - 1; x++) {
      zone.presets[x][0] = NB;
    }
    zone.presets[0][0] = NWB;
    zone.presets[zone.gridsX - 2][0] = URNB;
    zone.presets[zone.gridsX - 1][0] = LRC;
    zone.presets[0][zone.gridsY - 1] = SWB;
    zone.presets[1][zone.gridsY - 1] = SB;
    zone.presets[2][zone.gridsY - 1] = SB;
    zone.presets[3][zone.gridsY - 1] = LB;

    zone.presets[6][zone.gridsY - 2] = Preset.of(Diablo.files.LvlPrest.get(47), 1);

    return map;
  }

  public GridPoint2 find(int id) {
    return zones.first().presets[0][0].ds1.find(id);
  }

  public GridPoint2 find(int orientation, int mainIndex, int subIndex) {
    int id = DT1.Tile.Index.create(orientation, mainIndex, subIndex);
    return find(id);
  }

  public String getSpecialName(int id) {
    return ID_TO_NAME.get(id, "null");
  }

  private static int getPresets(LvlPrest.Entry preset, int[] fileIds) {
    int numFiles = 0;
    Arrays.fill(fileIds, -1);
    for (int i = 0; i < preset.File.length; i++) {
      if (preset.File[i].charAt(0) != '0') {
        fileIds[numFiles++] = i;
      }
    }

    return numFiles;
  }

  Array<Zone> zones = new Array<>();

  public void load() {
    for (Zone zone : zones) {
      for (int x = 0; x < zone.gridsX; x++) {
        for (int y = 0; y < zone.gridsY; y++) {
          Map.Preset preset = zone.presets[x][y];
          if (preset == null) continue;

          Diablo.assets.load(TILES_PATH + preset.ds1Name, DS1.class);

          int DT1Mask = preset.preset.Dt1Mask;
          for (int i = 0; i < Integer.SIZE; i++) {
            if ((DT1Mask & (1 << i)) != 0) {
              String dt1 = TILES_PATH + zone.type.File[i];
              Diablo.assets.load(dt1, DT1.class);
            }
          }
        }
      }
    }

    Diablo.assets.finishLoading();
    for (Zone zone : zones) {
      zone.load();
    }
  }

  Zone addZone(Levels.Entry level, int diff, LvlPrest.Entry preset, int ds1) {
    assert preset.LevelId != 0 : "presets should have an assigned level id";
    Zone zone = addZone(level, diff, level.SizeX[diff], level.SizeY[diff]);
    zone.presets[0][0] = Preset.of(preset, ds1);
    return zone;
  }

  Zone addZone(Levels.Entry level, int diff, int gridSizeX, int gridSizeY) {
    Zone zone = new Zone(gridSizeX, gridSizeY);
    zone.level   = level;
    zone.type    = Diablo.files.LvlTypes.get(level.LevelType);
    zone.width   = level.SizeX[diff];
    zone.height  = level.SizeY[diff];
    zone.gridsX  = zone.width  / zone.gridSizeX;
    zone.gridsY  = zone.height / zone.gridSizeY;
    zone.presets = new Preset[zone.gridsX][zone.gridsY];
    zone.flags   = new byte[zone.width * DT1.Tile.SUBTILE_SIZE][zone.height * DT1.Tile.SUBTILE_SIZE];
    zones.add(zone);
    return zone;
  }

  Zone getZone(int x, int y) {
    for (Zone zone : zones) {
      if (zone.contains(x, y)) {
        return zone;
      }
    }

    return null;
  }

  static class Zone {
    int x, y;
    int width, height;
    int gridSizeX, gridSizeY;
    int gridsX, gridsY;
    Levels.Entry   level;
    LvlTypes.Entry type;
    Preset         presets[][];
    Tile           tiles[][][];
    byte           flags[][];

    public Zone(int gridSizeX, int gridSizeY) {
      this.gridSizeX = gridSizeX;
      this.gridSizeY = gridSizeY;
    }

    public boolean contains(int x, int y) {
      x -= this.x;
      y -= this.y;
      return 0 <= x && x < width
          && 0 <= y && y < height;
    }

    public Tile get(int layer, int x, int y) {
      return tiles[layer] == null ? null : tiles[layer][x - this.x][y - this.y];
    }

    public int getLocalX(int x) {
      return x - this.x;
    }

    public int getLocalY(int y) {
      return y - this.y;
    }

    void load() {
      Preconditions.checkState(tiles == null, "tiles have already been loaded");
      tiles = new Tile[DS1.MAX_LAYERS][][];
      for (int x = 0, gridX = 0, gridY = 0; x < gridsX; x++, gridX += gridSizeX, gridY = 0) {
        for (int y = 0; y < gridsY; y++, gridY += gridSizeY) {
          Preset preset = presets[x][y];
          DS1 ds1 = Diablo.assets.get(TILES_PATH + preset.ds1Name);
          preset.load(this, ds1, gridX, gridY);
        }
      }
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this);
      if (level != null) {
        builder
            .append("name", level.LevelName)
            .append("id", level.Id);
      }

      return builder
          .append("x", x)
          .append("y", y)
          .append("width", width)
          .append("height", height)
          .build();
    }
  }

  static class Tile {
    DT1.Tile tile;
    DS1.Cell cell;
    //byte[] flags;

    public static Tile of(DS1.Cell cell) {
      return of(Diablo.dt1s.get(cell), cell);
    }

    public static Tile of(DT1.Tile tile, DS1.Cell cell) {
      Tile t  = new Tile();
      t.tile  = tile;
      t.cell  = cell;
      //t.flags = new byte[DT1.Tile.NUM_SUBTILES];
      return t;
    }
  }

  static class Preset {
    LvlPrest.Entry preset;
    String         ds1Name;
    DS1            ds1;

    public static Preset of(LvlPrest.Entry preset, int ds1) {
      Preset p  = new Preset();
      p.preset  = preset;
      p.ds1Name = preset.File[ds1];
      return p;
    }

    public static Preset of(LvlPrest.Entry preset, String ds1) {
      Preset p  = new Preset();
      p.preset  = preset;
      p.ds1Name = ds1;
      return p;
    }

    public static Preset[] of(LvlPrest.Entry preset) {
      Preset[] presets = new Preset[preset.Files];
      for (int i = 0; i < preset.Files; i++) {
        presets[i] = Preset.of(preset, i);
      }

      return presets;
    }

    public void load(Zone zone, DS1 ds1, int x, int y) {
      assert x + ds1.width <= zone.width  : "x=" + x + "; ds1.width="  + ds1.width  + "; zone.width="  + zone.width;
      assert y + ds1.height <= zone.width : "y=" + y + "; ds1.height=" + ds1.height + "; zone.height=" + zone.height;

      this.ds1 = ds1;
      loadFloors(zone, DS1.FLOOR_OFFSET, x, y, ds1);
      loadWalls(zone, DS1.WALL_OFFSET, x, y, ds1);
      //loadLayer(zone, FLOOR_OFFSET, x, y, ds1.floors, ds1.numFloors, ds1.floorLine);
      //loadLayer(zone, SHADOW_OFFSET, x, y, ds1.shadows, ds1.numShadows, ds1.shadowLine);
      //loadLayer(zone, WALL_OFFSET,   x, y, ds1.walls,   ds1.numWalls,   ds1.wallLine);
    }

    void loadLayer(Zone zone, int layer, int x, int y, DS1.Cell[] cells, int numLayers, int cellsLine) {
      final int X_OFFS = x, Y_OFFS = y;
      for (int i = 0, offsetX, offsetY; i < numLayers; i++, layer++) {
        offsetX = X_OFFS;
        offsetY = Y_OFFS;
        if (zone.tiles[layer] == null) zone.tiles[layer] = new Tile[zone.width][zone.height];
        for (y = 0; y < zone.height; y++, offsetY++, offsetX = X_OFFS) {
          //int ptr = i + (y * cellsLine);
          for (x = 0; x < zone.width; x++, offsetX++/*, ptr += numLayers*/) {
            int ptr = (y * cellsLine) + (x * numLayers);
            DS1.Cell cell = cells[ptr];
            zone.tiles[layer][offsetX][offsetY] = Tile.of(cell);
          }
        }
      }
    }

    void loadFloors(Zone zone, int layer, int x, int y, DS1 ds1) {
      final int X_OFFS = x, Y_OFFS = y;
      for (int i = 0, offsetX, offsetY; i < ds1.numFloors; i++, layer++) {
        offsetX = X_OFFS;
        offsetY = Y_OFFS;
        if (zone.tiles[layer] == null) zone.tiles[layer] = new Tile[zone.width][zone.height];
        for (y = 0; y < ds1.height; y++, offsetY++, offsetX = X_OFFS) {
          for (x = 0; x < ds1.width; x++, offsetX++) {
            int ptr = i + (y * ds1.floorLine) + (x * ds1.numFloors);
            DS1.Cell cell = ds1.floors[ptr];

            if ((cell.value & 0x00020000) != 0) {
              int offsetX2 = offsetX * DT1.Tile.SUBTILE_SIZE;
              int offsetY2 = offsetY * DT1.Tile.SUBTILE_SIZE;
              for (int y2 = 0; y2 < DT1.Tile.SUBTILE_SIZE; y2++) {
                for (int x2 = 0; x2 < DT1.Tile.SUBTILE_SIZE; x2++) {
                  zone.flags[offsetX2 + x2][offsetY2 + y2] |= 1;
                }
              }
            }

            if (ds1.numFloors == 1) {
              if ((ds1.floors[0].value & 0xFF) == 0) {
                int offsetX2 = offsetX * DT1.Tile.SUBTILE_SIZE;
                int offsetY2 = offsetY * DT1.Tile.SUBTILE_SIZE;
                for (int y2 = 0; y2 < DT1.Tile.SUBTILE_SIZE; y2++) {
                  for (int x2 = 0; x2 < DT1.Tile.SUBTILE_SIZE; x2++) {
                    zone.flags[offsetX2 + x2][offsetY2 + y2] |= 1;
                  }
                }
              }
            } else if (ds1.numFloors == 2) {
              if ((ds1.floors[0].value & 0xFF) == 0 && (ds1.floors[1].value & 0xFF) == 0) {
                int offsetX2 = offsetX * DT1.Tile.SUBTILE_SIZE;
                int offsetY2 = offsetY * DT1.Tile.SUBTILE_SIZE;
                for (int y2 = 0; y2 < DT1.Tile.SUBTILE_SIZE; y2++) {
                  for (int x2 = 0; x2 < DT1.Tile.SUBTILE_SIZE; x2++) {
                    zone.flags[offsetX2 + x2][offsetY2 + y2] |= 1;
                  }
                }
              }
            }

            if ((cell.value & 0x80000000) != 0) {
              continue;
            }

            Tile tile = zone.tiles[layer][offsetX][offsetY] = Tile.of(cell);
            copyFlags(zone.flags, offsetX, offsetY, tile.tile);
          }
        }
      }
    }

    void loadWalls(Zone zone, int layer, int x, int y, DS1 ds1) {
      final int X_OFFS = x, Y_OFFS = y;
      for (int i = 0, offsetX, offsetY; i < ds1.numWalls; i++, layer++) {
        offsetX = X_OFFS;
        offsetY = Y_OFFS;
        if (zone.tiles[layer] == null) zone.tiles[layer] = new Tile[zone.width][zone.height];
        for (y = 0; y < ds1.height; y++, offsetY++, offsetX = X_OFFS) {
          for (x = 0; x < ds1.width; x++, offsetX++) {
            int ptr = i + (y * ds1.wallLine) + (x * ds1.numWalls);
            DS1.Cell cell = ds1.walls[ptr];

            if ((cell.value & DS1.Cell.MASK_UNWALKABLE) != 0) {
              int offsetX2 = offsetX * DT1.Tile.SUBTILE_SIZE;
              int offsetY2 = offsetY * DT1.Tile.SUBTILE_SIZE;
              for (int y2 = 0; y2 < DT1.Tile.SUBTILE_SIZE; y2++) {
                for (int x2 = 0; x2 < DT1.Tile.SUBTILE_SIZE; x2++) {
                  zone.flags[offsetX2 + x2][offsetY2 + y2] |= 1;
                }
              }
            }

            if ((cell.value & 0x80000000) != 0) {
              // This seems like all the special tiles, null usually means marker tile (start pos),
              // non null usually means stuff like side of river, used for ?weather? ?rain drops?
              if (!Orientation.isSpecial(cell.orientation)) {
                //DT1.Tile tile = Diablo.dt1s.get(cell);
                //System.out.println(x + ", " + y + " " + tile);
                continue;
              }

              zone.tiles[layer][offsetX][offsetY] = Tile.of(cell);
              continue;
            }

            if (cell.orientation == Orientation.FLOOR) {
              continue;
            }

            Tile tile = zone.tiles[layer][offsetX][offsetY] = Tile.of(cell);
            copyFlags(zone.flags, offsetX, offsetY, tile.tile);

            // Special case, because LEFT_NORTH_CORNER_WALL don't seem to exist, but they contain
            // collision data for RIGHT_NORTH_CORNER_WALL, ORing the data just in case some
            // RIGHT_NORTH_CORNER_WALL actually does anything
            if (cell.orientation == Orientation.RIGHT_NORTH_CORNER_WALL) {
              DT1.Tile leftSide = Diablo.dt1s.get(Orientation.LEFT_NORTH_CORNER_WALL, cell.mainIndex, cell.subIndex);
              copyFlags(zone.flags, offsetX, offsetY, leftSide);
            }
          }
        }
      }
    }

    static void copyFlags(byte[][] flags, int tx, int ty, DT1.Tile tile) {
      int offsetX = tx * DT1.Tile.SUBTILE_SIZE;
      int offsetY = ty * DT1.Tile.SUBTILE_SIZE;
      // Note: walkable flags are stored inverted y-axis, this corrects it
      for (int y = DT1.Tile.SUBTILE_SIZE - 1, t = 0; y >= 0; y--) {
        for (int x = 0; x < DT1.Tile.SUBTILE_SIZE; x++, t++) {
          flags[offsetX + x][offsetY + y] |= tile.tileFlags[t];
        }
      }
    }
  }
}
