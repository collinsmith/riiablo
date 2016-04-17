package com.gmail.collinsmith70.unifi.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.math.Rectangle;

import org.apache.commons.lang3.Validate;

public class Bounds extends Rectangle {

  public Bounds() {
    super();
  }

  public Bounds(int left, int top, int right, int bottom) {
    super(left, top, right, bottom);
  }

  public Bounds(@NonNull Bounds src) {
    Validate.isTrue(src != null, "src cannot be null");
    _setLeft(src.getLeft());
    _setTop(src.getTop());
    _setRight(src.getRight());
    _setBottom(src.getBottom());
  }

  private void _setLeft(int left) {
    super.setLeft(left);
    if (getRight() < left) {
      _setRight(left);
    }
  }

  private void _setTop(int top) {
    super.setTop(top);
    if (getBottom() > top) {
      _setBottom(top);
    }
  }

  private void _setRight(int right) {
    super.setRight(right);
    if (getLeft() > right) {
      _setLeft(right);
    }
  }

  private void _setBottom(int bottom) {
    super.setBottom(bottom);
    if (getTop() < bottom) {
      _setTop(bottom);
    }
  }

  protected void onChange() {

  }

  @Override
  public void setLeft(int left) {
    if (getLeft() != left) {
      _setLeft(left);
      onChange();
    }
  }

  @Override
  public int getTop() {
    return super.getTop();
  }

  @Override
  public void setTop(int top) {
    if (getTop() != top) {
      _setTop(top);
      onChange();
    }
  }

  @Override
  public int getRight() {
    return super.getRight();
  }

  @Override
  public void setRight(int right) {
    if (getRight() != right) {
      _setRight(right);
      onChange();
    }
  }

  @Override
  public int getBottom() {
    return super.getBottom();
  }

  @Override
  public void setBottom(int bottom) {
    if (getBottom() != bottom) {
      _setBottom(bottom);
      onChange();
    }
  }

  @Override
  public void set(int left,
                  int top,
                  int right,
                  int bottom) {
    Validate.isTrue(right >= left, "right must be greater than or equal to left");
    Validate.isTrue(top >= bottom, "top must be greater than or equal to bottom");
    super.set(left, top, right, bottom);
  }

  @Override
  public void set(@NonNull Rectangle src) {
    Validate.isTrue(src != null, "src cannot be null");
    set(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @NonNull
  public Bounds inset(@Nullable Bounds dst, @NonNull Padding padding) {
    Validate.isTrue(padding != null, "padding cannot be null");
    if (dst == null) {
      dst = new Bounds();
    }

    dst.set(getLeft() + padding.getLeft(),
            getTop() - padding.getTop(),
            getRight() - padding.getRight(),
            getBottom() + padding.getBottom());
    return dst;
  }

  @NonNull
  public Bounds inset(@NonNull Padding padding) {
    return inset(null, padding);
  }

  @NonNull
  public Bounds remove(@Nullable Bounds dst, @NonNull Padding padding) {
    Validate.isTrue(padding != null, "padding cannot be null");
    if (dst == null) {
      dst = new Bounds();
    }

    dst.set(getLeft() - padding.getLeft(),
            getTop() + padding.getTop(),
            getRight() + padding.getRight(),
            getBottom() - padding.getBottom());
    return dst;
  }

  @NonNull
  public Bounds remove(@NonNull Padding padding) {
    return inset(null, padding);
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return getRight() - getLeft();
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return getTop() - getBottom();
  }

  @NonNull
  @Override
  public Rectangle immutableCopy() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public String toString() {
    return String.format("[left = %d, top = %d, right = %d, bottom = %d]",
            getLeft(),
            getTop(),
            getRight(),
            getBottom());
  }

}
