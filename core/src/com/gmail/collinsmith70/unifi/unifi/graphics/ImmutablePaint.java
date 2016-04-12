package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;

public class ImmutablePaint extends Paint {

  public ImmutablePaint() {}

  public ImmutablePaint(@NonNull final ImmutablePaint.Builder builder) {
    super.setColor(builder.getColor());
    super.setStyle(builder.getStyle());
    super.setStrokeWidth(builder.getStrokeWidth());
  }

  @Override
  public void setColor(@NonNull final Color color) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setStyle(@NonNull final Style style) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  public void setStrokeWidth(@IntRange(from = 1, to = Integer.MAX_VALUE) final int strokeWidth) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  public static ImmutablePaint of(@NonNull final ImmutablePaint.Builder builder) {
    return new ImmutablePaint(builder);
  }

  public static ImmutablePaint of(@Nullable final Color color,
                                  @Nullable final Style style,
                                  @IntRange(from = 1, to = Integer.MAX_VALUE) final int
                                          strokeWidth) {
    return new ImmutablePaint.Builder()
            .setColor(color)
            .setStyle(style)
            .setStrokeWidth(strokeWidth)
            .build();
  }

  public static class Builder {

    @Nullable
    private Color color;

    @Nullable
    private Style style;

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int strokeWidth;

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

    @IntRange(from = 1, to = Integer.MAX_VALUE)
    public int getStrokeWidth() {
      return Math.max(1, strokeWidth);
    }

    @NonNull
    public Builder setStrokeWidth(@IntRange(from = 1, to = Integer.MAX_VALUE) final int
                                              strokeWidth) {
      if (strokeWidth < 1) {
        throw new IllegalArgumentException("strokeWidth must be greater than or equal to 1");
      }

      this.strokeWidth = strokeWidth;
      return this;
    }

    @NonNull
    public ImmutablePaint build() {
      return new ImmutablePaint(this);
    }

  }

}
