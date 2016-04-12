package com.gmail.collinsmith70.unifi.unifi.graphics.drawable.shape;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.unifi.graphics.Paint;

public class RectangularShape extends Shape {

  @Override
  public void draw(@NonNull final Canvas canvas, @NonNull final Paint paint) {
    canvas.drawRectangle(0, 0, getWidth(), getHeight(), paint);
  }

  @Override
  public void draw(@NonNull final Canvas canvas) {
    draw(canvas, Paint.DEFAULT_PAINT);
  }

}
