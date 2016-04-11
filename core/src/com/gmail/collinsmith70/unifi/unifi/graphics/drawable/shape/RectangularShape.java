package com.gmail.collinsmith70.unifi.unifi.graphics.drawable.shape;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Pixmap;
import com.gmail.collinsmith70.unifi.unifi.graphics.Paint;

public class RectangularShape extends Shape {

  @Override
  public void draw(@NonNull final Pixmap pixmap, @NonNull final Paint paint) {
    pixmap.setColor(paint.getColor());
    pixmap.drawRectangle(0, 0, getWidth(), getHeight());
  }

  @Override
  public void draw(@NonNull final Pixmap pixmap) {
    draw(pixmap, Paint.DEFAULT_PAINT);
  }

}
