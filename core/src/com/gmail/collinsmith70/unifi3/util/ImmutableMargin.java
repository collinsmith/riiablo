package com.gmail.collinsmith70.unifi3.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public final class ImmutableMargin extends Margin {

  public ImmutableMargin() {
    super();
  }

  public ImmutableMargin(int left, int top, int right, int bottom) {
    super(left, top, right, bottom);
  }

  public ImmutableMargin(@NonNull Margin src) {
    super(src);
  }

  @Override
  public void setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

  @Override
  public void setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

  @Override
  public void setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

  @Override
  public void setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

  @Override
  public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                  @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

  @Override
  public void set(@IntRange(from = 0, to = Integer.MAX_VALUE) int margin) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

  @Override
  public void set(@NonNull Margin src) {
    throw new IllegalStateException("ImmutableMargin is immutable");
  }

}
