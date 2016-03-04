package com.gmail.collinsmith70.unifi.unifi.math;

import android.support.annotation.IntRange;

public class Dimension2D {

  public Dimension2D() {
    this(0, 0);
  }

  public Dimension2D(int width, int height) {
    if (width < 0) {
      throw new IllegalArgumentException("width should be greater than or equal to 0");
    } else if (height < 0) {
      throw new IllegalArgumentException("height should be greater than or equal to 0");
    }

    this.width = width;
    this.height = height;
  }

  public Dimension2D(Dimension2D dimension) {
    assert dimension.getWidth() >= 0;
    assert dimension.getHeight() >= 0;
    this.width = dimension.getWidth();
    this.height = dimension.getHeight();
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int width;

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return width;
  }

  public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
      throw new IllegalArgumentException("width should be greater than or equal to 0");
    }

    this.width = width;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int height;

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return height;
  }

  public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
      throw new IllegalArgumentException("height should be greater than or equal to 0");
    }

    this.height = height;
  }

  public void set(int width, int height) {
    setWidth(width);
    setHeight(height);
  }

  @Override
  public String toString() {
    return String.format("%d x %d", getWidth(), getHeight());
  }
}
