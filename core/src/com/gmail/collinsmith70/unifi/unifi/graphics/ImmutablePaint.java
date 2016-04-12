package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;

public class ImmutablePaint extends Paint {

  public ImmutablePaint() {}

  public ImmutablePaint(@NonNull final ImmutablePaint.Builder builder) {
    super.setColor(builder.getColor());
  }

  @Override
  public void setColor(@NonNull final Color color) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  public static ImmutablePaint of(@NonNull final ImmutablePaint.Builder builder) {
    return new ImmutablePaint(builder);
  }

  public static ImmutablePaint of(@NonNull final Color color) {
    return new ImmutablePaint.Builder()
            .setColor(color)
            .build();
  }

  public static class Builder {

    @Nullable
    private Color color;

    @NonNull
    public Builder setColor(@NonNull final Color color) {
      this.color = color;
      return this;
    }

    @NonNull
    public Color getColor() {
      if (color == null) {
        return Color.BLACK;
      }

      return color;
    }

    @NonNull
    public ImmutablePaint build() {
      return new ImmutablePaint(this);
    }

  }

}
