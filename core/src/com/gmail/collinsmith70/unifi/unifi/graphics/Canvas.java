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
    pixmap.fillRectangle(
            x, y, paint.getStrokeWidth(), length);
  }

  public void drawHorizontalLine(int x, int y, int length, @NonNull Paint paint) {
    pixmap.setColor(paint.getColor());
    pixmap.fillRectangle(
            x, y, length, paint.getStrokeWidth());
  }

  public void drawRectangle(int x, int y, int width, int height, @NonNull Paint paint) {
    pixmap.setColor(paint.getColor());
    if (paint.getStyle() == Paint.Style.FILL) {
      pixmap.fillRectangle(x, y, width, height);
      return;
    }

    pixmap.fillRectangle(x, y, paint.getStrokeWidth(), height);
    pixmap.fillRectangle(x + width - paint.getStrokeWidth(), y, paint.getStrokeWidth(), height);
    pixmap.fillRectangle(x, y + height - paint.getStrokeWidth(), width, paint.getStrokeWidth());
    pixmap.fillRectangle(x, y, width, paint.getStrokeWidth());

    //drawHorizontalLine(x, y, width, paint);
    //drawHorizontalLine(x, y + height, width, paint);
    //drawVerticalLine(x, y, height, paint);
    //drawVerticalLine(x + width, y, height, paint);
  }

  public Pixmap getPixmap() {
    return pixmap;
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
