package gdx.diablo.map;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

import gdx.diablo.Diablo;
import gdx.diablo.codec.excel.Levels;
import gdx.diablo.codec.excel.LvlPrest;
import gdx.diablo.codec.excel.LvlTypes;
import gdx.diablo.entity.Entity;

public class Map implements Disposable {
  private static final String TAG = "Map";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_BUILD = DEBUG && true;
  private static final boolean DEBUG_ZONES = DEBUG && true;

  public static final String TILES_PATH = "data/global/tiles/";

  public static final int MAX_WALLS   = DS1.MAX_WALLS;
  public static final int MAX_FLOORS  = DS1.MAX_FLOORS;
  public static final int MAX_SHADOWS = DS1.MAX_SHADOWS;
  public static final int MAX_TAGS    = DS1.MAX_TAGS;

  public static final int FLOOR_OFFSET  = 0;
  public static final int SHADOW_OFFSET = FLOOR_OFFSET  + MAX_FLOORS;
  public static final int WALL_OFFSET   = SHADOW_OFFSET + MAX_SHADOWS;
  public static final int TAG_OFFSET    = WALL_OFFSET   + MAX_WALLS;
  public static final int MAX_LAYERS    = TAG_OFFSET    + MAX_TAGS;

  static final int[] ACT_DEF = new int[]{1, 301, 529, 797, 863, 1091};

