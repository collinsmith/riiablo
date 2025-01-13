package com.riiablo.map5;

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

public class Ds1
  extends AbstractReferenceCounted
  implements Disposable
{
  private Ds1() {}

  static final int SUBST_NONE = 0;
  static final int SUBST_FIXED = 1;
  static final int SUBST_RANDOM = 2;

  public static String substToString(int substMethod) {
    switch (substMethod) {
      case SUBST_NONE: return "SUBST_NONE";
      case SUBST_FIXED: return "SUBST_FIXED";
      case SUBST_RANDOM: return "SUBST_RANDOM";
      default: return "UNKNOWN_SUBST(" + substMethod + ")";
    }
  }

  FileHandle handle;
  int version;
  int width;
  int height;
  int act;
  int substMethod;
  final List<String> dependencies = new ArrayList<>(32);

  int numWalls;
  int numFloors;
  int numShadows;
  int numTags;

  int wallRun, wallLen;
  int[] walls;
  int[] types;

  int floorRun, floorLen;
  int[] floors;

  int shadowRun, shadowLen;
  int[] shadows;

  int tagRun, tagLen;
  int[] tags;

  final Array<Spawner> spawners = new Array<>(32);
  final Array<Group> groups = new Array<>(32);
  final Array<Path> paths = new Array<>(32);

  /** cached locations of tiles matching {@link TileType#isSpecial} */
  final IntMap<Vector2> specials = new IntMap<>();

  public static Ds1 obtain(FileHandle handle) {
    Ds1 ds1 = new Ds1();
    ds1.handle = handle;
    return ds1;
  }

  @Override
  protected void deallocate() {
    ReferenceCountUtil.release(handle);
    dependencies.clear();
    Ds1Decoder.free(walls);
    Ds1Decoder.free(types);
    Ds1Decoder.free(floors);
    Ds1Decoder.free(shadows);
    Ds1Decoder.free(tags);
    spawners.clear();
    groups.clear();
    paths.clear();
    specials.clear();
  }

  @Override
  public ReferenceCounted touch(Object hint) {
    return this;
  }

  @Override
  public void dispose() {
    release();
  }

  public FileHandle handle() {
    return handle;
  }

  public int version() {
    return version;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("handle", handle)
        .append("version", version)
        .append("width", width)
        .append("height", height)
        .append("act", act)
        .append("substMethod", substMethod)
        .append("dependencies", dependencies)
        .append("numWalls", numWalls)
        .append("numFloors", numFloors)
        .append("numShadows", numShadows)
        .append("numTags", numTags)
        .append("spawners", spawners)
        .append("groups", groups)
        .append("paths", paths)
        .toString();
  }

  static final class Spawner {
    int type;
    int id;
    final Vector2 position = new Vector2();
    int flags;

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
    final Array<Node> nodes = new Array<>(32);
    final Vector2 position = new Vector2();

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("nodes", nodes)
          .append("position", position)
          .toString();
    }

    static final class Node extends Vector2 {
      int action;

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("action", action)
            .toString();
      }
    }
  }
}
