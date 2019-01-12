package gdx.diablo.codec.util;

import com.badlogic.gdx.utils.Pool;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BBox implements Pool.Poolable {
  public int xMin, xMax;
  public int yMin, yMax;
  public int width, height;

  public BBox() {}

  @Override
  public void reset() {}

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("xMin", xMin)
        .append("xMax", xMax)
        .append("yMin", yMin)
        .append("yMax", yMax)
        .append("width", width)
        .append("height", height)
        .toString();
  }
}
