package com.gmail.collinsmith70.unifi.graphics.drawable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ShapeDrawable implements Drawable {

  @Nullable
  private com.gmail.collinsmith70.unifi.graphics.drawable.shape.Shape shape;

  @NonNull
  private com.gmail.collinsmith70.unifi.graphics.Paint paint;

  public ShapeDrawable() {
    this(null);
  }

  public ShapeDrawable(@NonNull final com.gmail.collinsmith70.unifi.graphics.drawable.shape.Shape shape) {
    this.shape = shape;
    this.paint = new com.gmail.collinsmith70.unifi.graphics.Paint();
  }

  @Nullable
  public com.gmail.collinsmith70.unifi.graphics.drawable.shape.Shape getShape() {
    return shape;
  }

  public void setShape(@Nullable final com.gmail.collinsmith70.unifi.graphics.drawable.shape.Shape shape) {
    this.shape = shape;
  }

  @NonNull
  public com.gmail.collinsmith70.unifi.graphics.Paint getPaint() {
    return paint;
  }

  public void setPaint(@NonNull final com.gmail.collinsmith70.unifi.graphics.Paint paint) {
    this.paint = paint;
  }

  @Override
  public void draw(@NonNull final com.gmail.collinsmith70.unifi.graphics.Canvas canvas) {
    if (shape != null) {
      shape.resize(canvas.getWidth(), canvas.getHeight());
      shape.draw(canvas, paint);
    } else {
      canvas.drawRectangle(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
    }
  }

}
