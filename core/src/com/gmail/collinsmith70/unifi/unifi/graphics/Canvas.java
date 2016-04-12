package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class Canvas implements Disposable {

  private Pixmap pixmap;

  public Canvas(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {
    this(new Pixmap(width, height, Pixmap.Format.RGBA8888));
  }

  public Canvas(@NonNull final Pixmap pixmap) {
    if (pixmap == null) {
      throw new IllegalArgumentException("pixmap cannot be null");
    }

    this.pixmap = pixmap;
  }

  public void drawLine(int x, int y, int length, boolean vertical, @NonNull Paint paint) {
    if (vertical) {
      drawVerticalLine(x, y, length, paint);
    } else {
      drawHorizontalLine(x, y, length, paint);
    }
  }

  public void drawVerticalLine(int x, int y, int length, @NonNull Paint paint) {
    pixmap.setColor(paint.getColor());
    x -= Math.round(paint.getStrokeWidth() / 2);
    for (int i = 0; i < paint.getStrokeWidth(); i++) {
      pixmap.drawLine(x + i, y, x + i, y + length);
    }
  }

  public void drawHorizontalLine(int x, int y, int length, @NonNull Paint paint) {
    pixmap.setColor(paint.getColor());
    y -= Math.round(paint.getStrokeWidth() / 2);
    for (int i = 0; i < paint.getStrokeWidth(); i++) {
      pixmap.drawLine(x, y + i, x + length, y + i);
    }
  }

  public Texture toTexture() {
    if (pixmap == null) {
      throw new IllegalStateException("Canvas instance has been disposed!");
    }

    return new Texture(pixmap);
  }

  @Override
  public void dispose() {
    this.pixmap.dispose();
    this.pixmap = null;
  }

}
