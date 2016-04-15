package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public class Paint {

  public static final ImmutablePaint DEFAULT_PAINT = new ImmutablePaint();

  public enum Style {
    FILL,
    STROKE
  }

  @NonNull
  private Color color;

  @NonNull
  private Style style;

  @NonNull
  private Pixmap.Blending blending;

  @NonNull
  private Pixmap.Filter filter;

  @IntRange(from = 1, to = Integer.MAX_VALUE)
  private int strokeWidth;

  public Paint() {
    this.color = Color.BLACK;
    this.style = Style.STROKE;
    this.blending = Pixmap.Blending.SourceOver;
    this.filter = Pixmap.Filter.NearestNeighbour;
    this.strokeWidth = 1;
  }

  @NonNull
  public Color getColor() {
    return color;
  }

  @NonNull
  public Paint setColor(@NonNull final Color color) {
    if (color == null) {
      throw new IllegalArgumentException("color cannot be null");
    }

    this.color = color;
    return this;
  }

  @NonNull
  public Style getStyle() {
    return style;
  }

  @NonNull
  public Paint setStyle(@NonNull final Style style) {
    if (style == null) {
      throw new IllegalArgumentException("style cannot be null");
    }

    this.style = style;
    return this;
  }

  @IntRange(from = 1, to = Integer.MAX_VALUE)
  public int getStrokeWidth() {
    return strokeWidth;
  }

  @NonNull
  public Paint setStrokeWidth(@IntRange(from = 1, to = Integer.MAX_VALUE) final int strokeWidth) {
    if (strokeWidth < 1) {
      throw new IllegalArgumentException("strokeWidth must be greater than or equal to 1");
    }

    this.strokeWidth = strokeWidth;
    return this;
  }

  @NonNull
  public Pixmap.Blending getBlendingMode() {
    return blending;
  }

  @NonNull
  public Paint setBlendingMode(@NonNull final Pixmap.Blending blending) {
    if (blending == null) {
      throw new IllegalArgumentException("blending cannot be null");
    }

    this.blending = blending;
    return this;
  }

  @NonNull
  public Pixmap.Filter getFilterMode() {
    return filter;
  }

  @NonNull
  public Paint setFilterMode(@NonNull final Pixmap.Filter filter) {
    if (filter == null) {
      throw new IllegalArgumentException("filter cannot be null");
    }

    this.filter = filter;
    return this;
  }

}
