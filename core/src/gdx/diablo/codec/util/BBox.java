package gdx.diablo.codec.util;

import com.badlogic.gdx.utils.Pool;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BBox implements Pool.Poolable {
  public int xMin, xMax;
  public int yMin, yMax;
  public int width, height;

  public BBox() {}

  @Override
  public void reset() {
    xMin = xMax = width  = 0;
    yMin = yMax = height = 0;
  }

  public void set(BBox src) {
    xMin   = src.xMin;
    xMax   = src.xMax;
    yMin   = src.yMin;
    yMax   = src.yMax;
    width  = src.width;
    height = src.height;
  }

  public void max(BBox src) {
    if (src.xMin < xMin) xMin = src.xMin;
    if (src.yMin < yMin) yMin = src.yMin;
    if (src.xMax > xMax) xMax = src.xMax;
    if (src.yMax > yMax) yMax = src.yMax;
    width  = xMax - xMin;
    height = yMax - yMin;
  }

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
