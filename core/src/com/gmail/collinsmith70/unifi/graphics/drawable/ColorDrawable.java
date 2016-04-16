package com.gmail.collinsmith70.unifi.graphics.drawable;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Paint;

public class ColorDrawable implements Drawable {

  @NonNull
  private Paint paint;

  public ColorDrawable() {
    this(Color.BLACK);
  }

  public ColorDrawable(@NonNull Color color) {
    paint = new Paint();
    paint.setColor(color);
  }

  @NonNull
  public Color getColor() {
    return paint.getColor();
  }

  public void setColor(@NonNull final Color color) {
    paint.setColor(color);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    canvas.fill(paint);
  }

}
