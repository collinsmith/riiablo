package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public class ImmutablePaint extends Paint {

  public ImmutablePaint() {}

  public ImmutablePaint(@NonNull final ImmutablePaint.Builder builder) {
    super.setColor(builder.getColor());
    super.setStyle(builder.getStyle());
    super.setBlendingMode(builder.getBlendingMode());
    super.setFilterMode(builder.getFilterMode());
    super.setStrokeWidth(builder.getStrokeWidth());
  }

  @Override
  @NonNull
  public Paint setColor(@NonNull final Color color) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  @NonNull
  public Paint setStyle(@NonNull final Style style) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  @NonNull
  public Paint setBlendingMode(@NonNull Pixmap.Blending blending) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  @NonNull
  public Paint setFilterMode(@NonNull Pixmap.Filter filter) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  @Override
  @NonNull
  public Paint setStrokeWidth(@IntRange(from = 1, to = Integer.MAX_VALUE) final int strokeWidth) {
    throw new UnsupportedOperationException("this class' state is immutable");
  }

  public static ImmutablePaint of(@NonNull final ImmutablePaint.Builder builder) {
    return new ImmutablePaint(builder);
  }

  public static ImmutablePaint of(@Nullable final Color color,
                                  @Nullable final Style style,
                                  @Nullable final Pixmap.Blending blending,
                                  @Nullable Pixmap.Filter filter,
                                  @IntRange(from = 1, to = Integer.MAX_VALUE) final int
                                          strokeWidth) {
    return new ImmutablePaint.Builder()
            .setColor(color)
            .setStyle(style)
            .setBlendingMode(blending)
            .setFilterMode(filter)
            .setStrokeWidth(strokeWidth)
            .build();
  }

  public static class Builder {

    @Nullable
    private Color color;

    @Nullable
    private Style style;

    @Nullable
    private Pixmap.Blending blending;

    @Nullable
    private Pixmap.Filter filter;

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
    public Pixmap.Blending getBlendingMode() {
      if (blending == null) {
        return Pixmap.Blending.SourceOver;
      }

      return blending;
    }

    @NonNull
    public Builder setBlendingMode(@NonNull final Pixmap.Blending blending) {
      this.blending = blending;
      return this;
    }

    @NonNull
    public Pixmap.Filter getFilterMode() {
      if (filter == null) {
        return Pixmap.Filter.NearestNeighbour;
      }

      return filter;
    }

    @NonNull
    public Builder setFilterMode(@NonNull final Pixmap.Filter filter) {
      this.filter = filter;
      return this;
    }

    @NonNull
    public ImmutablePaint build() {
      return new ImmutablePaint(this);
    }

  }

}
