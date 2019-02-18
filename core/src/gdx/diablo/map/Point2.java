package gdx.diablo.map;

import com.badlogic.gdx.math.Vector3;

public class Point2 extends BinaryHeap.Node {
  final int x;
  final int y;
  final int hash;

  Point2(int x, int y, float cost) {
    super(cost);
    this.x = x;
    this.y = y;
    this.hash = hash();
  }

  Point2(Vector3 src) {
    this((int) src.x, (int) src.y, 0);
  }

  private int hash() {
    return 31 * x + y;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    if (!(obj instanceof Point2)) return false;
    Point2 other = (Point2) obj;
    return x == other.x && y == other.y;
  }

  public boolean equals(int x, int y) {
    return this.x == x && this.y == y;
  }

  public static float dst(Point2 src, Point2 dst) {
    final float dx = dst.x - src.x;
    final float dy = dst.y - src.y;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }
}
