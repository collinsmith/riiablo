package com.riiablo.map;

import com.google.common.primitives.Ints;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.StreamUtils;
import com.riiablo.util.BufferUtils;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DS1 {
  private static final String TAG = "DS1";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_FILES   = DEBUG && true;
  private static final boolean DEBUG_SIZES   = DEBUG && true;
  private static final boolean DEBUG_LAYERS  = DEBUG && true;
  private static final boolean DEBUG_CELLS   = DEBUG && !true;
  private static final boolean DEBUG_OBJECTS = DEBUG && true;
  private static final boolean DEBUG_GROUPS  = DEBUG && true;
  private static final boolean DEBUG_PATHS   = DEBUG && true;
  private static final boolean DEBUG_STREAM  = DEBUG && true;
  private static final boolean DEBUG_PROPS   = DEBUG && true;

  private static final int ACT_MAX = 5;

  private static final short ORIENTATION_TABLE[] = {
      0x00, 0x01, 0x02, 0x01, 0x02, 0x03, 0x03, 0x05, 0x05, 0x06,
      0x06, 0x07, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
      0x0F, 0x10, 0x11, 0x12, 0x14
  };

  static final int MAX_WALLS   = 4;
  static final int MAX_FLOORS  = 2;
  static final int MAX_SHADOWS = 1;
  static final int MAX_TAGS    = 1;

  private static final int WALL_OFFSET   = 0;
  private static final int ORIENT_OFFSET = WALL_OFFSET   + MAX_WALLS;
  private static final int FLOOR_OFFSET  = ORIENT_OFFSET + MAX_WALLS;
  private static final int SHADOW_OFFSET = FLOOR_OFFSET  + MAX_FLOORS;
  private static final int TAG_OFFSET    = SHADOW_OFFSET + MAX_SHADOWS;
  private static final int MAX_LAYERS    = TAG_OFFSET    + MAX_TAGS;

  int    version;
  int    width;
  int    height;
  int    act;
  int    tagType;

  int    numFiles;
  String files[];

  int    numWalls;
  int    numFloors;
  int    numTags;
  int    numShadows;

  int    numLayers;
  int    layerStream[];

  int    wallLine, wallLen;
  Cell   walls[];

  int    floorLine, floorLen;
  Cell   floors[];

  int    shadowLine, shadowLen;
  Cell   shadows[];

  int    tagLine, tagLen;
  int    tags[];

  int    numObjects;
  Object objects[];

  int    numGroups;
  Group  groups[];

  int    numPaths;
  Path   paths[];

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("version", version)
        .append("width", width)
        .append("height", height)
        .append("act", act)
        .append("tagType", tagType)
        .append("numFiles", numFiles)
        .append("numWalls", numWalls)
        .append("numFloors", numFloors)
        .append("numTags", numTags)
        .append("numShadows", numShadows)
        .append("layerStream", Arrays.toString(Arrays.copyOf(layerStream, numLayers)))
        .append("floorLine", floorLine)
        .append("floorLen", floorLen)
        .append("shadowLine", shadowLine)
        .append("shadowLen", shadowLen)
        .append("tagLine", tagLine)
        .append("tagLen", tagLen)
        .append("wallLine", wallLine)
        .append("wallLen", wallLen)
        .append("numObjects", numObjects)
        .append("numGroups", numGroups)
        .append("numPaths", numPaths)
        .toString();
  }

  public int getAct() {
    return act;
  }

  IntMap<GridPoint2> specials;

  public GridPoint2 find(int id) {
    return specials.get(id);
  }

  private DS1() {}

  public static DS1 loadFromFile(FileHandle handle) {
    return loadFromStream(handle.read());
  }

  public static DS1 loadFromStream(InputStream in) {
    try {
      in = IOUtils.buffer(in, 16384);
      DS1 ds1 = new DS1().read(in);
      if (DEBUG) Gdx.app.debug(TAG, ds1.toString());
      if (ds1.version < 9 || 13 < ds1.version) {
        // FIXME: version 9 <= 13 causes crash here /w 4B remaining, why?
        assert in.available() == 0 : in.available() + "B available!";
      } else if (DEBUG_STREAM && in.available() > 0) {
        Gdx.app.error(TAG, in.available() + "B still available in stream!");
      }

      return ds1;
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read DS1.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  private DS1 read(InputStream in) throws IOException {
    version  = EndianUtils.readSwappedInteger(in);
    width    = EndianUtils.readSwappedInteger(in) + 1;
    height   = EndianUtils.readSwappedInteger(in) + 1;
    act      = version < 8  ? 1 : Math.min(EndianUtils.readSwappedInteger(in) + 1, ACT_MAX);
    tagType  = version < 10 ? 0 : EndianUtils.readSwappedInteger(in);
    numFiles = version < 3  ? 0 : EndianUtils.readSwappedInteger(in);
    files    = numFiles == 0 ? ArrayUtils.EMPTY_STRING_ARRAY : new String[numFiles];
    for (int i = 0; i < numFiles; i++) {
      files[i] = BufferUtils.readString(in);
      if (DEBUG_FILES) Gdx.app.debug(TAG, "file[" + i + "] = " + files[i]);
    }
    if (9 <= version && version <= 13) IOUtils.skipFully(in, 2 * Ints.BYTES);
    if (version < 4) {
      numWalls   = 1;
      numFloors  = 1;
      numTags    = 1;
      numShadows = 1;
    } else {
      numWalls   = EndianUtils.readSwappedInteger(in);
      numFloors  = version < 16 ? 1 : EndianUtils.readSwappedInteger(in);
      numTags    = (tagType == 1 || tagType == 2) ? 1 : 0;
      numShadows = 1;
    }

    if (DEBUG_SIZES) Gdx.app.debug(TAG, String.format("layers: (2 * %d walls) + %d floors + %d shadow + %d tag", numWalls, numFloors, numShadows, numTags));

    layerStream = new int[MAX_LAYERS];
    if (version < 4) {
      numLayers = 5;
      layerStream[0] = WALL_OFFSET;
      layerStream[1] = FLOOR_OFFSET;
      layerStream[2] = ORIENT_OFFSET;
      layerStream[3] = TAG_OFFSET;
      layerStream[4] = SHADOW_OFFSET;
    } else {
      numLayers = 0;
      for (int i = 0; i < numWalls; i++) {
        layerStream[numLayers++] = WALL_OFFSET + i;
        layerStream[numLayers++] = ORIENT_OFFSET + i;
      }
      for (int i = 0; i < numFloors; i++) {
        layerStream[numLayers++] = FLOOR_OFFSET + i;
      }
      if (numShadows > 0) {
        layerStream[numLayers++] = SHADOW_OFFSET;
      }
      if (numTags > 0) {
        layerStream[numLayers++] = TAG_OFFSET;
      }
    }

    floorLine = width * numFloors;
    floorLen  = floorLine * height;
    floors = new Cell[floorLen];
    int floorOffset[] = new int[numFloors];
    for (int i = 0; i < numFloors; i++) {
      floorOffset[i] = i;
    }

    shadowLine = width * numShadows;
    shadowLen  = shadowLine * height;
    shadows = new Cell[shadowLen];
    int shadowOffset[] = new int[numShadows];
    for (int i = 0; i < numShadows; i++) {
      shadowOffset[i] = i;
    }

    tagLine = width * numTags;
    tagLen  = tagLine * height;
    tags = new int[tagLen];
    int tagOffset[] = new int[numTags];
    for (int i = 0; i < numTags; i++) {
      tagOffset[i] = i;
    }

    wallLine = width * numWalls;
    wallLen  = wallLine * height;
    walls = new Cell[wallLen];
    int wallOffset[] = new int[numWalls], orientationOffset[] = new int[numWalls];
    for (int i = 0; i < numWalls; i++) {
      wallOffset[i] = orientationOffset[i] = i;
    }

    if (DEBUG_LAYERS) Gdx.app.debug(TAG, "floorLine=" + floorLine + "; floorLen=" + floorLen);
    if (DEBUG_LAYERS) Gdx.app.debug(TAG, "shadowLine=" + shadowLine + "; shadowLen=" + shadowLen);
    if (DEBUG_LAYERS) Gdx.app.debug(TAG, "tagLine=" + tagLine + "; tagLen=" + tagLen);
    if (DEBUG_LAYERS) Gdx.app.debug(TAG, "wallLine=" + wallLine + "; wallLen=" + wallLen);
    if (DEBUG_LAYERS) Gdx.app.debug(TAG, "layerStream=" + Arrays.toString(Arrays.copyOf(layerStream, numLayers)));

    specials = new IntMap<>();
    for (int l = 0, layer; l < numLayers; l++) {
      layer = layerStream[l];
      if (layer >= MAX_LAYERS) {
        Gdx.app.error(TAG, "Unknown layer: " + layer);
      } else if (layer >= TAG_OFFSET) {
        layer -= TAG_OFFSET;
        tagOffset[layer] = readTags(in, tagOffset[layer]);
      } else if (layer >= SHADOW_OFFSET) {
        layer -= SHADOW_OFFSET;
        shadowOffset[layer] = readShadows(in, shadowOffset[layer]);
      } else if (layer >= FLOOR_OFFSET) {
        layer -= FLOOR_OFFSET;
        floorOffset[layer] = readFloors(in, floorOffset[layer]);
      } else if (layer >= ORIENT_OFFSET) {
        layer -= ORIENT_OFFSET;
        orientationOffset[layer] = readOrientations(in, orientationOffset[layer]);
      } else if (layer >= WALL_OFFSET) {
        layer -= WALL_OFFSET;
        wallOffset[layer] = readWalls(in, wallOffset[layer]);
      } else {
        Gdx.app.error(TAG, "Unknown layer: " + layer);
      }
    }

    if (DEBUG_CELLS) {
      for (int i = 0; i < wallLen; i++)   Gdx.app.debug(TAG, "walls[" + i + "]=" + walls[i].toString());
      for (int i = 0; i < floorLen; i++)  Gdx.app.debug(TAG, "floors[" + i + "]=" + floors[i].toString());
      for (int i = 0; i < shadowLen; i++) Gdx.app.debug(TAG, "shadows[" + i + "]=" + shadows[i].toString());
      Gdx.app.debug(TAG, "tags=" + Arrays.toString(tags));
    }

    readObjects(in);
    readGroups(in);
    readPaths(in);

    width  -= 1;
    height -= 1;
    return this;
  }

  private int readTags(InputStream in, int offset) throws IOException {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        tags[offset] = EndianUtils.readSwappedInteger(in);
        offset += numTags;
      }
    }

    return offset;
  }

  private int readShadows(InputStream in, int offset) throws IOException {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        assert shadows[offset] == null;
        shadows[offset] = new Cell().read(in, Orientation.SHADOW);
        offset += numShadows;
      }
    }

    return offset;
  }

  private int readFloors(InputStream in, int offset) throws IOException {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        assert floors[offset] == null;
        floors[offset] = new Cell().read(in);
        offset += numFloors;
      }
    }

    return offset;
  }

  private int readOrientations(InputStream in, int offset) throws IOException {
    final int layer = offset;
    int orientation;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Cell wall = walls[offset];
        assert wall != null;
        orientation = EndianUtils.readSwappedInteger(in);
        wall.setOrientation(version < 7 ? ORIENTATION_TABLE[orientation] : orientation);
        offset += numWalls;

        if (Orientation.isSpecial(wall.orientation)) {
          specials.put(wall.id, new GridPoint2(x, y));
          if (DEBUG_PROPS) {
            int mainIndex = (wall.value & 0x03F00000) >>> 20;
            int subIndex = (wall.value & 0x0000FF00) >>> 8;
            int first = (wall.value & 0xFC000000) >>> 26;
            int second = (wall.value & 0x000F0000) >>> 16;
            int third = (wall.value & 0x000000FF);
            Gdx.app.debug(TAG, String.format("%d (%2d,%2d) %08X    %2d,%2d,%2d    %02X %02X %X %02X %02X",
                layer, x, y, wall.value,
                mainIndex, wall.orientation, subIndex,
                first, mainIndex, second, subIndex, third));
          }
        }
      }
    }

    return offset;
  }

  private int readWalls(InputStream in, int offset) throws IOException {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        assert walls[offset] == null;
        walls[offset] = new Cell().read(in);
        offset += numWalls;
      }
    }

    return offset;
  }

  private void readObjects(InputStream in) throws IOException {
    final int MAX_X = width  * DT1.Tile.SUBTILE_SIZE;
    final int MAX_Y = height * DT1.Tile.SUBTILE_SIZE;
    numObjects = version < 2 ? 0 : EndianUtils.readSwappedInteger(in);
    objects = numObjects == 0 ? Object.EMPTY_OBJECT_ARRAY : new Object[numObjects];
    for (int i = 0; i < numObjects; i++) {
      try {
        Object object = objects[i] = new Object().read(version, in);
        if (DEBUG_OBJECTS) Gdx.app.debug(TAG, object.toString());
        if (object.x < 0 || MAX_X <= object.x
         || object.y < 0 || MAX_Y <= object.y) {
          Gdx.app.error(TAG, "Object out of DS1 bounds: " + object);
        }
      } catch (Throwable t) {
        // Don't care, invalid object, skip it. Log it for posterity.
        Gdx.app.error(TAG, t.getMessage(), t);
      }
    }
  }

  private void readGroups(InputStream in) throws IOException {
    if (version >= 12 && (tagType == 1 || tagType == 2)) {
      if (version >= 18) IOUtils.skip(in, Ints.BYTES);
      numGroups = EndianUtils.readSwappedInteger(in);
      groups = numGroups == 0 ? Group.EMPTY_GROUP_ARRAY : new Group[numGroups];
      for (int i = 0; i < numGroups; i++) {
        groups[i] = new Group().read(version, in);
        if (DEBUG_GROUPS) Gdx.app.debug(TAG, groups[i].toString());
      }
    } else {
      numGroups = 0;
      groups = Group.EMPTY_GROUP_ARRAY;
    }
  }

  private void readPaths(InputStream in) throws IOException {
    if (version >= 14 && in.available() >= Ints.BYTES) {
      numPaths = EndianUtils.readSwappedInteger(in);
      paths = numPaths == 0 ? Path.EMPTY_PATH_ARRAY : new Path[numPaths];
      for (int i = 0; i < numPaths; i++) {
        Path path = paths[i] = new Path().read(version, objects, in);
        if (DEBUG_PATHS) Gdx.app.debug(TAG, path.toString());
      }
    } else {
      numPaths = 0;
      paths = Path.EMPTY_PATH_ARRAY;
    }
  }

  static class Cell {
    private static final int MAIN_INDEX_OFFSET = 20;
    private static final int MAIN_INDEX_BITS   = 0x3F;

    private static final int SUB_INDEX_OFFSET  = 8;
    private static final int SUB_INDEX_BITS    = 0xFF;

    public static final int MAIN_INDEX_MASK    = MAIN_INDEX_BITS << MAIN_INDEX_OFFSET; // 0x03F00000
    public static final int SUB_INDEX_MASK     = SUB_INDEX_BITS  << SUB_INDEX_OFFSET;  // 0x0000FF00
    public static final int UNWALKABLE_MASK    = 0x00020000;
    public static final int HIDDEN_MASK        = 0x80000000;
    public static final int FLOOR_UNWALK_MASK  = 0x000000FF;

    int id;
    int value;

    short mainIndex;
    short subIndex;
    short orientation;

    Cell read(InputStream in) throws IOException {
      return read(in, Orientation.FLOOR);
    }

    Cell read(InputStream in, int orient) throws IOException {
      value       = EndianUtils.readSwappedInteger(in);
      mainIndex   = (short) ((value >>> MAIN_INDEX_OFFSET) & MAIN_INDEX_BITS);
      subIndex    = (short) ((value >>> SUB_INDEX_OFFSET)  & SUB_INDEX_BITS);
      orientation = (short) orient;
      return updateIndex();
    }

    public int prop4() { return (value >>> 24) & 0xFF; }
    public int prop3() { return (value >>> 16) & 0xFF; }
    public int prop2() { return (value >>>  8) & 0xFF; }
    public int prop1() { return (value >>>  0) & 0xFF; }

    void setOrientation(int orientation) {
      this.orientation = (short) orientation;
      updateIndex();
    }

    Cell updateIndex() {
      id = DT1.Tile.Index.create(orientation, mainIndex, subIndex);
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", String.format("%08x", id))
          .append("value", String.format("%08x", value))
          .append("mainIndex", mainIndex)
          .append("subIndex", subIndex)
          .append("orientation", orientation)
          .build();
    }
  }

  public static class Object {
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final int DYNAMIC_TYPE = 1;
    public static final int STATIC_TYPE  = 2;

    public int  type;
    public int  id;
    public int  x;
    public int  y;
    public int  flags;
    public Path path;

    Object read(int version, InputStream in) throws IOException {
      type  = EndianUtils.readSwappedInteger(in);
      id    = EndianUtils.readSwappedInteger(in);
      x     = EndianUtils.readSwappedInteger(in);
      y     = EndianUtils.readSwappedInteger(in);
      flags = version < 6 ? 0 : EndianUtils.readSwappedInteger(in);
      path  = null;
      return this;
    }

    public String getType() {
      switch (type) {
        case DYNAMIC_TYPE: return "DYNAMIC_TYPE";
        case STATIC_TYPE:  return "STATIC_TYPE";
        default:           return Integer.toString(type);
      }
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this)
          .append("type", getType())
          .append("id", id)
          .append("x", x)
          .append("y", y)
          .append("ds1Flags", String.format("%08x", flags));
      if (path != null) builder.append("path", path);
      return builder.build();
    }
  }

  static class Group {
    public static final Group[] EMPTY_GROUP_ARRAY = new Group[0];

    int x;
    int y;
    int width;
    int height;
    int unk;

    Group read(int version, InputStream in) throws IOException {
      if (in.available() >= Ints.BYTES) x      = EndianUtils.readSwappedInteger(in);
      if (in.available() >= Ints.BYTES) y      = EndianUtils.readSwappedInteger(in);
      if (in.available() >= Ints.BYTES) width  = EndianUtils.readSwappedInteger(in);
      if (in.available() >= Ints.BYTES) height = EndianUtils.readSwappedInteger(in);
      if (version >= 13) {
        if (in.available() >= Ints.BYTES) unk  = EndianUtils.readSwappedInteger(in);
      }
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("x", x)
          .append("y", y)
          .append("width", width)
          .append("height", height)
          .append("unk", String.format("%d (0x%1$08x)", unk))
          .build();
    }
  }

  public static class Path {
    public static final Path[] EMPTY_PATH_ARRAY = new Path[0];

    public int   numPoints;
    public Point points[];

    public int   x;
    public int   y;

    Path read(int version, Object[] objects, InputStream in) throws IOException {
      numPoints = EndianUtils.readSwappedInteger(in);
      points    = new Point[numPoints];
      x         = EndianUtils.readSwappedInteger(in);
      y         = EndianUtils.readSwappedInteger(in);

      Object object = null;
      for (int i = 0; i < objects.length; i++) {
        Object tmp = objects[i];
        if (tmp == null) continue;
        if (tmp.x != x || tmp.y != y) continue;
        if (object != null) Gdx.app.error(TAG, "More than one object is located at path position: " + this);
        object = tmp;
      }

      if (object == null) {
        Gdx.app.error(TAG, "No object associated with path: " + this);
        int skip = (version >= 15 ? 3 : 2) * Ints.BYTES;
        for (int p = 0; p < numPoints; p++) {
          in.skip(skip);
        }

        return this;
      }

      for (int p = 0; p < numPoints; p++) points[p] = new Point().read(version, in);
      object.path = this;
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("x", x)
          .append("y", y)
          .append("numPoints", numPoints)
          .append("points", Arrays.toString(points))
          .build();
    }

    public static class Point {
      public int x;
      public int y;
      public int action;

      Point read(int version, InputStream in) throws IOException {
        x      = EndianUtils.readSwappedInteger(in);
        y      = EndianUtils.readSwappedInteger(in);
        action = version < 15 ? 1 : EndianUtils.readSwappedInteger(in);
        return this;
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("x", x)
            .append("y", y)
            .append("action", action)
            .build();
      }
    }
  }

}
