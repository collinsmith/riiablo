package com.gmail.collinsmith70.unifi.graphics.drawable.shape;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Paint;

public class RoundedRectangularShape extends com.gmail.collinsmith70.unifi.unifi.graphics.drawable.shape.RectangularShape {

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
    int tempRadius = Math.min(radius, Math.min(getWidth() / 2, getHeight() / 2));
    canvas.drawRoundRectangle(0, 0, getWidth(), getHeight(), tempRadius, paint);
  }

}
