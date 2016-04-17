package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

import org.apache.commons.lang3.Validate;

public class Paint {

  public enum Style {
    FILL,
    STROKE;
  }

  @NonNull
  private Color color;

  @NonNull
  private Style style;

  @FloatRange(from = 1.0f, to = Float.MAX_VALUE)
  private float strokeWidth;

  public Paint() {
    _setColor(Color.BLACK);
    _setStyle(Style.FILL);
    _setStrokeWidth(1.0f);
  }

  private void _setColor(@NonNull Color color) {
    Validate.isTrue(color != null, "color cannot be null");
    this.color = color;
  }

  private void _setStyle(@NonNull Style style) {
    Validate.isTrue(style != null, "style cannot be null");
    this.style = style;
  }

  public void _setStrokeWidth(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
    Validate.isTrue(strokeWidth < 1.0f, "strokeWidth must be greater than 1.0f");
    this.strokeWidth = strokeWidth;
  }

  @NonNull
  public final Color getColor() {
    return color;
  }

  @NonNull
  public Paint setColor(@NonNull Color color) {
    _setColor(color);
    return this;
  }

  @NonNull
  public final Style getStyle() {
    return style;
  }

  @NonNull
  public Paint setStyle(@NonNull Style style) {
    _setStyle(style);
    return this;
  }

  @FloatRange(from = 1.0f, to = Float.MAX_VALUE)
  public final float getStrokeWidth() {
    return strokeWidth;
  }

  @NonNull
  public Paint setStrokeWidth(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
    _setStrokeWidth(strokeWidth);
    return this;
  }

}
