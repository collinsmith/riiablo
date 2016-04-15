package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.unifi.math.Boundary;

import java.util.ArrayDeque;
import java.util.Deque;

public class Canvas implements Disposable {

  private static final Pixmap.Format PIXMAP_FORMAT = Pixmap.Format.RGBA8888;

  @NonNull
  private Pixmap pixmap;

  @NonNull
  private final Boundary clip;

  @Nullable
  private Pixmap clippedPixmap;

  private final Deque<Boundary> saveStates;

  public Canvas(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {
    this(new Pixmap(width, height, PIXMAP_FORMAT));
  }

  public Canvas(@NonNull final Pixmap pixmap) {
    if (pixmap == null) {
      throw new IllegalArgumentException("pixmap cannot be null");
    }

    this.saveStates = new ArrayDeque<Boundary>();
    this.pixmap = pixmap;
    this.clip = new Boundary(0, 0, getWidth(), getHeight());
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int save() {
    if (clippedPixmap != null) {
      throw new IllegalStateException("finish must be called before save");
    }

    saveStates.push(new Boundary(clip));
    return saveStates.size() - 1;
  }

  public void restore() {
    if (clippedPixmap != null) {
      throw new IllegalStateException("finish must be called before restore");
    }

    if (saveStates.isEmpty()) {
      return;
    }

    clip.set(saveStates.pop());
  }

  public void restoreToCount(@IntRange(from = 0, to = Integer.MAX_VALUE) final int saveCount) {
    while (saveStates.size() > saveCount) {
      restore();
    }
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

  private void prepare(@NonNull final Paint paint, @NonNull final Pixmap pixmap) {
    Gdx.gl.glLineWidth(paint.getStrokeWidth());
    pixmap.setColor(paint.getColor());
    Pixmap.setBlending(paint.getBlendingMode());
    Pixmap.setFilter(paint.getFilterMode());
  }

  private void prepare(@NonNull final Paint paint) {
    if (clippedPixmap != null) {
      throw new IllegalStateException("prepare called without calling finish");
    }

    this.clippedPixmap = new Pixmap(clip.getWidth(), clip.getHeight(), PIXMAP_FORMAT);
    prepare(paint, clippedPixmap);
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
    clippedPixmap.fillRectangle(x - clip.getLeft(),
            y - clip.getTop(), paint.getStrokeWidth(), length);
    finish();
  }

  public void drawHorizontalLine(final int x, final int y, final int length,
                                 @NonNull final Paint paint) {
    prepare(paint);
    clippedPixmap.fillRectangle(x - clip.getLeft(),
            y - clip.getTop(), length, paint.getStrokeWidth());
    finish();
  }

  public void drawRectangle(final int x,
                            final int y,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) final int height,
                            @NonNull final Paint paint) {
    prepare(paint);
    if (paint.getStyle() == Paint.Style.FILL) {
      clippedPixmap.fillRectangle(x - clip.getLeft(), y - clip.getTop(), width, height);
      finish();
      return;
    }

    clippedPixmap.fillRectangle(x - clip.getLeft(),
            y - clip.getTop(), paint.getStrokeWidth(), height);
    clippedPixmap.fillRectangle(x + width - paint.getStrokeWidth() - clip.getLeft(),
            y - clip.getTop(), paint.getStrokeWidth(), height);
    clippedPixmap.fillRectangle(x - clip.getLeft(),
            y + height - paint.getStrokeWidth() - clip.getTop(), width, paint.getStrokeWidth());
    clippedPixmap.fillRectangle(x - clip.getLeft(),
            y - clip.getTop(), width, paint.getStrokeWidth());
    finish();
  }

  public void drawRoundRectangle(final int x,
                                 final int y,
                                 @IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                                 @IntRange(from = 0, to = Integer.MAX_VALUE) final int height,
                                 @IntRange(from = 0, to = Integer.MAX_VALUE) final int radius,
                                 @NonNull final Paint paint) {
    Pixmap tempPixmap = new Pixmap(radius, radius, PIXMAP_FORMAT);
    drawCircle(0, 0, radius, paint, tempPixmap);
    drawPixmap(x + width - radius, y + height - radius, tempPixmap, paint);
    tempPixmap.dispose();
    tempPixmap = new Pixmap(radius, radius, PIXMAP_FORMAT);
    drawCircle(radius, 0, radius, paint, tempPixmap);
    drawPixmap(x, y + height - radius, tempPixmap, paint);
    tempPixmap.dispose();
    tempPixmap = new Pixmap(radius, radius, PIXMAP_FORMAT);
    drawCircle(0, radius, radius, paint, tempPixmap);
    drawPixmap(x + width - radius, y, tempPixmap, paint);
    tempPixmap.dispose();
    tempPixmap = new Pixmap(radius, radius, PIXMAP_FORMAT);
    drawCircle(radius, radius, radius, paint, tempPixmap);
    drawPixmap(x, y, tempPixmap, paint);
    tempPixmap.dispose();

    prepare(paint);
    if (paint.getStyle() == Paint.Style.FILL) {
      clippedPixmap.fillRectangle(x - clip.getLeft() + radius,
              y - clip.getTop(),
              width - (2 * radius),
              height);
      clippedPixmap.fillRectangle(x - clip.getLeft(),
              y - clip.getTop() + radius,
              width,
              height - (2 * radius));
      clippedPixmap.fillRectangle(x - clip.getLeft() + width - radius,
              y - clip.getTop() + radius,
              width,
              height - (2 * radius));
      finish();
      return;
    }

    clippedPixmap.fillRectangle(x - clip.getLeft(),
            y - clip.getTop() + radius,
            paint.getStrokeWidth(),
            height - (2 * radius));
    clippedPixmap.fillRectangle(x + width - paint.getStrokeWidth() - clip.getLeft(),
            y - clip.getTop() + radius,
            paint.getStrokeWidth(),
            height - (2 * radius));
    clippedPixmap.fillRectangle(x - clip.getLeft() + radius,
            y + height - paint.getStrokeWidth() - clip.getTop(),
            width - (2 * radius),
            paint.getStrokeWidth());
    clippedPixmap.fillRectangle(x - clip.getLeft() + radius,
            y - clip.getTop(),
            width - (2 * radius),
            paint.getStrokeWidth());
    finish();
  }

  private void drawCircle(final int x,
                          final int y,
                          @IntRange(from = 0, to = Integer.MAX_VALUE) final int radius,
                          @NonNull final Paint paint,
                          @NonNull final Pixmap pixmap) {
    prepare(paint, pixmap);
    if (paint.getStyle() == Paint.Style.FILL) {
      pixmap.fillCircle(x, y, radius);
      return;
    }

    for (int i = 0; i < paint.getStrokeWidth(); i++) {
      pixmap.drawCircle(x, y, radius - i);
    }
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
