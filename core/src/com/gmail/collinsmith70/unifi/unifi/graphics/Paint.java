package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

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

  @IntRange(from = 1, to = Integer.MAX_VALUE)
  private int strokeWidth;

  public Paint() {
    this.color = Color.BLACK;
    this.style = Style.STROKE;
    this.strokeWidth = 1;
  }

  @NonNull
  public Color getColor() {
    return color;
  }

  public void setColor(@NonNull final Color color) {
    if (color == null) {
      throw new IllegalArgumentException("color cannot be null");
    }

    this.color = color;
  }

  @NonNull
  public Style getStyle() {
    return style;
  }

  public void setStyle(@NonNull final Style style) {
    if (style == null) {
      throw new IllegalArgumentException("style cannot be null");
    }

    this.style = style;
  }

  @IntRange(from = 1, to = Integer.MAX_VALUE)
  public int getStrokeWidth() {
    return strokeWidth;
  }

  public void setStrokeWidth(@IntRange(from = 1, to = Integer.MAX_VALUE) final int strokeWidth) {
    if (strokeWidth < 1) {
      throw new IllegalArgumentException("strokeWidth must be greater than or equal to 1");
    }

    this.strokeWidth = strokeWidth;
  }

}
