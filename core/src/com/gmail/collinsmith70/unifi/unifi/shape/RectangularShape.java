package com.gmail.collinsmith70.unifi.unifi.shape;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Pixmap;
import com.gmail.collinsmith70.unifi.unifi.math.Boundary;

public class RectangularShape extends Shape {

  @Override
  public void draw(@NonNull final Pixmap pixmap) {
    pixmap.drawRectangle(0, 0, getWidth(), getHeight());
  }

  @NonNull
  public final Boundary getBounds() {
    return new Boundary(0, 0, getWidth(), getHeight());
  }

  @NonNull
  public final Boundary getBounds(@Nullable final Boundary dst) {
    if (dst == null) {
      return getBounds();
    }

    dst.set(0, 0, getWidth(), getHeight());
    return dst;
  }

}
