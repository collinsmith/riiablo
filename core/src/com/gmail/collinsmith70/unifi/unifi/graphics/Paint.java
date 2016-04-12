package com.gmail.collinsmith70.unifi.unifi.graphics;

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

  public Paint() {
    this.color = Color.BLACK;
    this.style = Style.STROKE;
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

}
