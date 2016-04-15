package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.unifi.math.Boundary;

public class Canvas implements Disposable {

  private static final Pixmap.Format PIXMAP_FORMAT = Pixmap.Format.RGBA8888;

  @NonNull
  private Pixmap pixmap;

  @NonNull
  private Boundary clip;

  @Nullable
  private Pixmap clippedPixmap;

  public Canvas(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {
    this(new Pixmap(width, height, PIXMAP_FORMAT));
  }

  public Canvas(@NonNull final Pixmap pixmap) {
    if (pixmap == null) {
      throw new IllegalArgumentException("pixmap cannot be null");
    }

    this.pixmap = pixmap;
    this.clip = new Boundary(0, 0, getWidth(), getHeight());
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return pixmap.getWidth();
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return pixmap.getHeight();
  }

  @NonNull
  public Boundary getClipBounds() {
    return new Boundary(clip);
  }

  @NonNull
  public Boundary getClipBounds(@Nullable final Boundary dst) {
    if (dst == null) {
      return getClipBounds();
    }

    dst.set(clip);
    return dst;
  }

  public void clipRectangle(final int left, final int top, final int right, final int bottom) {
    if (right < left) {
      throw new IllegalArgumentException("left should be less than or equal to right");
    } else if (bottom < top) {
      throw new IllegalArgumentException("top should be less than or equal to bottom");
    }

    clip.set(left, top, right, bottom);
  }

  public void clipRectangle(@NonNull final Boundary src) {
    clipRectangle(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  private void prepare(@NonNull final Paint paint) {
    if (clippedPixmap != null) {
      throw new IllegalStateException("prepare called without calling finish");
    }

    this.clippedPixmap = new Pixmap(clip.getWidth(), clip.getHeight(), PIXMAP_FORMAT);
    Gdx.gl.glLineWidth(paint.getStrokeWidth());
    clippedPixmap.setColor(paint.getColor());
    Pixmap.setBlending(paint.getBlendingMode());
    Pixmap.setFilter(paint.getFilterMode());
  }

  private void finish() {
    if (clippedPixmap == null) {
      throw new IllegalStateException("finish called without calling prepare");
    }

    pixmap.drawPixmap(clippedPixmap, clip.getLeft(), clip.getTop());
    clippedPixmap.dispose();
    this.clippedPixmap = null;
  }

  public void drawLine(final int x,
                       final int y,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) final int length,
                       final boolean vertical,
                       @NonNull final Paint paint) {
    if (vertical) {
      drawVerticalLine(x, y, length, paint);
    } else {
      drawHorizontalLine(x, y, length, paint);
    }
  }

  public void drawVerticalLine(final int x,
                               final int y,
                               @IntRange(from = 0, to = Integer.MAX_VALUE) final int length,
                               @NonNull final Paint paint) {
    prepare(paint);
    clippedPixmap.fillRectangle(x - clip.getLeft(), y - clip.getTop(), paint.getStrokeWidth(), length);
    finish();
  }

  public void drawHorizontalLine(final int x, final int y, final int length,
                                 @NonNull final Paint paint) {
    prepare(paint);
    clippedPixmap.fillRectangle(x - clip.getLeft(), y - clip.getTop(), length, paint.getStrokeWidth());
    finish();
  }

  public void drawRectangle(final int x,
                            final int y,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) final int height,
                            @NonNull final Paint paint) {
    prepare(paint);
    if (paint.getStyle() == Paint.Style.FILL) {
      clippedPixmap.fillRectangle(x - clip.getLeft(), y, width, height);
      finish();
      return;
    }

    clippedPixmap.fillRectangle(x - clip.getLeft(), y - clip.getTop(), paint.getStrokeWidth(), height);
    clippedPixmap.fillRectangle(x + width - paint.getStrokeWidth() - clip.getLeft(), y - clip.getTop(), paint.getStrokeWidth(), height);
    clippedPixmap.fillRectangle(x - clip.getLeft(), y + height - paint.getStrokeWidth() - clip.getTop(), width, paint.getStrokeWidth());
    clippedPixmap.fillRectangle(x - clip.getLeft(), y - clip.getTop(), width, paint.getStrokeWidth());
    finish();
  }

  public void drawCircle(final int x,
                         final int y,
                         @IntRange(from = 0, to = Integer.MAX_VALUE) final int radius,
                         @NonNull final Paint paint) {
    prepare(paint);
    if (paint.getStyle() == Paint.Style.FILL) {
      clippedPixmap.fillCircle(x - clip.getLeft(), y - clip.getTop(), radius);
      finish();
      return;
    }

    for (int i = 0; i < paint.getStrokeWidth(); i++) {
      clippedPixmap.drawCircle(x - clip.getLeft(), y - clip.getTop(), radius - i);
    }

    finish();
  }

  public void drawCanvas(final int x,
                         final int y,
                         @NonNull final Canvas canvas) {
    drawCanvas(x, y, canvas, Paint.DEFAULT_PAINT);
  }

  public void drawCanvas(final int x,
                         final int y,
                         @NonNull final Canvas canvas,
                         @NonNull final Paint paint) {
    prepare(paint);
    clippedPixmap.drawPixmap(canvas.getPixmap(), x - clip.getLeft(), y - clip.getTop());
    finish();
  }

  public void drawPixmap(final int x,
                         final int y,
                         @NonNull final Pixmap pixmap) {
    drawPixmap(x, y, pixmap, Paint.DEFAULT_PAINT);
  }

  public void drawPixmap(final int x,
                         final int y,
                         @NonNull final Pixmap pixmap,
                         @NonNull final Paint paint) {
    prepare(paint);
    clippedPixmap.drawPixmap(pixmap, x - clip.getLeft(), y - clip.getTop());
    finish();
  }

  public void fill(@NonNull final Paint paint) {
    prepare(paint);
    clippedPixmap.fill();
    finish();
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
