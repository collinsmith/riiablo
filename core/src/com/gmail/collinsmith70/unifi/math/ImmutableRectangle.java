package com.gmail.collinsmith70.unifi.math;

import android.support.annotation.NonNull;

public final class ImmutableRectangle extends Rectangle {

  public ImmutableRectangle(int left, int top, int right, int bottom) {
    super(left, top, right, bottom);
  }

  public ImmutableRectangle(@NonNull Rectangle src) {
    super(src);
  }

  @Override
  public void setLeft(int left) {
    throw new IllegalStateException("ImmutableRectangle is immutable");
  }

  @Override
  public void setTop(int top) {
    throw new IllegalStateException("ImmutableRectangle is immutable");
  }

  @Override
  public void setRight(int right) {
    throw new IllegalStateException("ImmutableRectangle is immutable");
  }

  @Override
  public void setBottom(int bottom) {
    throw new IllegalStateException("ImmutableRectangle is immutable");
  }

}
