package com.gmail.collinsmith70.unifi.unifi.math;

public final class ImmutableBoundary extends Boundary {

  public ImmutableBoundary(int left, int top, int right, int bottom) {
    super(left, top, right, bottom);
  }

  public ImmutableBoundary(Boundary boundary) {
    super(boundary);
  }

  @Override
  public void setBottom(int bottom) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setLeft(int left) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setRight(int right) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setTop(int top) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

}
