package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.NonNull;

public class Point2D {

  private int x;
  private int y;

  public Point2D() {
    this(0, 0);
  }

  public Point2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point2D(Point2D point) {
    this.x = point.x;
    this.y = point.y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void set(int x, int y) {
    setX(x);
    setY(y);
  }

  public void set(@NonNull final Point2D src) {
    if (src == null) {
      throw new IllegalArgumentException("src point cannot be null");
    }

    set(src.x, src.y);
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", x, y);
  }

}
