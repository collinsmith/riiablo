package com.gmail.collinsmith70.unifi.graphics.drawable.shape;

import android.support.annotation.NonNull;

public class RectangularShape extends Shape {

  @Override
  public void draw(@NonNull final com.gmail.collinsmith70.unifi.graphics.Canvas canvas, @NonNull final com.gmail.collinsmith70.unifi.graphics.Paint paint) {
    canvas.drawRectangle(0, 0, getWidth(), getHeight(), paint);
  }

}
