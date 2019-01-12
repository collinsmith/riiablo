package gdx.diablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import gdx.diablo.util.BufferUtils;

public class DS1 {
  private static final String TAG = "DS1";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_FILES   = DEBUG && false;
  private static final boolean DEBUG_CELLS   = DEBUG && false;
  private static final boolean DEBUG_OBJECTS = DEBUG && false;
  private static final boolean DEBUG_GROUPS  = DEBUG && true;
  private static final boolean DEBUG_PATHS   = DEBUG && false;

  private static final int FLOOR_MAX_LAYER  = 2;
  private static final int SHADOW_MAX_LAYER = 1;
  private static final int TAG_MAX_LAYER    = 1;
  private static final int WALL_MAX_LAYER   = 4;

  private static final int ACT_MAX = 5;

  private static final short DIR_TABLE[] = {
      0x00, 0x01, 0x02, 0x01, 0x02, 0x03, 0x03, 0x05, 0x05, 0x06,
      0x06, 0x07, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
      0x0F, 0x10, 0x11, 0x12, 0x14
  };

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
  Wall   walls[];

  int    floorLine, floorLen;
  Floor  floors[];

  int    shadowLine, shadowLen;
  Shadow shadows[];

  int    tagLine, tagLen;
  Tag    tags[];

  int    numObjects;
  Object objects[];

  int    numGroups;
  Group  groups[];

  int    numPaths;
  Path   paths[];

  int    tileWidth  = 160;
  int    tileHeight = 80;

  private DS1() {}

