package com.gmail.collinsmith70.unifi3.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Bounds extends com.gmail.collinsmith70.unifi3.math.Rectangle {

  public static final Bounds tmp = new Bounds();

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
  public void set(@NonNull com.gmail.collinsmith70.unifi3.math.Rectangle src) {
    Validate.isTrue(src != null, "src cannot be null");
    set(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @NonNull
  private Bounds _add(@Nullable Bounds dst, @NonNull com.gmail.collinsmith70.unifi3.math.Rectangle rectangle) {
    Validate.isTrue(rectangle != null, rectangle.getClass().getSimpleName() + " cannot be null");
    if (dst == null) {
      dst = new Bounds();
    }

    dst.set(getLeft() + rectangle.getLeft(),
            getTop() - rectangle.getTop(),
            getRight() - rectangle.getRight(),
            getBottom() + rectangle.getBottom());
    return dst;
  }

  @NonNull
  private Bounds _remove(@Nullable Bounds dst, @NonNull com.gmail.collinsmith70.unifi3.math.Rectangle rectangle) {
    Validate.isTrue(rectangle != null, rectangle.getClass().getSimpleName() + " cannot be null");
    if (dst == null) {
      dst = new Bounds();
    }

    dst.set(getLeft() - rectangle.getLeft(),
            getTop() + rectangle.getTop(),
            getRight() + rectangle.getRight(),
            getBottom() - rectangle.getBottom());
    return dst;
  }

  @NonNull
  public Bounds inset(@Nullable Bounds dst, @NonNull Padding padding) {
    return _add(dst, padding);
  }

  @NonNull
  public Bounds inset(@NonNull Padding padding) {
    return inset(null, padding);
  }

  @NonNull
  public Bounds extract(@Nullable Bounds dst, @NonNull Padding padding) {
    return _remove(dst, padding);
  }

  @NonNull
  public Bounds extract(@NonNull Padding padding) {
    return extract(null, padding);
  }

  @NonNull
  public Bounds add(@Nullable Bounds dst, @NonNull Margin margin) {
    return _remove(dst, margin);
  }

  @NonNull
  public Bounds add(@NonNull Margin margin) {
    return add(null, margin);
  }

  @NonNull
  public Bounds remove(@Nullable Bounds dst, @NonNull Margin margin) {
    return _add(dst, margin);
  }

  @NonNull
  public Bounds remove(@NonNull Margin margin) {
    return remove(null, margin);
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
  public com.gmail.collinsmith70.unifi3.math.Rectangle immutableCopy() {
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
