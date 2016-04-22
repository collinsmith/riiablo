package com.gmail.collinsmith70.unifi3.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi3.math.Dimension2D;

public class Measured extends Dimension2D {

  private final ImmutableMeasured immutableView;

  public Measured() {
    super();
    this.immutableView = this.new ImmutableMeasured();
  }

  public Measured(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    super(width, height);
    this.immutableView = this.new ImmutableMeasured();
  }

  public Measured(@NonNull Dimension2D src) {
    super(src);
    this.immutableView = this.new ImmutableMeasured();
  }

  public ImmutableMeasured immutableView() {
    return immutableView;
  }

  public final class ImmutableMeasured extends Dimension2D {

    @Override
    public int getWidth() {
      return Measured.this.getWidth();
    }

    @Override
    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
      throw new UnsupportedOperationException("ImmutableMeasured is immutable");
    }

    @Override
    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getHeight() {
      return Measured.this.getHeight();
    }

    @Override
    public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
      throw new UnsupportedOperationException("ImmutableMeasured is immutable");
    }

  }

}
