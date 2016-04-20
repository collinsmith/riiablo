package com.gmail.collinsmith70.unifi3.math;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class Dimension2D {

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int width;

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int height;

  public Dimension2D() {
    this(0, 0);
  }

  public Dimension2D(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                     @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    _setWidth(width);
    _setHeight(height);
  }

  public Dimension2D(@NonNull Dimension2D src) {
    Validate.isTrue(src != null, "src cannot be null");
    _setWidth(src.getWidth());
    _setHeight(src.getHeight());
  }

  private void _setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
    this.width = width;
  }

  private void _setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    _setWidth(width);
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    _setHeight(height);
  }

  public final void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    setWidth(width);
    setHeight(height);
  }

  public final void set(@NonNull Dimension2D src) {
    Validate.isTrue(src != null, "src cannot be null");
    set(src.getWidth(), src.getHeight());
  }

  @Override
  @CallSuper
  public boolean equals(@Nullable Object obj) {
    if (obj == null) {
      return false;
    } else if (obj == this) {
      return true;
    } else if (!(obj instanceof Dimension2D)) {
      return false;
    }

    Dimension2D other = (Dimension2D)obj;
    return this.getWidth() == other.getWidth()
        && this.getHeight() == other.getHeight();
  }

  @Override
  @CallSuper
  public int hashCode() {
    int result = 17;
    result = 31 * result + getWidth();
    result = 31 * result + getHeight();
    return result;
  }

  @NonNull
  public Dimension2D immutableCopy() {
    return new ImmutableDimension2D(this);
  }

  @Override
  public String toString() {
    return String.format("[width = %d, height = %d]", getWidth(), getHeight());
  }

}