  public static final class ID {
    public static final int VIS_0_00        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 0, 0);
    public static final int VIS_0_01        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 0, 1);
    public static final int VIS_0_02        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 0, 17);
    public static final int VIS_0_03        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 0, 21);
    public static final int VIS_0_04        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 0, 23);
    public static final int VIS_0_05        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 0, 32);
    public static final int VIS_0_06        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 0, 0);
    public static final int VIS_0_07        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 0, 1);
    public static final int VIS_0_08        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 0, 2);
    public static final int VIS_0_09        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 0, 3);
    public static final int VIS_0_10        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 0, 4);
    public static final int VIS_1_11        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 0);
    public static final int VIS_1_12        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 1);
    public static final int VIS_1_13        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 17);
    public static final int VIS_1_14        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 18);
    public static final int VIS_1_15        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 21);
    public static final int VIS_1_16        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 22);
    public static final int VIS_1_17        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 1, 32);
    public static final int VIS_1_18        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 1, 0);
    public static final int VIS_1_19        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 1, 1);
    public static final int VIS_2_20        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 2, 0);
    public static final int VIS_2_21        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 2, 1);
    public static final int VIS_2_22        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 2, 2);
    public static final int VIS_2_23        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 2, 21);
    public static final int VIS_2_24        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 2, 23);
    public static final int VIS_2_25        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 2, 25);
    public static final int VIS_2_26        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 2, 0);
    public static final int VIS_2_27        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 2, 1);
    public static final int VIS_2_28        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 2, 2);
    public static final int VIS_2_29        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 2, 5);
    public static final int VIS_3_30        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 3, 0);
    public static final int VIS_3_31        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 3, 1);
    public static final int VIS_3_32        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 3, 5);
    public static final int VIS_3_33        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 3, 22);
    public static final int VIS_3_34        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 3, 24);
    public static final int VIS_3_35        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 3, 0);
    public static final int VIS_3_36        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 3, 1);
    public static final int VIS_4_37        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 4, 0);
    public static final int VIS_4_38        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 4, 4);
    public static final int VIS_4_39        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 4, 47);
    public static final int VIS_4_40        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 4, 0);
    public static final int VIS_4_41        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 4, 1);
    public static final int VIS_5_81        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 5, 0);
    public static final int VIS_5_42        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 5, 21);
    public static final int VIS_5_43        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 5, 0);
    public static final int VIS_6_44        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 6, 0);
    public static final int VIS_6_45        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 6, 24);
    public static final int VIS_6_82        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 6, 0);
    public static final int VIS_7_46        = DT1.Tile.Index.create(Orientation.SPECIAL_10, 7, 0);
    public static final int VIS_7_83        = DT1.Tile.Index.create(Orientation.SPECIAL_11, 7, 0);
    public static final int _47             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 2);
    public static final int _48             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 7);
    public static final int _49             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 8);
    public static final int _50             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 12);
    public static final int _51             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 14);
    public static final int _52             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 16);
    public static final int _53             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 22);
    public static final int _54             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 23);
    public static final int _55             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 31);
    public static final int _56             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 40);
    public static final int _57             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 41);
    public static final int _58             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 42);
    public static final int _59             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 8, 46);
    public static final int _60             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 9, 23);
    public static final int _61             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 9, 40);
    public static final int _62             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 9, 46);
    public static final int _63             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 10, 23);
    public static final int _64             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 12, 13);
    public static final int _65             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 12, 14);
    public static final int _66             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 12, 23);
    public static final int _67             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 12, 24);
    public static final int _68             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 12, 41);
    public static final int _69             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 13, 13);
    public static final int _70             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 16, 25);
    public static final int _71             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 16, 40);
    public static final int _72             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 16, 41);
    public static final int _73             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 16, 42);
    public static final int _74             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 20, 23);
    public static final int _80             = DT1.Tile.Index.create(Orientation.SPECIAL_10, 34, 0);

    public static final int MAP_ENTRY       = DT1.Tile.Index.create(Orientation.SPECIAL_10, 30, 11);
    public static final int TOWN_ENTRY_1    = DT1.Tile.Index.create(Orientation.SPECIAL_10, 30, 0);
    public static final int TOWN_ENTRY_2    = DT1.Tile.Index.create(Orientation.SPECIAL_10, 31, 0);
    public static final int CORPSE_LOCATION = DT1.Tile.Index.create(Orientation.SPECIAL_10, 32, 0);
    public static final int TP_LOCATION     = DT1.Tile.Index.create(Orientation.SPECIAL_10, 33, 0);

    static IntSet POPPADS;
    static {
      POPPADS = new IntSet();
      POPPADS.addAll(
          _47, _48, _49,
          _50, _51, _52, _53, _54, _55, _56, _57, _58, _59,
          _60, _61, _62, _63, _64, _66, _66, _67, _68, _69,
          _70, _71, _72, _73, _74);
    }

    static IntMap<String> ID_TO_NAME;
    static {
      ID_TO_NAME = new IntMap<>();
      ID_TO_NAME.put(MAP_ENTRY,       "ID_MAP_ENTRY");
      ID_TO_NAME.put(TOWN_ENTRY_1,    "ID_TOWN_ENTRY_1");
      ID_TO_NAME.put(TOWN_ENTRY_2,    "ID_TOWN_ENTRY_2");
      ID_TO_NAME.put(CORPSE_LOCATION, "ID_CORPSE_LOCATION");
      ID_TO_NAME.put(TP_LOCATION,     "ID_TP_LOCATION");
      ID_TO_NAME.put(VIS_0_00,        "ID_VIS_0_00");
      ID_TO_NAME.put(VIS_0_01,        "ID_VIS_0_01");
      ID_TO_NAME.put(VIS_0_02,        "ID_VIS_0_02");
      ID_TO_NAME.put(VIS_0_03,        "ID_VIS_0_03");
      ID_TO_NAME.put(VIS_0_04,        "ID_VIS_0_04");
      ID_TO_NAME.put(VIS_0_05,        "ID_VIS_0_05");
      ID_TO_NAME.put(VIS_0_06,        "ID_VIS_0_06");
      ID_TO_NAME.put(VIS_0_07,        "ID_VIS_0_07");
      ID_TO_NAME.put(VIS_0_08,        "ID_VIS_0_08");
      ID_TO_NAME.put(VIS_0_09,        "ID_VIS_0_09");
      ID_TO_NAME.put(VIS_0_10,        "ID_VIS_0_10");
      ID_TO_NAME.put(VIS_1_11,        "ID_VIS_1_11");
      ID_TO_NAME.put(VIS_1_12,        "ID_VIS_1_12");
      ID_TO_NAME.put(VIS_1_13,        "ID_VIS_1_13");
      ID_TO_NAME.put(VIS_1_14,        "ID_VIS_1_14");
      ID_TO_NAME.put(VIS_1_15,        "ID_VIS_1_15");
      ID_TO_NAME.put(VIS_1_16,        "ID_VIS_1_16");
      ID_TO_NAME.put(VIS_1_17,        "ID_VIS_1_17");
      ID_TO_NAME.put(VIS_1_18,        "ID_VIS_1_18");
      ID_TO_NAME.put(VIS_1_19,        "ID_VIS_1_19");
      ID_TO_NAME.put(VIS_2_20,        "ID_VIS_2_20");
      ID_TO_NAME.put(VIS_2_21,        "ID_VIS_2_21");
      ID_TO_NAME.put(VIS_2_22,        "ID_VIS_2_22");
      ID_TO_NAME.put(VIS_2_23,        "ID_VIS_2_23");
      ID_TO_NAME.put(VIS_2_24,        "ID_VIS_2_24");
      ID_TO_NAME.put(VIS_2_25,        "ID_VIS_2_25");
      ID_TO_NAME.put(VIS_2_26,        "ID_VIS_2_26");
      ID_TO_NAME.put(VIS_2_27,        "ID_VIS_2_27");
      ID_TO_NAME.put(VIS_2_28,        "ID_VIS_2_28");
      ID_TO_NAME.put(VIS_2_29,        "ID_VIS_2_29");
      ID_TO_NAME.put(VIS_3_30,        "ID_VIS_3_30");
      ID_TO_NAME.put(VIS_3_31,        "ID_VIS_3_31");
      ID_TO_NAME.put(VIS_3_32,        "ID_VIS_3_32");
      ID_TO_NAME.put(VIS_3_33,        "ID_VIS_3_33");
      ID_TO_NAME.put(VIS_3_34,        "ID_VIS_3_34");
      ID_TO_NAME.put(VIS_3_35,        "ID_VIS_3_35");
      ID_TO_NAME.put(VIS_3_36,        "ID_VIS_3_36");
      ID_TO_NAME.put(VIS_4_37,        "ID_VIS_4_37");
      ID_TO_NAME.put(VIS_4_38,        "ID_VIS_4_38");
      ID_TO_NAME.put(VIS_4_39,        "ID_VIS_4_39");
      ID_TO_NAME.put(VIS_4_40,        "ID_VIS_4_40");
      ID_TO_NAME.put(VIS_4_41,        "ID_VIS_4_41");
      ID_TO_NAME.put(VIS_5_81,        "ID_VIS_5_81");
      ID_TO_NAME.put(VIS_5_42,        "ID_VIS_5_42");
      ID_TO_NAME.put(VIS_5_43,        "ID_VIS_5_43");
      ID_TO_NAME.put(VIS_6_44,        "ID_VIS_6_44");
      ID_TO_NAME.put(VIS_6_45,        "ID_VIS_6_45");
      ID_TO_NAME.put(VIS_6_82,        "ID_VIS_6_82");
      ID_TO_NAME.put(VIS_7_46,        "ID_VIS_7_46");
      ID_TO_NAME.put(VIS_7_83,        "ID_VIS_7_83");
      ID_TO_NAME.put(_47,             "ID_47");
      ID_TO_NAME.put(_48,             "ID_48");
      ID_TO_NAME.put(_49,             "ID_49_AREA_W2_W3");
      ID_TO_NAME.put(_50,             "ID_50");
      ID_TO_NAME.put(_51,             "ID_51_AREA_W2");
      ID_TO_NAME.put(_52,             "ID_52");
      ID_TO_NAME.put(_53,             "ID_53");
      ID_TO_NAME.put(_54,             "ID_54");
      ID_TO_NAME.put(_55,             "ID_55");
      ID_TO_NAME.put(_56,             "ID_56");
      ID_TO_NAME.put(_57,             "ID_57");
      ID_TO_NAME.put(_58,             "ID_58");
      ID_TO_NAME.put(_59,             "ID_59");
      ID_TO_NAME.put(_60,             "ID_60");
      ID_TO_NAME.put(_61,             "ID_61");
      ID_TO_NAME.put(_62,             "ID_62");
      ID_TO_NAME.put(_63,             "ID_63");
      ID_TO_NAME.put(_64,             "ID_64");
      ID_TO_NAME.put(_65,             "ID_65_AREA_W2");
      ID_TO_NAME.put(_66,             "ID_66");
      ID_TO_NAME.put(_67,             "ID_67");
      ID_TO_NAME.put(_68,             "ID_68");
      ID_TO_NAME.put(_69,             "ID_69_AREA_W2_W4");
      ID_TO_NAME.put(_70,             "ID_70");
      ID_TO_NAME.put(_71,             "ID_71");
      ID_TO_NAME.put(_72,             "ID_72");
      ID_TO_NAME.put(_73,             "ID_73");
      ID_TO_NAME.put(_74,             "ID_74");
      ID_TO_NAME.put(_80,             "ID_80");
    }

    public static String getName(int id) {
      return ID_TO_NAME.get(id, "null");
    }

    public static int getGroup(DS1.Cell cell) {
      if (cell == null) return 0;
      switch (cell.mainIndex) {
        case 8: case 9: case 10:
          return 1;
        case 12: case 13:
          return 2;
        case 16:
          return 3;
        case 20:
          return 4;
        default:
          return 0;
      }
    }

    public static Color getColor(DS1.Cell cell) {
      if (cell == null) return Color.WHITE;
      switch (cell.mainIndex) {
        case 8:  return Color.RED;
        case 9:  return Color.ORANGE;
        case 10: return Color.YELLOW;
        case 12: return Color.GREEN;
        case 13: return Color.BLUE;
        case 16: return Color.TEAL;
        case 20: return Color.VIOLET;
        default: return Color.WHITE;
      }
    }
  }

  // TODO: maybe replace with R-tree? // https://en.wikipedia.org/wiki/R-tree
  Array<Zone> zones = new Array<>();
  IntMap<DT1s> dt1s;

  private Map() {}

  public static Map build(MapLoader.MapParameters params) {
    return build(params.seed, params.act, params.diff);
  }

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
    Zone prev = zone;

    level = Diablo.files.Levels.get(2);
    zone = map.addZone(level, diff, 8, 8);
    zone.setPosition(prev.width - zone.width, -zone.height);
    if (DEBUG_BUILD) Gdx.app.debug(TAG, "Moved " + zone.level.LevelName + " " + zone);

    Preset SB   = Preset.of(Diablo.files.LvlPrest.get(4),  0);
    Preset EB   = Preset.of(Diablo.files.LvlPrest.get(5),  0);
    Preset NB   = Preset.of(Diablo.files.LvlPrest.get(6),  0);
    Preset WB   = Preset.of(Diablo.files.LvlPrest.get(7),  0);
    Preset NWB  = Preset.of(Diablo.files.LvlPrest.get(9),  0);
    Preset LRC  = Preset.of(Diablo.files.LvlPrest.get(27), 0);
    Preset UR   = Preset.of(Diablo.files.LvlPrest.get(26), 3);
    Preset URNB = Preset.of(Diablo.files.LvlPrest.get(26), 1);
    Preset SWB  = Preset.of(Diablo.files.LvlPrest.get(8),  0);
    Preset LB   = Preset.of(Diablo.files.LvlPrest.get(12), 0);

    //Preset FILL[] = Preset.of(Diablo.files.LvlPrest.get(29));
    /*for (int x = 0; x < zone.gridsX; x++) {
      for (int y = 0; y < zone.gridsY; y++) {
        zone.presets[x][y] = FILL[MathUtils.random(FILL.length - 1)];
      }
    }*/

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

    zone.generator = new Zone.Generator() {
      @Override
      public void generate(Zone zone, DT1s dt1s, int tx, int ty) {
        if (zone.tiles[FLOOR_OFFSET] == null) zone.tiles[FLOOR_OFFSET] = new Tile[zone.tilesX][zone.tilesY];
        final int startY = ty;
        for (int x = 0; x < zone.gridSizeX; x++, tx++, ty = startY) {
          for (int y = 0; y < zone.gridSizeY; y++, ty++) {
            zone.tiles[FLOOR_OFFSET][tx][ty] = Tile.of(dt1s.get(0));
          }
        }
      }
    };

    return map;
  }

  private static int getPresets(LvlPrest.Entry preset, int[] fileIds) {
    int numFiles = 0;
    for (int i = 0; i < preset.File.length; i++) {
      if (preset.File[i].charAt(0) != '0') {
        fileIds[numFiles++] = i;
      }
    }

    return numFiles;
  }

  public GridPoint2 find(int id) {
    GridPoint2 origin = zones.first().presets[0][0].ds1.find(id);
    if (origin == null) return null;
    origin = origin.cpy();
    origin.x *= DT1.Tile.SUBTILE_SIZE;
    origin.y *= DT1.Tile.SUBTILE_SIZE;
    return origin.add(DT1.Tile.SUBTILE_CENTER);
  }

  public Array<AssetDescriptor> getDependencies() {
    Array<AssetDescriptor> dependencies = new Array<>();
    for (Zone zone : zones) {
      for (int x = 0; x < zone.gridsX; x++) {
        for (int y = 0; y < zone.gridsY; y++) {
          Map.Preset preset = zone.presets[x][y];
          if (preset == null) continue;
          dependencies.addAll(preset.getDependencies(zone.type));
        }
      }
    }

    return dependencies;
  }

  public void buildDT1s() {
    Preconditions.checkState(dt1s == null, "dt1s have already been loaded");
    IntMap<ObjectSet<AssetDescriptor<DT1>>> typeDependencies = new IntMap<>();
    for (Zone zone : zones) {
      int type = zone.level.LevelType;
      ObjectSet<AssetDescriptor<DT1>> dependencies = typeDependencies.get(type);
      if (dependencies == null) typeDependencies.put(type, dependencies = new ObjectSet<>());
      for (int x = 0; x < zone.gridsX; x++) {
        for (int y = 0; y < zone.gridsY; y++) {
          Map.Preset preset = zone.presets[x][y];
          if (preset == null) continue;
          int DT1Mask = preset.preset.Dt1Mask;
          for (int i = 0; i < Integer.SIZE; i++) {
            if ((DT1Mask & (1 << i)) != 0) {
              dependencies.add(new AssetDescriptor<>(TILES_PATH + zone.type.File[i], DT1.class));
            }
          }

        }
      }
    }

    dt1s = new IntMap<>();
    for (IntMap.Entry<ObjectSet<AssetDescriptor<DT1>>> entry : typeDependencies.entries()) {
      int type = entry.key;
      DT1s dt1s = this.dt1s.get(type);
      if (dt1s == null) this.dt1s.put(type, dt1s = new DT1s());
      for (AssetDescriptor<DT1> dt1 : entry.value) {
        dt1s.add(Diablo.assets.get(dt1));
      }
    }

    for (Zone zone : zones) {
      int type = zone.level.LevelType;
      zone.load(dt1s.get(type));
    }
  }

  public void load() {
    Preconditions.checkState(dt1s == null, "dt1s have already been loaded");

    IntMap<ObjectSet<AssetDescriptor<DT1>>> typeDependencies = new IntMap<>();
    for (Zone zone : zones) {
      int type = zone.level.LevelType;
      ObjectSet<AssetDescriptor<DT1>> dependencies = typeDependencies.get(type);
      if (dependencies == null) typeDependencies.put(type, dependencies = new ObjectSet<>());
      for (int x = 0; x < zone.gridsX; x++) {
        for (int y = 0; y < zone.gridsY; y++) {
          Map.Preset preset = zone.presets[x][y];
          if (preset == null) continue;
          preset.load(Diablo.assets, zone.type, dependencies);
          /*Diablo.assets.load(TILES_PATH + preset.ds1Path, DS1.class);
          int DT1Mask = preset.preset.Dt1Mask;
          for (int i = 0; i < Integer.SIZE; i++) {
            if ((DT1Mask & (1 << i)) != 0) {
              String dt1 = TILES_PATH + zone.type.File[i];
              Diablo.assets.load(dt1, DT1.class); // DT1Loader.DT1LoaderParameters.newInstance(dt1s)
              loaded.add(dt1);
            }
          }*/
        }
      }
    }

    Diablo.assets.finishLoading();

    dt1s = new IntMap<>();
    for (IntMap.Entry<ObjectSet<AssetDescriptor<DT1>>> entry : typeDependencies.entries()) {
      int type = entry.key;
      DT1s dt1s = this.dt1s.get(type);
      if (dt1s == null) this.dt1s.put(type, dt1s = new DT1s());
      for (AssetDescriptor<DT1> dt1 : entry.value) {
        dt1s.add(Diablo.assets.get(dt1));
      }
    }

    for (Zone zone : zones) {
      int type = zone.level.LevelType;
      zone.load(dt1s.get(type));
    }
  }

  @Override
  public void dispose() {
    for (Zone zone : zones) {
      for (int x = 0; x < zone.gridsX; x++) {
        for (int y = 0; y < zone.gridsY; y++) {
          Preset preset = zone.presets[x][y];
          if (preset == null) continue;
          preset.dispose(Diablo.assets);
          /*Diablo.assets.unload(TILES_PATH + preset.ds1Path);
          Gdx.app.debug(TAG, "unloading " + TILES_PATH + preset.ds1Path);

          for (DT1 dt1 : preset.dt1s.dt1s) {
            if (Diablo.assets.isLoaded(dt1.fileName)) {
              Diablo.assets.unload(dt1.fileName);
              Gdx.app.debug(TAG, "unloading " + dt1.fileName);
            }
          }*/
        }
      }
    }
  }

  Zone getZone(int x, int y) {
    for (Zone zone : zones) {
      if (zone.contains(x, y)) {
        return zone;
      }
    }

    return null;
  }

  Zone addZone(Levels.Entry level, int diff, LvlPrest.Entry preset, int ds1) {
    assert preset.LevelId != 0 : "presets should have an assigned level id";
    Zone zone = addZone(level, diff, level.SizeX[diff], level.SizeY[diff]);
    zone.presets[0][0] = Preset.of(preset, ds1);
    return zone;
  }

  Zone addZone(Levels.Entry level, int diff, int gridSizeX, int gridSizeY) {
    Zone zone = new Zone(level, diff, gridSizeX, gridSizeY);
    if (DEBUG_ZONES) Gdx.app.debug(TAG, zone.toString());
    zones.add(zone);
    return zone;
  }

  Zone addZone(Levels.Entry level, int gridSizeX, int gridSizeY, int gridsX, int gridsY) {
    Zone zone = new Zone(level, gridSizeX, gridSizeY, gridsX, gridsY);
    if (DEBUG_ZONES) Gdx.app.debug(TAG, zone.toString());
    zones.add(zone);
    return zone;
  }

  static class Zone {
    static final Array<Entity> EMPTY_ARRAY = new Array<>(0);

    int x, y;
    int width, height;
    int gridSizeX, gridSizeY;
    int gridsX, gridsY;
    int tx, ty;
    int tilesX, tilesY;

    Levels.Entry   level;
    LvlTypes.Entry type;
    Preset         presets[][];
    Tile           tiles[][][];
    byte           flags[][];
    Array<Entity>  entities;

    Generator generator;

    /**
     * Constructs a zone using sizing info from levels.txt
     */
    Zone(Levels.Entry level, int diff, int gridSizeX, int gridSizeY) {
      this.level     = level;
      this.type      = Diablo.files.LvlTypes.get(level.LevelType);
      this.gridSizeX = gridSizeX;
      this.gridSizeY = gridSizeY;

      tilesX   = level.SizeX[diff];
      tilesY   = level.SizeY[diff];
      width    = tilesX * DT1.Tile.SUBTILE_SIZE;
      height   = tilesY * DT1.Tile.SUBTILE_SIZE;
      gridsX   = tilesX / gridSizeX;
      gridsY   = tilesY / gridSizeY;
      presets  = new Preset[gridsX][gridsY];
      flags    = new byte[width][height];
      entities = EMPTY_ARRAY;
    }

    /**
     * Constructs a zone using custom sizing info
     */
    Zone(Levels.Entry level, int gridSizeX, int gridSizeY, int gridsX, int gridsY) {
      this.level     = level;
      this.type      = Diablo.files.LvlTypes.get(level.LevelType);
      this.gridSizeX = gridSizeX;
      this.gridSizeY = gridSizeY;
      this.gridsX    = gridsX;
      this.gridsY    = gridsY;

      tilesX   = gridsX * gridSizeX;
      tilesY   = gridsY * gridSizeY;
      width    = gridsX * DT1.Tile.SUBTILE_SIZE;
      height   = gridsY * DT1.Tile.SUBTILE_SIZE;
      presets  = new Preset[gridsX][gridsY];
      flags    = new byte[width][height];
      entities = EMPTY_ARRAY;
    }

    private void loadEntities(DS1 ds1, int gridX, int gridY) {
      if (entities == EMPTY_ARRAY) entities = new Array<>();
      for (int i = 0; i < ds1.numObjects; i++) {
        DS1.Object obj = ds1.objects[i];
        Entity entity = Entity.create(ds1, obj);
        if (entity == null) continue;
        entity.position().set(x + gridX + obj.x, y + gridY + obj.y, 0);
        entities.add(entity);
      }
    }

    public void setPosition(int x, int y) {
      this.x = x;
      this.y = y;
      tx = x / DT1.Tile.SUBTILE_SIZE;
      ty = y / DT1.Tile.SUBTILE_SIZE;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("name", level.LevelName)
          .append("id", level.Id)
          .append("x", x)
          .append("y", y)
          .append("width", width)
          .append("height", height)
          .append("tx", tx)
          .append("ty", ty)
          .append("tilesX", tilesX)
          .append("tilesY", tilesY)
          .append("gridSizeX", gridSizeX)
          .append("gridSizeY", gridSizeY)
          .append("gridsX", gridsX)
          .append("gridsY", gridsY)
          .build();
    }

    public int getLocalTX(int tx) { return tx - this.tx; }
    public int getLocalTY(int ty) { return ty - this.ty; }

    public boolean contains(int x, int y) {
      x -= this.x;
      y -= this.y;
      return 0 <= x && x < width
          && 0 <= y && y < height;
    }

    public Tile get(int layer, int tx, int ty) {
      //System.out.println("layer " + layer + " " + tx + ", " + ty + " -W " + this.tx + ", " + this.ty + " -> " + (tx - this.tx) + ", " + (ty - this.ty));
      return tiles[layer] == null ? null : tiles[layer][tx - this.tx][ty - this.ty];
    }

    public int getGridX(int tx) { return ((tx - this.tx) % gridSizeX) * DT1.Tile.SUBTILE_SIZE; }
    public int getGridY(int ty) { return ((ty - this.ty) % gridSizeY) * DT1.Tile.SUBTILE_SIZE; }

    public Preset getGrid(int tx, int ty) {
      return presets[(tx - this.tx) / gridSizeX][(ty - this.ty) / gridSizeY];
    }

    void load(DT1s dt1s) {
      Preconditions.checkState(tiles == null, "tiles have already been loaded");
      tiles = new Tile[Map.MAX_LAYERS][][];
      for (int x = 0, gridX = 0, gridY = 0; x < gridsX; x++, gridX += gridSizeX, gridY = 0) {
        for (int y = 0; y < gridsY; y++, gridY += gridSizeY) {
          Preset preset = presets[x][y];
          if (preset == null) {
            generator.generate(this, dt1s, gridX, gridY);
            continue;
          }

          DS1 ds1 = Diablo.assets.get(TILES_PATH + preset.ds1Path);
          preset.set(ds1, dt1s);
          preset.copyTo(this, gridX, gridY);
          loadEntities(ds1, gridX, gridY);
        }
      }
    }

    interface Generator {
      void generate(Zone zone, DT1s dt1s, int tx, int ty);
    }
  }

  static class Tile {
    DT1.Tile tile;
    DS1.Cell cell;
    DT1.Tile sibling;

    public static Tile of(DT1.Tile tile) {
      return of(tile, null);
    }

    public static Tile of(DT1s dt1s, DS1.Cell cell) {
      return of(dt1s.get(cell), cell);
    }

    public static Tile of(DT1.Tile tile, DS1.Cell cell) {
      Tile t = new Tile();
      t.cell = cell;
      t.tile = tile;
      return t;
    }

    void setSibling(DT1.Tile sibling) {
      this.sibling = sibling;
    }
  }

  static class Preset {
    LvlPrest.Entry    preset;
    String            ds1Path;
    DS1               ds1;
    DT1s              dt1s;
    IntMap<PopPad>    popPads;

    Array<AssetDescriptor> dependencies;

    static Preset of(LvlPrest.Entry preset, int id) {
      Preset p  = new Preset();
      p.preset  = preset;
      p.ds1Path = preset.File[id];
      return p;
    }

    static Preset of(LvlPrest.Entry preset, String ds1Path) {
      Preset p  = new Preset();
      p.preset  = preset;
      p.ds1Path = ds1Path;
      return p;
    }

    static Preset[] of(LvlPrest.Entry preset) {
      Preset[] presets = new Preset[preset.Files];
      for (int i = 0; i < preset.Files; i++) {
        presets[i] = Preset.of(preset, i);
      }

      return presets;
    }

    Array<AssetDescriptor> getDependencies(LvlTypes.Entry type) {
      Array<AssetDescriptor> dependencies = new Array<>();
      dependencies.add(new AssetDescriptor<>(TILES_PATH + ds1Path, DS1.class));

      int DT1Mask = preset.Dt1Mask;
      for (int i = 0; i < Integer.SIZE; i++) {
        if ((DT1Mask & (1 << i)) != 0) {
          dependencies.add(new AssetDescriptor<>(TILES_PATH + type.File[i], DT1.class));
        }
      }

      return dependencies;
    }

    void load(AssetManager assets, LvlTypes.Entry type, ObjectSet<AssetDescriptor<DT1>> dt1Dependencies) {
      if (dependencies != null) return;
      dependencies = new Array<>();

      AssetDescriptor ds1Descriptor;
      dependencies.add(ds1Descriptor = new AssetDescriptor<>(TILES_PATH + ds1Path, DS1.class));
      assets.load(ds1Descriptor);

      int DT1Mask = preset.Dt1Mask;
      for (int i = 0; i < Integer.SIZE; i++) {
        if ((DT1Mask & (1 << i)) != 0) {
          AssetDescriptor<DT1> dt1Descriptor = new AssetDescriptor<>(TILES_PATH + type.File[i], DT1.class);
          dependencies.add(dt1Descriptor);
          dt1Dependencies.add(dt1Descriptor);
          assets.load(dt1Descriptor);
        }
      }
    }

    void dispose(AssetManager assets) {
      if (dependencies == null) return;
      for (AssetDescriptor descriptor : dependencies) {
        assets.unload(descriptor.fileName);
      }

      dependencies = null;
    }

    void set(DS1 ds1, DT1s dt1s) {
      if (this.ds1 == null) {
        this.ds1  = ds1;
        this.dt1s = dt1s;
      } else assert this.ds1 == ds1 && this.dt1s == dt1s;
    }

    void copyTo(Zone zone, int tx, int ty) {
      copyFloors(zone, Map.FLOOR_OFFSET, tx, ty);
      copyWalls (zone, Map.WALL_OFFSET,  tx, ty);
    }

    void copyFloors(Zone zone, int layer, int tx, int ty) {
      final boolean NO_FLOOR;
      switch (ds1.numFloors) {
        case 1:
          NO_FLOOR = (ds1.floors[0].value & DS1.Cell.FLOOR_UNWALK_MASK) == 0;
          break;
        case 2:
          NO_FLOOR = (ds1.floors[0].value & DS1.Cell.FLOOR_UNWALK_MASK) == 0
                  && (ds1.floors[1].value & DS1.Cell.FLOOR_UNWALK_MASK) == 0;
          break;
        default:
          NO_FLOOR = false;
      }

      final int startTx = tx;
      final int startTy = ty;
      for (int l = 0; l < ds1.numFloors; l++, layer++, ty = startTy) {
        if (zone.tiles[layer] == null) zone.tiles[layer] = new Tile[zone.tilesX][zone.tilesY];
        for (int y = 0; y < ds1.height; y++, ty++, tx = startTx) {
          int ptr = l + (y * ds1.floorLine);
          for (int x = 0; x < ds1.width; x++, tx++, ptr += ds1.numFloors) {
            DS1.Cell cell = ds1.floors[ptr];
            if ((cell.value & DS1.Cell.UNWALKABLE_MASK) != 0) {
              orFlags(zone.flags, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
            }

            if ((cell.value & DS1.Cell.HIDDEN_MASK) != 0) {
              continue;
            }

            Tile tile = zone.tiles[layer][tx][ty] = Tile.of(dt1s, cell);
            copyFlags(zone.flags, tx, ty, tile.tile);
            if (NO_FLOOR) {
              orFlags(zone.flags, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
            }
          }
        }
      }
    }

    void copyWalls(Zone zone, int layer, int tx, int ty) {
      final int startTx = tx;
      final int startTy = ty;
      for (int l = 0; l < ds1.numWalls; l++, layer++, ty = startTy) {
        if (zone.tiles[layer] == null) zone.tiles[layer] = new Tile[zone.tilesX][zone.tilesY];
        for (int y = 0; y < ds1.height; y++, ty++, tx = startTx) {
          int ptr = l + (y * ds1.wallLine);
          for (int x = 0; x < ds1.width; x++, tx++, ptr += ds1.numWalls) {
            DS1.Cell cell = ds1.walls[ptr];

            if (Orientation.isSpecial(cell.orientation)) {
              zone.tiles[layer][tx][ty] = Tile.of(dt1s, cell);
              if (ID.POPPADS.contains(cell.id)) {
                if (popPads == null) popPads = new IntMap<>();
                PopPad popPad = popPads.get(cell.id);
                if (popPad == null)
                  popPads.put(cell.id, new PopPad(cell.id, x * DT1.Tile.SUBTILE_SIZE, y * DT1.Tile.SUBTILE_SIZE));
                else
                  popPad.setEnd(
                      x * DT1.Tile.SUBTILE_SIZE + DT1.Tile.SUBTILE_SIZE + preset.PopPad,
                      y * DT1.Tile.SUBTILE_SIZE + DT1.Tile.SUBTILE_SIZE + preset.PopPad);
              }
            }

            if ((cell.value & DS1.Cell.HIDDEN_MASK) != 0) {
              // This seems like all the special tiles, null usually means marker tile (start pos),
              // non null usually means stuff like side of river, used for ?weather? ?rain drops?
              if (!Orientation.isSpecial(cell.orientation)) {
                // prints all of the debug tiles on side of river (any maybe elsewhere)
                //DT1.Tile tile = dt1s.get(cell);
                //System.out.println(x + ", " + y + " " + tile);
                orFlags(zone.flags, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
              }

              continue;
            }

            if (Orientation.isFloor(cell.orientation)) {
              continue;
            }

            // This tracks the blue river tiles that might be left for debugging
            // since they also appear in the ds1 editor this is based from,
            // I'll leave them in for now
            //if (cell.orientation == 1 && cell.mainIndex == 5 && cell.subIndex == 0) {
            //  System.out.println("found it! " + String.format("%08x", cell.value));
            //}

            Tile tile = zone.tiles[layer][tx][ty] = Tile.of(dt1s, cell);
            copyFlags(zone.flags, tx, ty, tile.tile);

            // Special case, because LEFT_NORTH_CORNER_WALL don't seem to exist, but they contain
            // collision data for RIGHT_NORTH_CORNER_WALL, ORing the data just in case some
            // RIGHT_NORTH_CORNER_WALL actually does anything
            if (cell.orientation == Orientation.RIGHT_NORTH_CORNER_WALL) {
              tile.sibling = dt1s.get(Orientation.LEFT_NORTH_CORNER_WALL, cell.mainIndex, cell.subIndex);
              copyFlags(zone.flags, tx, ty, tile.sibling);
            }
          }
        }
      }
    }

    static void orFlags(byte[][] flags, int tx, int ty, int flag) {
      tx *= DT1.Tile.SUBTILE_SIZE;
      final int startY = ty * DT1.Tile.SUBTILE_SIZE;
      for (int x = 0; x < DT1.Tile.SUBTILE_SIZE; x++, tx++) {
        ty = startY;
        for (int y = 0; y < DT1.Tile.SUBTILE_SIZE; y++, ty++) {
          flags[tx][ty] |= flag;
        }
      }
    }

    static void copyFlags(byte[][] flags, int tx, int ty, DT1.Tile tile) {
      // Note: walkable flags are stored inverted y-axis, this corrects it
      final int startX = tx * DT1.Tile.SUBTILE_SIZE;
      ty = (ty * DT1.Tile.SUBTILE_SIZE) + (DT1.Tile.SUBTILE_SIZE - 1);
      for (int y = 0, t = 0; y < DT1.Tile.SUBTILE_SIZE; y++, ty--) {
        tx = startX;
        for (int x = 0; x < DT1.Tile.SUBTILE_SIZE; x++, tx++, t++) {
          flags[tx][ty] |= tile.flags[t];
        }
      }
    }

    @Override
    public String toString() {
      return ds1Path;
    }

    static class PopPad {
      int id;
      int startX, startY;
      int endX, endY;

      PopPad(int id, int x, int y) {
        this.id = id;
        startX  = x;
        startY  = y;
      }

      void setEnd(int x, int y) {
        endX = x;
        endY = y;
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("id", ID.getName(id))
            .append("startX", startX)
            .append("startY", startY)
            .append("endX", endX)
            .append("endY", endY)
            .build();
      }
    }
  }
}
