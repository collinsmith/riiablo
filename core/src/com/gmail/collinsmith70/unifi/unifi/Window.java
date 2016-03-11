package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.math.Boundary;
import com.gmail.collinsmith70.unifi.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.unifi.math.Point2D;

public class Window
        implements WidgetParent {

  private final Dimension2D dimension;

  public Window(int width, int height) {
    this.dimension = new Dimension2D(width, height);
  }

  public void resize(int width, int height) {
    dimension.set(width, height);
  }

  @Nullable
  @Override
  public WidgetParent getParent() {
    return null;
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public int getBottom() {
    return 0;
  }

  @Override
  public void setBottom(final int bottom) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getLeft() {
    return 0;
  }

  @Override
  public void setLeft(final int left) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getRight() {
    return dimension.getWidth();
  }

  @Override
  public void setRight(final int right) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getTop() {
    return dimension.getHeight();
  }

  @Override
  public void setTop(final int top) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @NonNull
  @Override
  public Boundary getBoundary() {
    return new Boundary(getLeft(), getTop(), getRight(), getBottom());
  }

  @NonNull
  @Override
  public Boundary getBoundary(@Nullable final Boundary dst) {
    if (dst == null) {
      return getBoundary();
    }

    dst.set(getLeft(), getTop(), getRight(), getBottom());
    return dst;
  }

  @Override
  public void setBoundary(final int left, final int top, final int right, final int bottom) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public void setBoundary(@NonNull final Boundary src) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @NonNull
  @Override
  public Dimension2D getSize() {
    return new Dimension2D(dimension);
  }

  @NonNull
  @Override
  public Dimension2D getSize(@Nullable final Dimension2D dst) {
    if (dst == null) {
      return getSize();
    }

    dst.set(dimension);
    return dst;
  }

  @Override
  public boolean hasSize() {
    return getWidth() > 0 && getHeight() > 0;
  }

  @Override
  public int getX() {
    return 0;
  }

  @Override
  public void setX(final int x) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  public int getY() {
    return 0;
  }

  @Override
  public void setY(final int y) {
    throw new UnsupportedOperationException("Window does not support this operation");
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return dimension.getWidth();
  }

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return dimension.getHeight();
  }

  @Override
  public boolean contains(final int x, final int y) {
    return getLeft() <= x && x <= getRight()
            && getBottom() <= y && y <= getTop();
  }

  @Override
  public boolean contains(@NonNull final Point2D point) {
    if (point == null) {
      throw new IllegalArgumentException("point cannot be null");
    }

    return contains(point.getX(), point.getY());
  }

}
