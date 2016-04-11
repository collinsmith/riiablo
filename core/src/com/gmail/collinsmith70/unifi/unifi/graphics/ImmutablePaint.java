package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

public class ImmutablePaint extends Paint {

  public ImmutablePaint() {}

  public ImmutablePaint(@NonNull ImmutablePaint.Builder builder) {
    super.setColor(builder.getColor());
  }

  @Override
  public void setColor(@NonNull Color color) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  public static ImmutablePaint of(@NonNull ImmutablePaint.Builder builder) {
    return new ImmutablePaint(builder);
  }

  public static ImmutablePaint of(@NonNull Color color) {
    return new ImmutablePaint.Builder()
            .setColor(color)
            .build();
  }

  public static class Builder {

    private Color color;

    public Builder setColor(@NonNull final Color color) {
      this.color = color;
      return this;
    }

    public Color getColor() {
      return color;
    }

    public ImmutablePaint build() {
      return new ImmutablePaint(this);
    }

  }

}
