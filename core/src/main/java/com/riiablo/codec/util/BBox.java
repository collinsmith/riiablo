package com.riiablo.codec.util;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Pool;

public class BBox implements Pool.Poolable {
  public int xMin, xMax;
  public int yMin, yMax;
  public int width, height;

  public BBox() {}

  public BBox(int xMin, int yMin, int xMax, int yMax) {
    this.xMin = xMin;
    this.yMin = yMin;
    this.xMax = xMax;
    this.yMax = yMax;
    width = xMax - xMin;
    height = yMax - yMin;
  }

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

  public BBox setZero() {
    reset();
    return this;
  }

  public BBox prepare() {
    xMin = yMin = Integer.MAX_VALUE;
    xMax = yMax = Integer.MIN_VALUE;
    width = height = Integer.MIN_VALUE;
    // invalid, must call #update() for width,height
    return this;
  }

  public BBox update() {
    width = xMax - xMin;
    height = yMax - yMin;
    return this;
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
