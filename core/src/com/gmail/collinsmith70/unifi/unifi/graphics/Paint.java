package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

public class Paint {

  public static final ImmutablePaint DEFAULT_PAINT = new ImmutablePaint();

  @NonNull
  private Color color;

  public Paint() {
    this.color = Color.BLACK;
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

}
