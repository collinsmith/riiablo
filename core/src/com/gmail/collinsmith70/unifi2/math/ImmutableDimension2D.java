package com.gmail.collinsmith70.unifi2.math;

public final class ImmutableDimension2D extends Dimension2D {

  public ImmutableDimension2D(int width, int height) {
    super(width, height);
  }

  public ImmutableDimension2D(Dimension2D dimension) {
    super(dimension);
  }

  @Override
  public void setWidth(int width) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setHeight(int height) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

}
