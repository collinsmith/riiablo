package com.gmail.collinsmith70.unifi2.graphics.drawable.shape;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi2.graphics.Canvas;
import com.gmail.collinsmith70.unifi2.graphics.Paint;

public class RectangularShape extends Shape {

  @Override
  public void draw(@NonNull final Canvas canvas, @NonNull final Paint paint) {
    canvas.drawRectangle(0, 0, getWidth(), getHeight(), paint);
  }

}
