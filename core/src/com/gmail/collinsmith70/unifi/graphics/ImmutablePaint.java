package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

public final class ImmutablePaint extends Paint {

  public ImmutablePaint() {
    super();
  }

  public ImmutablePaint(@NonNull Paint src) {
    super(src);
  }

  @NonNull
  @Override
  public Paint setColor(@NonNull Color color) {
    throw new IllegalStateException("ImmutablePaint is immutable");
  }

  @NonNull
  @Override
  public Paint setStyle(@NonNull Style style) {
    throw new IllegalStateException("ImmutablePaint is immutable");
  }

  @NonNull
  @Override
  public Paint setStrokeWidth(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
    throw new IllegalStateException("ImmutablePaint is immutable");
  }

}
