package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;

public class ImmutablePaint extends Paint {

  public ImmutablePaint() {}

  public ImmutablePaint(@NonNull final ImmutablePaint.Builder builder) {
    super.setColor(builder.getColor());
    super.setStyle(builder.getStyle());
  }

  @Override
  public void setColor(@NonNull final Color color) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setStyle(@NonNull final Style style) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  public static ImmutablePaint of(@NonNull final ImmutablePaint.Builder builder) {
    return new ImmutablePaint(builder);
  }

  public static ImmutablePaint of(@Nullable final Color color,
                                  @Nullable final Style style) {
    return new ImmutablePaint.Builder()
            .setColor(color)
            .setStyle(style)
            .build();
  }

  public static class Builder {

    @Nullable
    private Color color;

    @Nullable
    private Style style;

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
    public Builder setStyle(@NonNull final Style style) {
      this.style = style;
      return this;
    }

    @NonNull
    public Style getStyle() {
      if (style == null) {
        return Style.STROKE;
      }

      return style;
    }

    @NonNull
    public ImmutablePaint build() {
      return new ImmutablePaint(this);
    }

  }

}
