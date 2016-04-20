package com.gmail.collinsmith70.unifi3.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi3.math.Rectangle;

import org.apache.commons.lang3.Validate;

public class Padding extends Rectangle {

  public Padding() {
    super();
  }

  public Padding(int left, int top, int right, int bottom) {
    super(left, top, right, bottom);
  }

  public Padding(@NonNull Padding src) {
    Validate.isTrue(src != null, "src cannot be null");
    _setLeft(src.getLeft());
    _setTop(src.getTop());
    _setRight(src.getRight());
    _setBottom(src.getBottom());
  }

  private void _setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
    Validate.isTrue(left >= 0, "left must be greater than or equal to 0");
    super.setLeft(left);
  }

  private void _setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    Validate.isTrue(top >= 0, "top must be greater than or equal to 0");
    super.setTop(top);
  }

  private void _setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
    Validate.isTrue(right >= 0, "right must be greater than or equal to 0");
    super.setRight(right);
  }

  private void _setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    Validate.isTrue(bottom >= 0, "bottom must be greater than or equal to 0");
    super.setBottom(bottom);
  }

  protected void onChange() {

  }

  @Override
  public void setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
    if (getLeft() != left) {
      _setLeft(left);
      onChange();
    }
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getTop() {
    return super.getTop();
  }

  @Override
  public void setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    if (getTop() != top) {
      _setTop(top);
      onChange();
    }
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getRight() {
    return super.getRight();
  }

  @Override
  public void setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
    if (getRight() != right) {
      _setRight(right);
      onChange();
    }
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getBottom() {
    return super.getBottom();
  }

  @Override
  public void setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    if (getBottom() != bottom) {
      _setBottom(bottom);
      onChange();
    }
  }

  @Override
  public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    super.set(left, top, right, bottom);
  }

  public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int padding) {
    set(padding, padding, padding, padding);
  }

  public void set(@NonNull Padding src) {
    Validate.isTrue(src != null, "src cannot be null");
    set(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  @Override
  public void set(@NonNull Rectangle src) {
    throw new UnsupportedOperationException("not supported");
  }

  public boolean isEmpty() {
    return getLeft() != 0 || getTop() != 0 || getRight() != 0 || getBottom() != 0;
  }

  @Override
  public int getX() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public int getY() {
    throw new UnsupportedOperationException("not supported");
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    throw new UnsupportedOperationException("not supported");
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    throw new UnsupportedOperationException("not supported");
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
