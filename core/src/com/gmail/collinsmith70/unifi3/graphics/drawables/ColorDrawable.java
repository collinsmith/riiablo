package com.gmail.collinsmith70.unifi3.graphics.drawables;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

import org.apache.commons.lang3.Validate;

public class ColorDrawable extends com.gmail.collinsmith70.unifi3.graphics.AbstractDrawable {

  private static final com.gmail.collinsmith70.unifi3.graphics.Paint paint = new com.gmail.collinsmith70.unifi3.graphics.Paint();

  private Color color;

  public ColorDrawable() {
    this(Color.WHITE);
  }

  public ColorDrawable(@NonNull Color color) {
    setColor(color);
  }

  public Color getColor() {
    return color;
  }

  public void setColor(@NonNull Color color) {
    Validate.isTrue(color != null, "color cannot be null");
    this.color = color;
    //invalidate();
  }

  @Override
  public void draw(@NonNull com.gmail.collinsmith70.unifi3.graphics.Canvas canvas) {
    paint.setColor(color);
    canvas.drawPaint(paint);
  }

}
