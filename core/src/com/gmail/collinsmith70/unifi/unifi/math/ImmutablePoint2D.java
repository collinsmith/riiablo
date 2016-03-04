package com.gmail.collinsmith70.unifi.unifi.math;

public final class ImmutablePoint2D extends Point2D {

  public ImmutablePoint2D(int x, int y) {
    super(x, y);
  }

  public ImmutablePoint2D(Point2D point) {
    super(point);
  }

  @Override
  public void setX(int x) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setY(int y) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void set(int x, int y) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

}
