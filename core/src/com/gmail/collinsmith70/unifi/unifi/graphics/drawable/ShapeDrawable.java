package com.gmail.collinsmith70.unifi.unifi.graphics.drawable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.unifi.graphics.Paint;
import com.gmail.collinsmith70.unifi.unifi.graphics.drawable.shape.Shape;

public class ShapeDrawable implements Drawable {

  @Nullable
  private Shape shape;

  @NonNull
  private Paint paint;

  public ShapeDrawable() {
    this(null);
  }

  public ShapeDrawable(@NonNull final Shape shape) {
    this.shape = shape;
    this.paint = new Paint();
  }

  @Nullable
  public Shape getShape() {
    return shape;
  }

  public void setShape(@Nullable final Shape shape) {
    this.shape = shape;
  }

  @NonNull
  public Paint getPaint() {
    return paint;
  }

  public void setPaint(@NonNull final Paint paint) {
    this.paint = paint;
  }

  @Override
  public void draw(@NonNull final Canvas canvas) {
    if (shape != null) {
      shape.draw(canvas, paint);
    } else {
      canvas.drawRectangle(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
    }
  }

}
