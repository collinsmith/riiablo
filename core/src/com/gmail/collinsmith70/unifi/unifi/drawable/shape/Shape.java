package com.gmail.collinsmith70.unifi.unifi.drawable.shape;

import android.support.annotation.IntRange;

import com.gmail.collinsmith70.unifi.unifi.drawable.Drawable;

public abstract class Shape implements Drawable {

  private int width;
  private int height;

  public final void resize(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {
    setWidth(width);
    setHeight(height);
    onResize(width, height);
  }

  protected void onResize(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                          @IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {}

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public final int getWidth() {
    return width;
  }

  private void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width) {
    if (width < 0) {
      throw new IllegalArgumentException("width must be greater than or equal to 0");
    } else if (getWidth() == width) {
      return;
    }

    this.width = width;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public final int getHeight() {
    return height;
  }

  private void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {
    if (height < 0) {
      throw new IllegalArgumentException("height must be greater than or equal to 0");
    } else if (getHeight() == height) {
      return;
    }

    this.height = height;
  }

}
