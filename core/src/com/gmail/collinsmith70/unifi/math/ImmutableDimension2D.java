package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public final class ImmutableDimension2D extends Dimension2D {

  public ImmutableDimension2D(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                              @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    super(width, height);
  }

  public ImmutableDimension2D(@NonNull Dimension2D src) {
    super(src);
  }

  @Override
  public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    throw new IllegalStateException("ImmutableDimension2D is immutable");
  }

  @Override
  public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    throw new IllegalStateException("ImmutableDimension2D is immutable");
  }

}
