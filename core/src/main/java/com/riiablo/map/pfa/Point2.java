package com.riiablo.map.pfa;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.map.Map;

public class Point2 extends BinaryHeap.Node implements Pool.Poolable {
  public int x;
  public int y;

  //public int index;
  byte clearance;

  static final byte UNVISITED = 0;
  static final byte OPEN      = 1;
  static final byte CLOSED    = 2;

  int searchId = -1;
  Point2 parent;
  byte category;
  float g;

  public Point2() {
    super(0f);
  }

  public Point2(int x, int y) {
    this();
    set(x, y);
  }

  public Point2(Point2 src) {
    this();
    set(src);
  }

  @Override
  public void reset() {
    category = UNVISITED;
  }

  public float g() {
    return g;
  }

  public float f() {
    return super.getValue();
  }

  public Point2 set(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Point2 set(Vector2 src) {
    x = Map.round(src.x);
    y = Map.round(src.y);
    return this;
  }

  public Point2 set(Point2 src) {
    x = src.x;
    y = src.y;
    return this;
  }

  public Point2 add(Point2 p) {
    x += p.x;
    y += p.y;
    return this;
  }

  private static final Point2[][] NEAR = {
      { // 1
        new Point2( 0,  0)
      },
      { // 2
        new Point2(-1, -1),
        new Point2( 0, -1),
        new Point2( 1, -1),

        new Point2(-1,  0),
      //new Point2( 0,  0),
        new Point2( 1,  0),

        new Point2(-1,  1),
        new Point2( 0,  1),
        new Point2( 1,  1),
      },
      { // 3
        new Point2(-1, -2),
        new Point2( 0, -2),
        new Point2( 1, -2),

        new Point2(-2, -1),
      //new Point2(-1, -1),
      //new Point2( 0, -1),
      //new Point2( 1, -1),
        new Point2( 2, -1),

        new Point2(-2,  0),
      //new Point2(-1,  0),
      //new Point2( 0,  0),
      //new Point2( 1,  0),
        new Point2( 2,  0),

        new Point2(-2,  1),
      //new Point2(-1,  1),
      //new Point2( 0,  1),
      //new Point2( 1,  1),
        new Point2( 2,  1),

        new Point2(-1,  2),
        new Point2( 0,  2),
        new Point2( 1,  2),
      }
  };

  public void updateClearance(Map map, int flags) {
    byte i;
size:
    for (i = 0; i < NEAR.length; i++) {
      for (Point2 p : NEAR[i]) {
        if (map.flags(x + p.x, y + p.y) != 0) {
          break size;
        }
      }
    }

    clearance = i;
  }

  @Override
  public int hashCode() {
    return (x * 73856093) ^ (y * 83492791);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null) return false;
    if (!(obj instanceof Point2)) return false;
    return equals((Point2) obj);
  }

  public boolean equals(Point2 other) {
    return x == other.x && y == other.y;
  }

  public boolean equals(int x, int y) {
    return x == this.x && y == this.y;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
