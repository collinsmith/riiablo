package com.gmail.collinsmith70.unifi.unifi.math;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Boundary {

  private int bottom;
  private int left;
  private int right;
  private int top;

  public Boundary() {
    this(0, 0, 0, 0);
  }

  public Boundary(int left, int top, int right, int bottom) {
    this.bottom = bottom;
    this.left = left;
    this.right = right;
    this.top = top;
  }

  public Boundary(@NonNull final Boundary boundary) {
    if (boundary == null) {
      throw new IllegalArgumentException("src boundary cannot be null");
    }

    this.bottom = boundary.bottom;
    this.left = boundary.left;
    this.right = boundary.right;
    this.top = boundary.top;
  }

  public int getBottom() {
    return bottom;
  }

  public int getLeft() {
    return left;
  }

  public int getRight() {
    return right;
  }

  public int getTop() {
    return top;
  }

  public void setBottom(int bottom) {
    this.bottom = bottom;
  }

  public void setLeft(int left) {
    this.left = left;
  }

  public void setRight(int right) {
    this.right = right;
  }

  public void setTop(int top) {
    this.top = top;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return Math.abs(left - right);
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return Math.abs(top - bottom);
  }

  public void set(int left, int top, int right, int bottom) {
    setBottom(bottom);
    setLeft(left);
    setRight(right);
    setTop(top);
  }

  public void set(final @NonNull Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src boundary cannot be null");
    }

    set(src.left, src.top, src.right, src.bottom);
  }

  @NonNull
  public Dimension2D getSize() {
    return new Dimension2D(getWidth(), getHeight());
  }

  @NonNull
  public Dimension2D getSize(@Nullable Dimension2D dst) {
    if (dst == null) {
      return getSize();
    }

    dst.set(getWidth(), getHeight());
    return dst;
  }

  public boolean isEmpty() {
    return left != right && top != bottom;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d, %d, %d)", left, top, right, bottom);
  }

}