  public int getVersion() {
    return version;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getAct() {
    return act;
  }

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

  private DS1 read(InputStream in) throws IOException {
    version  = EndianUtils.readSwappedInteger(in);
    width    = EndianUtils.readSwappedInteger(in) + 1;
    height   = EndianUtils.readSwappedInteger(in) + 1;
    act      = version < 8  ? 1 : Math.min(EndianUtils.readSwappedInteger(in) + 1, ACT_MAX);
    tagType  = version < 10 ? 0 : EndianUtils.readSwappedInteger(in);
    numFiles = version < 3  ? 0 : EndianUtils.readSwappedInteger(in);
    files    = new String[numFiles];
    for (int i = 0; i < numFiles; i++) {
      files[i] = BufferUtils.readString(in);
      if (DEBUG_FILES) Gdx.app.debug(TAG, "file[" + i + "] = " + files[i]);
    }
    if (9 <= version && version <= 13) IOUtils.skipFully(in, 2);
    if (version >= 4) {
      numWalls   = EndianUtils.readSwappedInteger(in);
      numFloors  = version < 16 ? 1 : EndianUtils.readSwappedInteger(in);
      numTags    = (version >= 10 && (tagType == 1 || tagType == 2)) ? 1 : 0;
      numShadows = 1;
    } else {
      numWalls   = 1;
      numFloors  = 1;
      numTags    = 1;
      numShadows = 1;
    }

    layerStream = new int[14];
    if (version < 4) {
      numLayers = 5;
      layerStream[0] = 1;  // wall 1
      layerStream[1] = 9;  // floor 1
      layerStream[2] = 5;  // orientation 1
      layerStream[3] = 12; // tag
      layerStream[4] = 11; // shadow
    } else {
      numLayers = 0;
      for (int i = 0; i < numWalls; i++) {
        layerStream[numLayers++] = 1 + i; // wall i
        layerStream[numLayers++] = 5 + i; // orientation i
      }
      for (int i = 0; i < numFloors; i++) {
        layerStream[numLayers++] = 9 + i; // floor i
      }
      if (numShadows > 0)
        layerStream[numLayers++] = 11;    // shadow
      if (numTags > 0)
        layerStream[numLayers++] = 12;    // tag
    }

    floorLine = width * numFloors;
    floorLen  = floorLine * height;
    floors = new Floor[floorLen];
    int floorOffset[] = new int[FLOOR_MAX_LAYER];
    for (int i = 0; i < numFloors; i++) {
      floorOffset[i] = i;
    }

    shadowLine = width * numShadows;
    shadowLen  = shadowLine * height;
    shadows = new Shadow[shadowLen];
    int shadowOffset[] = new int[SHADOW_MAX_LAYER];
    for (int i = 0; i < numShadows; i++) {
      shadowOffset[i] = i;
    }

    tagLine = width * numTags;
    tagLen  = tagLine * height;
    tags = new Tag[tagLen];
    int tagOffset[] = new int[TAG_MAX_LAYER];
    for (int i = 0; i < numTags; i++) {
      tagOffset[i] = i;
    }

    wallLine = width * numWalls;
    wallLen  = wallLine * height;
    walls = new Wall[wallLen];
    int wallOffset[] = new int[WALL_MAX_LAYER];
    for (int i = 0; i < numWalls; i++) {
      wallOffset[i] = i;
    }

    int orientationOffset[] = Arrays.copyOf(wallOffset, WALL_MAX_LAYER);

    int p, layer, id;
    for (int l = 0; l < numLayers; l++) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          switch (layer = layerStream[l]) {
            // wall
            case 1: case 2: case 3: case 4:
              p = layer - 1;
              id = wallOffset[p];
              assert walls[id] == null;
              walls[id] = new Wall();
              walls[id].read(in);
              wallOffset[p] += numWalls;
              break;

            // orientation
            case 5: case 6: case 7: case 8:
              // TODO: This looks like it could be a single unsigned byte, but I am skipping next 3 bytes
              p = layer - 5;
              id = orientationOffset[p];
              assert walls[id] != null;
              short i = (short) EndianUtils.readSwappedInteger(in);
              walls[id].orientation = version < 7 ? DIR_TABLE[i] : i;
              orientationOffset[p] += numWalls;
              break;

            // floor
            case 9: case 10:
              p = layer - 9;
              id = floorOffset[p];
              assert floors[id] == null;
              floors[id] = new Floor();
              floors[id].read(in);
              floorOffset[p] += numFloors;
              break;

            // shadow
            case 11:
              p = layer - 11;
              id = shadowOffset[p];
              assert shadows[id] == null;
              shadows[id] = new Shadow();
              shadows[id].read(in);
              shadowOffset[p] += numShadows;
              break;

            // tag
            case 12:
              p = layer - 12;
              id = tagOffset[p];
              assert tags[id] == null;
              tags[id] = new Tag();
              tags[id].num = EndianUtils.readSwappedInteger(in);
              tagOffset[p] += numTags;
              break;
          }
        }
      }
    }

    if (DEBUG_CELLS) {
      for (int i = 0; i < numFloors; i++)  Gdx.app.debug(TAG, floors[i].toString());
      for (int i = 0; i < numShadows; i++) Gdx.app.debug(TAG, shadows[i].toString());
      for (int i = 0; i < numTags; i++)    Gdx.app.debug(TAG, tags[i].toString());
      for (int i = 0; i < numWalls; i++)   Gdx.app.debug(TAG, walls[i].toString());
    }

    int maxSubtileWidth  = width  * 5;
    int maxSubtileHeight = height * 5;

    numObjects = version < 2 ? 0 : EndianUtils.readSwappedInteger(in);
    objects = new Object[numObjects];
    int lastValidObject = 0;
    for (int i = 0; i < numObjects; i++) {
      try {
        objects[lastValidObject] = new Object(version, in, maxSubtileWidth, maxSubtileHeight);
        if (DEBUG_OBJECTS) Gdx.app.debug(TAG, objects[lastValidObject].toString());
        lastValidObject++;
      } catch (Throwable t) {
        // Don't care, invalid object, skip it. Log it for posterity.
        Gdx.app.error(TAG, t.getMessage(), t);
      }
    }

    numObjects = lastValidObject;

    numGroups = 0;
    if (version >= 12 && (tagType == 1 || tagType == 2)) {
      if (version >= 18) {
        IOUtils.skipFully(in, 4);
      }

      numGroups = EndianUtils.readSwappedInteger(in);
    }

    groups = new Group[numGroups];
    for (int i = 0; i < numGroups; i++) {
      groups[i] = new Group(version, in);
      if (DEBUG_GROUPS) Gdx.app.debug(TAG, groups[i].toString());
    }

    numPaths = 0;
    if (version >= 14) {
      if (in.available() >= 4) {
        numPaths = EndianUtils.readSwappedInteger(in);
      }
    }

    paths = new Path[numPaths];
    for (int i = 0; i < numPaths; i++) {
      paths[i] = new Path(version, numObjects, objects, in);
      if (DEBUG_PATHS) Gdx.app.debug(TAG, paths[i].toString());
    }

    return this;
  }

  public static DS1 loadFromStream(InputStream in) {
    try {
      DS1 ds1 = new DS1().read(in);
      if (DEBUG) Gdx.app.debug(TAG, ds1.toString());
      assert in.available() == 0;
      return ds1;
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read DS1.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  static class CELL_F_S_W {
    short prop1;
    short prop2;
    short prop3;
    short prop4;

    int blockIndex;

    void read(InputStream in) throws IOException {
      prop1 = (short) in.read();
      prop2 = (short) in.read();
      prop3 = (short) in.read();
      prop4 = (short) in.read();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("prop1", prop1)
          .append("prop2", prop2)
          .append("prop3", prop3)
          .append("prop4", prop4)
          .append("blockIndex", blockIndex)
          .build();
    }
  }
  static class Floor  extends CELL_F_S_W {}
  static class Shadow extends CELL_F_S_W {}
  static class Wall   extends CELL_F_S_W {
    short orientation;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .appendSuper(super.toString())
          .append("orientation", orientation)
          .build();
    }
  }
  static class Tag {
    int num;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("num", num)
          .build();
    }
  }
  static class Object {
    int type;
    int id;
    int x;
    int y;
    int dslFlags;

    int descIdx;
    int flags;

    short frameDelta;

    public Object(int version, InputStream in, int maxSubtileWidth, int maxSubtileHeight) throws IOException {
      type     = EndianUtils.readSwappedInteger(in);
      id       = EndianUtils.readSwappedInteger(in);
      x        = EndianUtils.readSwappedInteger(in);
      y        = EndianUtils.readSwappedInteger(in);
      dslFlags = version <= 5 ? 0 : EndianUtils.readSwappedInteger(in);
      if (x >= 0 && x < maxSubtileWidth && y >= 0 && y < maxSubtileHeight) {
        descIdx    = -1;
        flags      = 0;
        frameDelta = (short) MathUtils.random(255);
      } else {
        throw new GdxRuntimeException("Object integrity check failed.");
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("type", type)
          .append("id", id)
          .append("x", x)
          .append("y", y)
          .append("dslFlags", "0x" + Integer.toHexString(dslFlags))
          .append("descIdx", descIdx)
          .append("flags", "0x" + Integer.toHexString(flags))
          .build();
    }
  }
  static class Group {
    int tileX;
    int tileY;
    int width;
    int height;
    int unk;

    Group(int version, InputStream in) throws IOException {
      if (in.available() >= 4) tileX  = EndianUtils.readSwappedInteger(in);
      if (in.available() >= 4) tileY  = EndianUtils.readSwappedInteger(in);
      if (in.available() >= 4) width  = EndianUtils.readSwappedInteger(in);
      if (in.available() >= 4) height = EndianUtils.readSwappedInteger(in);
      if (version >= 13) {
        if (in.available() >= 4) unk  = EndianUtils.readSwappedInteger(in);
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("tileX", tileX)
          .append("tileY", tileY)
          .append("width", width)
          .append("height", height)
          .append("unk", unk)
          .build();
    }
  }
  static class Path {
    int    x;
    int    y;
    int    numPoints;
    Point  points[];
    Object ref;

    // TODO: I think this might be coded wrong. Path *shouldn't* need to rely on parent Object as far
    //       as I can tell. If that's the case, Path should extend object. I'll learn more when I
    //       eventually find a DS1 with a conflict.
    Path(int version, int numObjects, Object[] objects, InputStream in) throws IOException {
      numPoints = EndianUtils.readSwappedInteger(in);
      x         = EndianUtils.readSwappedInteger(in);
      y         = EndianUtils.readSwappedInteger(in);
      points    = new Point[numPoints];

      int o = 0, last_o = 0, duplicates = 0;
      while (o < numObjects) {
        Object object = objects[o];
        if (object.x == x && object.y == y) {
          last_o = o;
          if (duplicates++ >= 2) break;
        }
        o++;
      }

      // there are a least 2 objects at the same coordinates
      if (duplicates >= 2) {
        Gdx.app.error(TAG, "At least 2 objects are located at the same coordinates for some path datas.");
        /*
        No longer needed as paths are their own struct and don't rely on Object
        for (int i = 0; i < numObjects; i++) {
          Object  object = objects[i];
          if (object.x == x && object.y == y && object.numPoints != 0) {
            // TODO: Garbage collect (zero) path data?
            object.numPoints = 0;
          }
        }
        */

        int skip = version >= 15 ? 12 : 8;
        for (int p = 0; p < numPoints; p++) {
          in.skip(skip);
        }
      } else {
        o = last_o;
        if (o > numObjects) {
          int skip = version >= 15 ? 12 : 8;
          for (int p = 0; p < numPoints; p++) {
            in.skip(skip);
          }
        } else {
          ref = objects[o];
          for (int p = 0; p < numPoints; p++) {
            points[p] = new Point(version, in);
          }
        }
      }
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

    static class Point {
      int x;
      int y;
      int action;
      int flags;

      Point(int version, InputStream in) throws IOException {
        x      = EndianUtils.readSwappedInteger(in);
        y      = EndianUtils.readSwappedInteger(in);
        action = version < 15 ? 1 : EndianUtils.readSwappedInteger(in);
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("x", x)
            .append("y", y)
            .append("action", action)
            .append("flags", "0x" + Integer.toHexString(flags))
            .build();
      }
    }
  }
}
