package com.gmail.collinsmith70.unifi.unifi.shape;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class Shape {

  private float width;
  private float height;

  public abstract void draw(@NonNull final Batch batch);

  public final void resize(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) final float width,
                           @FloatRange(from = 0.0f, to = Float.MAX_VALUE) final float height) {
    setWidth(width);
    setHeight(height);
    onResize(width, height);
  }

  protected void onResize(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) final float width,
                          @FloatRange(from = 0.0f, to = Float.MAX_VALUE) final float height) {}

  @FloatRange(from = 0.0f, to = Float.MAX_VALUE)
  public final float getWidth() {
    return width;
  }

  private void setWidth(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) final float width) {
    if (width < 0.0f) {
      throw new IllegalArgumentException("width must be greater than or equal to 0.0f");
    }

    this.width = width;
  }

  @FloatRange(from = 0.0f, to = Float.MAX_VALUE)
  public final float getHeight() {
    return height;
  }

  private void setHeight(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) final float height) {
    if (height < 0.0f) {
      throw new IllegalArgumentException("height must be greater than or equal to 0.0f");
    }

    this.height = height;
  }

}
