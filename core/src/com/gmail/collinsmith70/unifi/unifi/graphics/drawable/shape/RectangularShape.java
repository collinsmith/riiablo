package com.gmail.collinsmith70.unifi.unifi.graphics.drawable.shape;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Pixmap;
import com.gmail.collinsmith70.unifi.unifi.graphics.Paint;

public class RectangularShape extends Shape {

  @Override
  public void draw(@NonNull final Pixmap pixmap, @NonNull final Paint paint) {
    pixmap.setColor(paint.getColor());
    switch (paint.getStyle()) {
      case FILL:
        pixmap.fillRectangle(0, 0, getWidth(), getHeight());
        break;
      case STROKE:
        pixmap.drawRectangle(0, 0, getWidth(), getHeight());
        break;
      default:
        throw new IllegalStateException("paint.getStyle() should be Style.FILL or Style.STROKE");
    }

  }

  @Override
  public void draw(@NonNull final Pixmap pixmap) {
    draw(pixmap, Paint.DEFAULT_PAINT);
  }

}
