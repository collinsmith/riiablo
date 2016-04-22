package com.gmail.collinsmith70.unifi3.math;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;

public class Point {

  private int x;
  private int y;

  public Point() {
    this(0, 0);
  }

  public Point(int x, int y) {
    _setX(x);
    _setY(y);
  }

  public Point(@NonNull Point src) {
    Validate.isTrue(src != null, "src cannot be null");
    _setX(src.getX());
    _setY(src.getY());
  }

  private void _setX(int x) {
    this.x = x;
  }

  private void _setY(int y) {
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    _setX(x);
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    _setY(y);
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", getX(), getY());
  }

}
