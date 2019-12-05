package com.riiablo.map;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.Riiablo;
import com.riiablo.codec.COFD2;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlPrest;
import com.riiablo.codec.excel.LvlTypes;
import com.riiablo.engine.component.WarpComponent;
import com.riiablo.map.pfa.AStarPathFinder;
import com.riiablo.map.pfa.Point2;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;

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

    static IntSet WARPS;
    static {
      WARPS = new IntSet();
      WARPS.addAll(
          VIS_0_00, VIS_0_01, VIS_0_02, VIS_0_03, VIS_0_04, VIS_0_05, VIS_0_06, VIS_0_07, VIS_0_08, VIS_0_09, VIS_0_10,
          VIS_1_11, VIS_1_12, VIS_1_13, VIS_1_14, VIS_1_15, VIS_1_16, VIS_1_17, VIS_1_18, VIS_1_19,
          VIS_2_20, VIS_2_21, VIS_2_22, VIS_2_23, VIS_2_24, VIS_2_25, VIS_2_26, VIS_2_27, VIS_2_28, VIS_2_29,
          VIS_3_30, VIS_3_31, VIS_3_32, VIS_3_33, VIS_3_34, VIS_3_35, VIS_3_36,
          VIS_4_37, VIS_4_38, VIS_4_39, VIS_4_40, VIS_4_41,
          VIS_5_81, VIS_5_42, VIS_5_43,
          VIS_6_44, VIS_6_45, VIS_6_82,
          VIS_7_46, VIS_7_83);
    }

    public static int getWarpIndex(DS1.Cell cell) {
      return cell.mainIndex;
    }

    static IntSet POPPADS;
    static {
      POPPADS = new IntSet();
      POPPADS.addAll(
          _47, _48, _49,
          _50, _51, _52, _53, _54, _55, _56, _57, _58, _59,
          _60, _61, _62, _63, _64, _65, _66, _67, _68, _69,
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

    public static int getGroup(int mainIndex) {
      switch (mainIndex) {
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

    public static int getGroup(DT1.Tile tile) {
      if (tile == null) return 0;
      return getGroup(tile.mainIndex);
    }

    public static int getGroup(DS1.Cell cell) {
      if (cell == null) return 0;
      return getGroup(cell.mainIndex);
    }

    public static Color getColor(int mainIndex) {
      switch (mainIndex) {
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

    public static Color getColor(DS1.Cell cell) {
      if (cell == null) return Color.WHITE;
      return getColor(cell.mainIndex);
    }

    public static Color getColor(DT1.Tile tile) {
      if (tile == null) return Color.WHITE;
      return getColor(tile.mainIndex);
    }
  }

  public static int round(float i) {
    return MathUtils.round(i);
  }

  final int seed;
  final int diff;
  int act = -1;

  final IntMap<DT1s> dt1s = new IntMap<>();
  final Array<Zone> zones = new Array<>(); // TODO: replace with R-tree? https://en.wikipedia.org/wiki/R-tree

  public Map(int seed, int diff) {
    this.seed = seed;
    this.diff = diff;
  }

  public int getAct() {
    return act;
  }

  public void setAct(int act) {
    if (this.act != act) {
      this.act = act;
      dispose();
      generate(act);
      // trigger load screen...
    }
  }

  /**
   * TODO: the 3 methods below should be package-private and create a class which can delegate
   */

  public void load() {
    for (Zone zone : zones) zone.load();
  }

  public void finishLoading() {
    for (Zone zone : zones) zone.finishLoading();
  }

  public void generate() {
    /**
     * FIXME: throwing nested iterator error if not creating new wrapper
     *        nested iterator is within systems which use Map#getZone()
     */
    for (Zone zone : new Array.ArrayIterator<>(zones)) zone.generate();
  }

  @Override
  public void dispose() {
    for (Zone zone : zones) Zone.free(zone);
    zones.clear();
    for (DT1s dt1s : this.dt1s.values()) dt1s.clear();
    dt1s.clear();
    mapGraph.clear();
  }

  public Array<AssetDescriptor> getDependencies() {
    Array<AssetDescriptor> dependencies = new Array<>();
    for (Zone zone : zones) dependencies.addAll(zone.getDependencies());
    return dependencies;
  }

  public void generate(int act) {
    MathUtils.random.setSeed(seed);
    Riiablo.cofs.active = updateCofs(act);
    switch (act) {
      case 0: Act1MapBuilder.INSTANCE.generate(this, seed, diff); break;
      case 1: Act2MapBuilder.INSTANCE.generate(this, seed, diff); break;
      case 2: Act3MapBuilder.INSTANCE.generate(this, seed, diff); break;
      case 3: Act4MapBuilder.INSTANCE.generate(this, seed, diff); break;
      case 4: Act5MapBuilder.INSTANCE.generate(this, seed, diff); break;
    }
  }

  private COFD2 updateCofs(int act) {
    switch (act) {
      case 0:  return Riiablo.cofs.cmncof_a1;
      case 1:  return Riiablo.cofs.cmncof_a2;
      case 2:  return Riiablo.cofs.cmncof_a3;
      case 3:  return Riiablo.cofs.cmncof_a4;
      case 4:  return Riiablo.cofs.cmncof_a6;
      default: return Riiablo.cofs.active;
    }
  }

  private MapGraph        mapGraph   = new MapGraph(this);
  private AStarPathFinder pathFinder = new AStarPathFinder(mapGraph, true);

  public boolean findPath(Vector2 src, Vector2 dst, GraphPath<Point2> path) {
    return findPath(src, dst, DT1.Tile.FLAG_BLOCK_WALK, 0, path);
  }

  public boolean findPath(Vector2 src, Vector2 dst, int flags, int size, GraphPath<Point2> path) {
    return mapGraph.searchNodePath(pathFinder, src, dst, flags, size, path);
  }

  public void smoothPath(SmoothableGraphPath<Point2, Vector2> path) {
    smoothPath(DT1.Tile.FLAG_BLOCK_WALK, 0, path);
  }

  public void smoothPath(int flags, int size, SmoothableGraphPath<Point2, Vector2> path) {
    mapGraph.smoothPath(flags, size, path);
  }

  public boolean castRay(Ray<Vector2> ray, int flags, int size, Collision<Vector2> dst) {
    return mapGraph.raycaster.findCollision(ray, flags, size, dst);
  }

  /**
   * @param x   world sub-tile
   * @param y   world sub-tile
   * @param tx  world tile
   * @param ty  world tile
   * @param stx sub-tile (0-4)
   * @param sty sub-tile (0-4)
   */
  // TODO: x,y alone should be enough, but others are available in MapRenderer on each position change anyways
  public void updatePopPads(Bits bits, int x, int y, int tx, int ty, int stx, int sty) {
    bits.clear();
    Zone zone = getZone(x, y);
    if (zone != null) {
      Map.Preset preset = zone.getGrid(tx, ty);
      if (preset != null) {
        int presetX = zone.getGridX(tx) + stx;
        int presetY = zone.getGridX(ty) + sty;
        preset.updatePopPads(bits, presetX, presetY);
      }
    }
  }

  final IntIntMap warpSubsts = new IntIntMap();

  public void addWarpSubsts(IntIntMap warps) {
    this.warpSubsts.putAll(warps);
  }

  public void clearWarpSubsts(IntIntMap warps) {
    for (IntIntMap.Entry entry : warps.entries()) {
      this.warpSubsts.remove(entry.key, entry.value);
    }
  }

  public void clearWarpSubsts() {
    this.warpSubsts.clear();
  }

  // FIXME: only works properly for zone 0 at 0,0
  public Vector2 find(int id) {
    if (zones.size == 0) return null;
    Vector2 origin = zones.first().presets[0][0].ds1.find(id);
    if (origin == null) return null;
    return origin.cpy()
        .scl(DT1.Tile.SUBTILE_SIZE)
        .add(DT1.Tile.SUBTILE_CENTER);
  }

  public int flags(Vector2 vec) {
    return flags(round(vec.x), round(vec.y));
  }

  public int flags(int x, int y) {
    Zone zone = getZone(x, y);
    if (zone == null) return 0xFF;
    return zone.flags(x - zone.x, y - zone.y);
  }

  void or(Vector2 position, int width, int height, int flags) {
    if (width == 0 || height == 0) return;
    int x0 = round(position.x - width  / 2f);
    int y0 = round(position.y - height / 2f);
    for (int x = 0, dx = x0; x < width; x++, dx++) {
      for (int y = 0, dy = y0; y < height; y++, dy++) {
        Zone zone = getZone(dx, dy);
        if (zone != null) zone.or(dx - zone.x, dy - zone.y, flags);
      }
    }
  }

  public Zone getZone(Vector2 vec) {
    return getZone(round(vec.x), round(vec.y));
  }

  public Zone getZone(int x, int y) {
    for (Zone zone : zones) if (zone.contains(x, y)) return zone;
    return null;
  }

  public Zone findZone(Levels.Entry level) {
    for (Zone zone : zones) if (zone.level == level) return zone;
    return null;
  }

  Zone addZone(Levels.Entry level, LvlPrest.Entry preset, int ds1) {
    assert preset.LevelId != 0 : "presets should have an assigned level id";
    Zone zone = addZone(level, level.SizeX[diff], level.SizeY[diff]);
    zone.presets[0][0] = Preset.of(preset, ds1);
    return zone;
  }

  Zone addZone(Levels.Entry level, int gridSizeX, int gridSizeY) {
    Zone zone = Zone.obtain(this, level, diff, gridSizeX, gridSizeY);
    if (DEBUG_ZONES) Gdx.app.debug(TAG, zone.toString());
    zones.add(zone);
    return zone;
  }

  Zone addZone(Levels.Entry level, int gridSizeX, int gridSizeY, int gridsX, int gridsY) {
    Zone zone = Zone.obtain(this, level, diff, gridSizeX, gridSizeY, gridsX, gridsY);
    if (DEBUG_ZONES) Gdx.app.debug(TAG, zone.toString());
    zones.add(zone);
    return zone;
  }

  public static class Zone implements Pool.Poolable, Disposable {
    static final int[] sizes = {80 * 80, 200 * 200};
    @SuppressWarnings("unchecked")
    static final Pool<DT1.Tile[]>[] tilePools = (Pool<DT1.Tile[]>[]) new Pool[sizes.length];
    @SuppressWarnings("unchecked")
    static final Pool<byte[]>[] bytePools = (Pool<byte[]>[]) new Pool[sizes.length];
    static {
      for (int i = 0; i < tilePools.length; i++) tilePools[i] = new TileArrayPool(sizes[i]);
      for (int i = 0; i < bytePools.length; i++) bytePools[i] = new ByteArrayPool(sizes[i] * DT1.Tile.NUM_SUBTILES);
    }

    static DT1.Tile[] obtainTileArray(int size) {
      for (int i = 0; i < sizes.length; i++) {
        if (size <= sizes[i]) {
          DT1.Tile[] tiles = tilePools[i].obtain();
          Arrays.fill(tiles, 0, size, null);
          return tiles;
        }
      }

      Gdx.app.error(TAG, "Creating custom sized tile array: " + size);
      return new DT1.Tile[size];
    }
    static void free(DT1.Tile[] layer) {
      if (layer == null) return;
      for (int i = 0; i < sizes.length; i++) {
        if (layer.length <= sizes[i]) {
          tilePools[i].free(layer);
          return;
        }
      }
    }

    static byte[] obtainByteArray(int size) {
      for (int i = 0; i < sizes.length; i++) {
        if (size <= sizes[i] * DT1.Tile.NUM_SUBTILES) {
          byte[] bytes = bytePools[i].obtain();
          Arrays.fill(bytes, 0, size, (byte) 0);
          return bytes;
        }
      }

      Gdx.app.error(TAG, "Creating custom sized byte array: " + size);
      return new byte[size];
    }
    static void free(byte[] b) {
      for (int i = 0; i < sizes.length; i++) {
        if (b.length <= sizes[i] * DT1.Tile.NUM_SUBTILES) {
          bytePools[i].free(b);
          return;
        }
      }
    }

    int x, y;
    int width, height;
    int gridSizeX, gridSizeY;
    int gridsX, gridsY;
    int tx, ty;
    int tilesX, tilesY;

    public Map          map;
    public Levels.Entry level;
    public int          diff;

    boolean        town;
    LvlTypes.Entry type;
    DT1s           dt1s;
    byte           flags[];
    final DT1.Tile tiles[][] = new DT1.Tile[Map.MAX_LAYERS][];
    Preset         presets[][];

    static final Array<Entity> EMPTY_ENTITY_ARRAY = new Array<>(0);
    Array<Entity> entities = EMPTY_ENTITY_ARRAY;

    static final ComponentMapper<WarpComponent> warpComponent = ComponentMapper.getFor(WarpComponent.class);
    static final IntIntMap EMPTY_INT_INT_MAP = new IntIntMap(0);
    IntIntMap warps = EMPTY_INT_INT_MAP;

    static final Array<AssetDescriptor> EMPTY_ASSET_ARRAY = new Array<>(0);
    Array<AssetDescriptor> dependencies = EMPTY_ASSET_ARRAY;

    static final IntMap<DS1.Cell> EMPTY_INT_CELL_MAP = new IntMap<>();
    IntMap<DS1.Cell> specials = EMPTY_INT_CELL_MAP;

    static final Generator EMPTY_GENERATOR = new Generator() {
      @Override public void init(Zone zone) {}
      @Override public void generate(Zone zone, DT1s dt1s, int tx, int ty) {}
    };
    Generator generator = EMPTY_GENERATOR;

    static final Pool<Zone> pool = Pools.get(Zone.class, 16);

    static Zone obtain(Map map, Levels.Entry level, int diff, int gridSizeX, int gridSizeY) {
      return pool.obtain().set(map, level, diff, gridSizeX, gridSizeY);
    }

    static Zone obtain(Map map, Levels.Entry level, int diff, int gridSizeX, int gridSizeY, int gridsX, int gridsY) {
      return pool.obtain().set(map, level, diff, gridSizeX, gridSizeY, gridsX, gridsY);
    }

    static void free(Zone zone) {
      zone.dispose();
      pool.free(zone);
    }

    private Zone setInternal(Map map, Levels.Entry level, int diff, int gridSizeX, int gridSizeY) {
      this.map       = map;
      this.level     = level;
      this.diff      = diff;
      this.type      = Riiablo.files.LvlTypes.get(level.LevelType);
      this.gridSizeX = gridSizeX;
      this.gridSizeY = gridSizeY;
      return this;
    }

    Zone set(Map map, Levels.Entry level, int diff, int gridSizeX, int gridSizeY) {
      setInternal(map, level, diff, gridSizeX, gridSizeY);
      tilesX   = level.SizeX[diff];
      tilesY   = level.SizeY[diff];
      width    = tilesX * DT1.Tile.SUBTILE_SIZE;
      height   = tilesY * DT1.Tile.SUBTILE_SIZE;
      gridsX   = tilesX / gridSizeX;
      gridsY   = tilesY / gridSizeY;
      flags    = obtainByteArray(width * height);
      presets  = new Preset[gridsX][gridsY];
      return this;
    }

    Zone set(Map map, Levels.Entry level, int diff, int gridSizeX, int gridSizeY, int gridsX, int gridsY) {
      setInternal(map, level, diff, gridSizeX, gridSizeY);
      this.gridsX = gridsX;
      this.gridsY = gridsY;
      tilesX   = gridsX * gridSizeX;
      tilesY   = gridsY * gridSizeY;
      width    = gridsX * DT1.Tile.SUBTILE_SIZE;
      height   = gridsY * DT1.Tile.SUBTILE_SIZE;
      flags    = obtainByteArray(width * height);
      presets  = new Preset[gridsX][gridsY];
      return this;
    }

    /**
     * This is called when zones are cleared before they are disposed -- not when obtained.
     */
    @Override
    public void reset() {}

    @Override
    public void dispose() {
      x = y = 0;
      width = height = 0;
      gridSizeX = gridSizeY = 0;
      gridsX = gridsY = 0;
      tx = ty = 0;
      tilesX = tilesY = 0;

      free(flags);
      flags = null;

      for (DT1.Tile[] layer : tiles) free(layer);
      Arrays.fill(tiles, null);

      //for (Preset[] x : presets) for (Preset y : x) if (y != null) y.dispose();
      presets = null;

      for (AssetDescriptor asset : dependencies) Riiablo.assets.unload(asset.fileName);
      dependencies = EMPTY_ASSET_ARRAY;

      dt1s = null; // TODO: setting null -- depending on Map dispose to clear DT1s on act change
      town = false;
      entities = EMPTY_ENTITY_ARRAY;
      warps = EMPTY_INT_INT_MAP;
      generator = EMPTY_GENERATOR;
      specials = EMPTY_INT_CELL_MAP;
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

    public void setPosition(int x, int y) {
      this.x = x;
      this.y = y;
      tx = x / DT1.Tile.SUBTILE_SIZE;
      ty = y / DT1.Tile.SUBTILE_SIZE;
    }

    public boolean isTown() {
      return town;
    }

    static int index(int width, int x, int y) {
      return y * width + x;
    }

    int tileIndex(int tx, int ty) {
      return index(tilesX, tx, ty);
    }

    public DT1.Tile get(int layer, int tx, int ty) {
      //System.out.println("layer " + layer + " " + tx + ", " + ty + " -W " + this.tx + ", " + this.ty + " -> " + (tx - this.tx) + ", " + (ty - this.ty));
      return tiles[layer] == null ? null : tiles[layer][tileIndex(tx - this.tx, ty - this.ty)];
    }

    public int flags(int x, int y) {
      return flags[index(width, x, y)] & 0xFF;
    }

    public int or(int x, int y, int flags) {
      return (this.flags[index(width, x, y)] |= flags) & 0xFF;
    }

    void addEntity(Entity entity) {
      if (entity == null) return;
      if (entities == EMPTY_ENTITY_ARRAY) entities = new Array<>();
      entities.add(entity);
      Riiablo.engine.addEntity(entity);
    }

    void addWarp(int index, int warpX, int warpY) {
      final int x = this.x + (warpX * DT1.Tile.SUBTILE_SIZE);
      final int y = this.y + (warpY * DT1.Tile.SUBTILE_SIZE);
      if (entities == EMPTY_ENTITY_ARRAY) entities = new Array<>();
      Entity entity = Riiablo.engine.createWarp(map, this, index, x, y);
      entities.add(entity);
      Riiablo.engine.addEntity(entity);
    }

    void setWarp(int src, int dst) {
      if (warps == EMPTY_INT_INT_MAP) warps = new IntIntMap(4);
      warps.put(src, dst);
    }

    public int getWarp(int src) {
      return warps.get(src, -1);
    }

    public Entity findWarp(int id) {
      for (Entity entity : entities) {
        WarpComponent warpComponent = this.warpComponent.get(entity);
        if (warpComponent != null && warpComponent.index == id) {
          return entity;
        }
      }

      return null;
    }

    public int getLocalTX(int tx) { return tx - this.tx; }
    public int getLocalTY(int ty) { return ty - this.ty; }
    public int getGlobalX(int x) { return this.x + x; }
    public int getGlobalY(int y) { return this.y + y; }

    public int getGridX(int tx) { return ((tx - this.tx) % gridSizeX) * DT1.Tile.SUBTILE_SIZE; }
    public int getGridY(int ty) { return ((ty - this.ty) % gridSizeY) * DT1.Tile.SUBTILE_SIZE; }

    public Preset getGrid(int tx, int ty) {
      return presets[(tx - this.tx) / gridSizeX][(ty - this.ty) / gridSizeY];
    }

    public boolean contains(int x, int y) {
      x -= this.x;
      y -= this.y;
      return 0 <= x && x < width
          && 0 <= y && y < height;
    }

    public DT1.Tile[] getLayer(int layer) {
      return tiles[layer];
    }

    Array<AssetDescriptor> getDependencies() {
      if (dependencies == EMPTY_ASSET_ARRAY) {
        dependencies = new Array<>(false, 64);
        for (Preset[] x : presets) for (Preset y : x) if (y != null) dependencies.addAll(y.getDependencies(type));
      }
      return dependencies;
    }

    void load() {
      for (AssetDescriptor asset : getDependencies()) Riiablo.assets.load(asset);
    }

    void finishLoading() {
      int type = this.type.Id;
      DT1s dt1s = map.dt1s.get(type);
      if (dt1s == null) map.dt1s.put(type, dt1s = new DT1s());
      for (AssetDescriptor asset : getDependencies()) {
        Riiablo.assets.finishLoadingAsset(asset);
        if (asset.type == DT1.class) {
          dt1s.add((DT1) Riiablo.assets.get(asset));
        }
      }
    }

    void generate() {
//      boolean allNull = true;
//      for (int i = 0; allNull && i < MAX_LAYERS; i++) allNull = tiles[i] == null;
//      Validate.validState(allNull, "tiles have already been loaded");
      generator.init(this);
      dt1s = map.dt1s.get(type.Id);
      tiles[Map.FLOOR_OFFSET] = Zone.obtainTileArray(tilesX * tilesY);
      for (int x = 0, gridX = 0, gridY = 0; x < gridsX; x++, gridX += gridSizeX, gridY = 0) {
        for (int y = 0; y < gridsY; y++, gridY += gridSizeY) {
          Preset preset = presets[x][y];
          if (preset == null) {
            generator.generate(this, dt1s, gridX, gridY);
            continue;
          }

          preset.finishLoading();
          preset.copyTo(this, gridX, gridY);
        }
      }
    }

    Vector2 find(int id) {
      for (Preset[] x : presets) {
        for (Preset y : x) if (y != null) {
          Vector2 origin = y.ds1.find(id);
          if (origin != null) return origin;
        }
      }

      return null;
    }

    /**
     * this key should be good enough -- layers are 0-15 and tiles are 0-200
     */
    static int tileHashCode(int layer, int tx, int ty) {
      return ((layer & 0xF) << 28) | ((tx & 0x3FFF) << 14) | (ty & 0x3FFF);
    }

    void putCell(int layer, int tx, int ty, DS1.Cell cell) {
      if (specials == EMPTY_INT_CELL_MAP) specials = new IntMap<>();
      specials.put(tileHashCode(layer, tx, ty), cell);
    }

    DS1.Cell getCell(int layer, int tx, int ty) {
      return specials.get(tileHashCode(layer, tx - this.tx, ty - this.ty));
    }

    interface Generator {
      void init(Zone zone);
      void generate(Zone zone, DT1s dt1s, int tx, int ty);
    }
  }

  static class Preset implements Disposable {
    LvlPrest.Entry preset;
    String         ds1Path;
    DS1            ds1;
    IntMap<PopPad> popPads;

    AssetDescriptor<DS1>   ds1Descriptor;
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

    @Deprecated
    static Preset[] getPresets(LvlPrest.Entry preset) {
      Preset[] presets = new Preset[preset.Files];
      for (int i = 0; i < preset.Files; i++) {
        presets[i] = Preset.of(preset, i);
      }

      return presets;
    }

    static int getPresets(LvlPrest.Entry preset, int[] fileIds) {
      int numFiles = 0;
      for (int i = 0; i < preset.File.length; i++) {
        if (preset.File[i].charAt(0) != '0') {
          fileIds[numFiles++] = i;
        }
      }

      return numFiles;
    }

    @Override
    public String toString() {
      return ds1Path;
    }

    @Override
    @Deprecated
    public void dispose() {
      throw new UnsupportedOperationException("Preset assets should be disposed by parent Zone");
    }

    Array<AssetDescriptor> getDependencies(LvlTypes.Entry type) {
      if (dependencies == null) {
        dependencies = new Array<>(16);
        dependencies.add(ds1Descriptor = new AssetDescriptor<>(TILES_PATH + ds1Path, DS1.class));

        int DT1Mask = preset.Dt1Mask;
        for (int i = 0; i < Integer.SIZE; i++) {
          if ((DT1Mask & (1 << i)) != 0) {
            dependencies.add(new AssetDescriptor<>(TILES_PATH + type.File[i], DT1.class));
          }
        }
      }

      return dependencies;
    }

    DS1 finishLoading() {
      assert Riiablo.assets.isLoaded(ds1Descriptor) : ds1Path + " should have been loaded by parent Zone";
      return ds1 = Riiablo.assets.get(ds1Descriptor);
    }

    void copyTo(Zone zone, int tx, int ty) {
      copyFloors (zone, Map.FLOOR_OFFSET,  tx, ty);
      copyWalls  (zone, Map.WALL_OFFSET,   tx, ty);
      copyShadows(zone, Map.SHADOW_OFFSET, tx, ty);
      copyObjects(zone, tx, ty);
    }

    void copyFloors(Zone zone, int layer, int tx, int ty) {
      /**
       * TODO: can this be safely removed? Maybe the first tile has some special flags?
       */
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
        if (zone.tiles[layer] == null) zone.tiles[layer] = Zone.obtainTileArray(zone.tilesX * zone.tilesY);
        for (int y = 0; y < ds1.height; y++, ty++, tx = startTx) {
          int ptr = l + (y * ds1.floorLine);
          for (int x = 0; x < ds1.width; x++, tx++, ptr += ds1.numFloors) {
            DS1.Cell cell = ds1.floors[ptr];
            if ((cell.value & DS1.Cell.UNWALKABLE_MASK) != 0) {
              or(zone, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
            }

            if ((cell.value & DS1.Cell.FLOOR_UNWALK_MASK) == 0) {
              if (l == 0) or(zone, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
              continue;
            }

            if ((cell.value & DS1.Cell.HIDDEN_MASK) != 0) {
              continue;
            }

            DT1.Tile tile = zone.tiles[layer][zone.tileIndex(tx, ty)] = zone.dt1s.get(cell);
            // FIXME: These are "empty"/"unknown" tiles, in caves, they fill in the gaps
            if (tile == null) {
              continue;
            }

            or(zone, tx, ty, tile);
            if (NO_FLOOR) {
              or(zone, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
            }
          }
        }
      }
    }

    void copyWalls(Zone zone, int layer, int tx, int ty) {
      final int startTx = tx;
      final int startTy = ty;
      for (int l = 0; l < ds1.numWalls; l++, layer++, ty = startTy) {
        if (zone.tiles[layer] == null) zone.tiles[layer] = Zone.obtainTileArray(zone.tilesX * zone.tilesY);
        for (int y = 0; y < ds1.height; y++, ty++, tx = startTx) {
          int ptr = l + (y * ds1.wallLine);
          for (int x = 0; x < ds1.width; x++, tx++, ptr += ds1.numWalls) {
            DS1.Cell cell = ds1.walls[ptr];
            if (Orientation.isSpecial(cell.orientation)) {
              //DT1.Tile tile = zone.tiles[layer][zone.tileIndex(tx, ty)] = zone.dt1s.get(cell);
              if (ID.POPPADS.contains(cell.id)) {
                if (popPads == null) popPads = new IntMap<>();
                PopPad popPad = popPads.get(cell.id);
                if (popPad == null)
                  popPads.put(cell.id, new PopPad(cell.id, x * DT1.Tile.SUBTILE_SIZE, y * DT1.Tile.SUBTILE_SIZE));
                else
                  popPad.setEnd(
                      x * DT1.Tile.SUBTILE_SIZE + DT1.Tile.SUBTILE_SIZE + preset.PopPad,
                      y * DT1.Tile.SUBTILE_SIZE + DT1.Tile.SUBTILE_SIZE + preset.PopPad);
                zone.putCell(layer, tx, ty, cell);
              } else if (ID.WARPS.contains(cell.id) && cell.subIndex != 1) {
                zone.addWarp(cell.id, tx, ty);
                zone.putCell(layer, tx, ty, cell);
              }
            }

            if ((cell.value & DS1.Cell.FLOOR_UNWALK_MASK) == 0) {
              continue;
            }

            if ((cell.value & DS1.Cell.HIDDEN_MASK) != 0) {
              // This seems like all the special tiles, null usually means marker tile (start pos),
              // non null usually means stuff like side of river, used for ?weather? ?rain drops?
              if (!Orientation.isSpecial(cell.orientation)) {
                // prints all of the debug tiles on side of river (any maybe elsewhere)
                //DT1.Tile tile = dt1s.get(cell);
                //System.out.println(x + ", " + y + " " + tile);
                or(zone, tx, ty, DT1.Tile.FLAG_BLOCK_WALK);
              } else {
                zone.putCell(layer, tx, ty, cell);
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

            DT1.Tile tile = zone.tiles[layer][zone.tileIndex(tx, ty)] = zone.dt1s.get(cell);
            or(zone, tx, ty, tile);

            // Special case, because LEFT_NORTH_CORNER_WALL don't seem to exist, but they contain
            // collision data for RIGHT_NORTH_CORNER_WALL, ORing the data just in case some
            // RIGHT_NORTH_CORNER_WALL actually does anything
            if (cell.orientation == Orientation.RIGHT_NORTH_CORNER_WALL) {
              DT1.Tile sibling = zone.dt1s.get(Orientation.LEFT_NORTH_CORNER_WALL, cell.mainIndex, cell.subIndex);
              or(zone, tx, ty, sibling);
            }
          }
        }
      }
    }

    void copyShadows(Zone zone, int layer, int tx, int ty) {
      final int startTx = tx;
      final int startTy = ty;
      for (int l = 0; l < ds1.numShadows; l++, layer++, ty = startTy) {
        if (zone.tiles[layer] == null) zone.tiles[layer] = Zone.obtainTileArray(zone.tilesX * zone.tilesY);
        for (int y = 0; y < ds1.height; y++, ty++, tx = startTx) {
          int ptr = l + (y * ds1.shadowLine);
          for (int x = 0; x < ds1.width; x++, tx++, ptr += ds1.numShadows) {
            DS1.Cell cell = ds1.shadows[ptr];
            if ((cell.value & DS1.Cell.FLOOR_UNWALK_MASK) == 0) {
              continue;
            }

            if ((cell.value & DS1.Cell.HIDDEN_MASK) != 0) {
              continue;
            }

            zone.tiles[layer][zone.tileIndex(tx, ty)] = zone.dt1s.get(cell);
          }
        }
      }
    }

    void copyObjects(Zone zone, int tx, int ty) {
      final int x = zone.x + (tx * DT1.Tile.SUBTILE_SIZE);
      final int y = zone.y + (ty * DT1.Tile.SUBTILE_SIZE);
      for (int i = 0; i < ds1.numObjects; i++) {
        DS1.Object obj = ds1.objects[i];
        Entity entity = Riiablo.engine.createObject(zone.map, zone, ds1, obj, x + obj.x, y + obj.y);
        zone.addEntity(entity);
      }
    }

    static void or(Zone zone, int tx, int ty, int flags) {
      tx *= DT1.Tile.SUBTILE_SIZE;
      final int startY = ty * DT1.Tile.SUBTILE_SIZE;
      for (int x = 0; x < DT1.Tile.SUBTILE_SIZE; x++, tx++) {
        ty = startY;
        for (int y = 0; y < DT1.Tile.SUBTILE_SIZE; y++, ty++) {
          zone.or(tx, ty, flags);
        }
      }
    }

    static void or(Zone zone, int tx, int ty, DT1.Tile tile) {
      // Note: walkable flags are stored inverted y-axis, this corrects it
      final int startX = tx * DT1.Tile.SUBTILE_SIZE;
      ty = (ty * DT1.Tile.SUBTILE_SIZE) + (DT1.Tile.SUBTILE_SIZE - 1);
      for (int y = 0, t = 0; y < DT1.Tile.SUBTILE_SIZE; y++, ty--) {
        tx = startX;
        for (int x = 0; x < DT1.Tile.SUBTILE_SIZE; x++, tx++, t++) {
          zone.or(tx, ty, tile.flags[t]);
        }
      }
    }

    public void updatePopPads(Bits bits, int x, int y) {
      if (popPads == null) return;
      for (PopPad popPad : popPads.values()) {
        if (popPad.contains(x, y)) {
          bits.set(DT1.Tile.Index.subIndex(popPad.id));
        }
      }
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

      boolean contains(int x, int y) {
        return startX <= x && x < endX
            && startY <= y && y < endY;
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
