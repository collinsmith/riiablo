package com.gmail.collinsmith70.unifi3.math;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Rectangle {

  private int left;
  private int top;
  private int right;
  private int bottom;

  public Rectangle() {
    this(0, 0, 0, 0);
  }

  public Rectangle(int left, int top, int right, int bottom) {
    _setLeft(left);
    _setTop(top);
    _setRight(right);
    _setBottom(bottom);
  }

  public Rectangle(@NonNull Rectangle src) {
    Validate.isTrue(src != null, "src cannot be null");
    _setLeft(src.getLeft());
    _setTop(src.getTop());
    _setRight(src.getRight());
    _setBottom(src.getBottom());
  }

  private void _setLeft(int left) {
    this.left = left;
  }

  private void _setTop(int top) {
    this.top = top;
  }

  private void _setRight(int right) {
    this.right = right;
  }

  private void _setBottom(int bottom) {
    this.bottom = bottom;
  }

  public int getLeft() {
    return left;
  }

  public void setLeft(int left) {
    _setLeft(left);
  }

  public int getTop() {
    return top;
  }

  public void setTop(int top) {
    _setTop(top);
  }

  public int getRight() {
    return right;
  }

  public void setRight(int right) {
    _setRight(right);
  }

  public int getBottom() {
    return bottom;
  }

  public void setBottom(int bottom) {
    _setBottom(bottom);
  }

  public int getX() {
    return getLeft();
  }

  public int getY() {
    return getTop();
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return Math.abs(getLeft() - getRight());
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return Math.abs(getTop() - getBottom());
  }

  public void set(int left, int top, int right, int bottom) {
    setLeft(left);
    setTop(top);
    setRight(right);
    setBottom(bottom);
  }

  public void set(@NonNull Rectangle src) {
    Validate.isTrue(src != null, "src cannot be null");
    set(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  public boolean isEmpty() {
    return getLeft() != getRight() && getTop() != getBottom();
  }

  public final void setEmpty() {
    set(0, 0, 0, 0);
  }

  @Override
  @CallSuper
  public boolean equals(@Nullable Object obj) {
    if (obj == null) {
      return false;
    } else if (obj == this) {
      return true;
    } else if (!(obj instanceof Rectangle)) {
      return false;
    }

    Rectangle other = (Rectangle)obj;
    return this.getLeft() == other.getLeft()
        && this.getTop() == other.getTop()
        && this.getRight() == other.getRight()
        && this.getBottom() == other.getBottom();
  }

  @Override
  @CallSuper
  public int hashCode() {
    int result = 17;
    result = 31 * result + getLeft();
    result = 31 * result + getTop();
    result = 31 * result + getRight();
    result = 31 * result + getBottom();
    return result;
  }

  @NonNull
  public Rectangle immutableCopy() {
    return new ImmutableRectangle(this);
  }

  @Override
  public String toString() {
    return String.format("[left = %d, top = %d, right = %d, bottom = %d, width = %d, height = %d]",
            getLeft(),
            getTop(),
            getRight(),
            getBottom(),
            getWidth(),
            getHeight());
  }
}
