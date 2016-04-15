package com.gmail.collinsmith70.unifi.unifi.graphics.drawable.shape;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.unifi.graphics.Paint;

public class RoundedRectangularShape extends RectangularShape {

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int radius;

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getRadius() {
    return radius;
  }

  public void setRadius(@IntRange(from = 0, to = Integer.MAX_VALUE) final int radius) {
    this.radius = radius;
  }

  @Override
  public void draw(@NonNull final Canvas canvas, @NonNull final Paint paint) {
    canvas.drawRoundRectangle(0, 0, getWidth(), getHeight(), radius, paint);
  }

}
