package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.math.ImmutableRectangle;
import com.gmail.collinsmith70.unifi.math.Rectangle;

import org.apache.commons.lang3.Validate;

import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class Canvas implements Disposable {

  private static final class State {

    private final ImmutableRectangle clip;

    private State(@NonNull Rectangle clip) {
      this.clip = new ImmutableRectangle(clip);
    }

    private ImmutableRectangle getClip() {
      return clip;
    }

  }

  @NonNull
  private final Dimension2D dimension;

  @NonNull
  private final Batch batch;

  @NonNull
  private final ShapeRenderer shapeRenderer;

  @NonNull
  private final Deque<State> saveStates;

  @NonNull
  private final Rectangle tmp;

  public Canvas(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    this(width, height, new SpriteBatch(), new ShapeRenderer());
  }

  public Canvas(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) int height,
                @NonNull Batch batch,
                @NonNull ShapeRenderer shapeRenderer) {
    Validate.isTrue(batch != null, "batch cannot be null");
    Validate.isTrue(shapeRenderer != null, "shapeRenderer cannot be null");
    this.dimension = new Dimension2D(width, height);
    this.batch = batch;
    this.shapeRenderer = shapeRenderer;
    shapeRenderer.translate(1, 1, 0);

    this.saveStates = new ArrayDeque<State>();
    this.tmp = new Rectangle();
  }

  protected final Batch getBatch() {
    return batch;
  }

  protected final ShapeRenderer getShapeRenderer() {
    return shapeRenderer;
  }

  public final int getWidth() {
    return dimension.getWidth();
  }

  public final int getHeight() {
    return dimension.getHeight();
  }

  public final Dimension2D getDimensions() {
    return new Dimension2D(dimension);
  }

  public final Dimension2D getDimensions(@Nullable Dimension2D dst) {
    if (dst == null) {
      return getDimensions();
    }

    dst.set(dimension);
    return dst;
  }

  public final void resize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    dimension.set(width, height);
    onResize(width, height);
  }

  protected void onResize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                          @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    //...
  }

  @CallSuper
  public boolean isDrawing() {
    return batch.isDrawing() || shapeRenderer.isDrawing();
  }

  @Override
  public final void dispose() {
    batch.dispose();
    shapeRenderer.dispose();
    onDispose();
  }

  protected void onDispose() {
    //...
  }

  @NonNull
  private Rectangle getClip() {
    IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
    Gdx.gl.glGetIntegerv(GL20.GL_SCISSOR_BOX, intBuffer);
    return new Rectangle(intBuffer.get(), intBuffer.get(), intBuffer.get(), intBuffer.get());
  }

  @NonNull
  private Rectangle getClip(@Nullable Rectangle dst) {
    if (dst == null) {
      return getClip();
    }

    IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
    Gdx.gl.glGetIntegerv(GL20.GL_SCISSOR_BOX, intBuffer);
    dst.set(intBuffer.get(), intBuffer.get(), intBuffer.get(), intBuffer.get());
    return dst;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int save() {
    getClip(tmp);
    State saveState = new State(tmp);
    saveStates.push(saveState);
    return saveStates.size() - 1;
  }

  public void restore() {
    if (saveStates.isEmpty()) {
      Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
      return;
    }

    State saveState = saveStates.pop();
    clipRect(saveState.getClip());
  }

  public void restoreToCount(@IntRange(from = 0, to = Integer.MAX_VALUE) int saveCount) {
    Validate.isTrue(saveCount >= 0, "saveCount must be greater than or equal to 0");
    State saveState = null;
    while (saveStates.size() > saveCount) {
      saveState = saveStates.pop();
    }

    if (saveState == null) {
      Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
      return;
    }

    clipRect(saveState.getClip());
  }

  public void clipRect(@NonNull Rectangle rectangle) {
    Validate.isTrue(rectangle != null, "rectangle cannot be null");
    clipRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
  }

  public void clipRect(int x,
                       int y,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
    Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
    flush();
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    Gdx.gl.glScissor(x, y, width + 1, height + 1);
  }

  private void startBatch() {
    if (shapeRenderer.isDrawing()) {
      shapeRenderer.end();
    }

    if (batch.isDrawing()) {
      return;
    }

    batch.begin();
  }

  protected void startShapeRenderer(@NonNull ShapeRenderer.ShapeType shapeType) {
    Validate.isTrue(shapeType != null, "shapeType cannot be null");
    if (batch.isDrawing()) {
      batch.end();
    }

    if (shapeRenderer.isDrawing()) {
      if (shapeRenderer.getCurrentType() == shapeType) {
        return;
      }

      shapeRenderer.end();
    }

    shapeRenderer.begin(shapeType);
  }

  public void flush() {
    if (batch.isDrawing()) {
      batch.flush();
    }

    if (shapeRenderer.isDrawing()) {
      shapeRenderer.flush();
    }
  }

  public void end() {
    if (batch.isDrawing()) {
      batch.end();
    }

    if (shapeRenderer.isDrawing()) {
      shapeRenderer.end();
    }

    reset();
  }

  private void prepare(@NonNull Paint paint) {
    Validate.isTrue(paint != null, "paint cannot be null");
    if (isDrawing()) {
      flush();
    }

    Gdx.gl.glLineWidth(paint.getStrokeWidth());
  }

  private void reset() {
    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    Gdx.gl.glLineWidth(Paint.DEFAULT.getStrokeWidth());
  }

  public void drawColor(@NonNull Color color) {
    Validate.isTrue(color != null, "color cannot be null");
    startShapeRenderer(ShapeRenderer.ShapeType.Filled); {
      shapeRenderer.setColor(color);
      shapeRenderer.rect(0, 0, getWidth(), getHeight());
    }
  }

  public void drawPaint(@NonNull Paint paint) {
    Validate.isTrue(paint != null, "paint cannot be null");
    drawColor(paint.getColor());
  }

  public void drawRect(float x,
                       float y,
                       @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                       @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height,
                       @NonNull Paint paint) {
    Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
    Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
    Validate.isTrue(paint != null, "paint cannot be null");
    prepare(paint);
    switch (paint.getStyle()) {
      case STROKE:
        final float adjust = paint.getStrokeWidth() / 2;
        width -= adjust;
        height -= adjust;
        startShapeRenderer(ShapeRenderer.ShapeType.Line); {
          shapeRenderer.setColor(paint.getColor());
          shapeRenderer.line(x, y - adjust, x, y + height + adjust);
          shapeRenderer.line(x + width, y - adjust, x + width, y + height + adjust);
          shapeRenderer.line(x - adjust, y, x + width + adjust, y);
          shapeRenderer.line(x - adjust, y + height, x + width + adjust, y + height);
        }

        break;
      case FILL:
      default:
        startShapeRenderer(ShapeRenderer.ShapeType.Line); {
          shapeRenderer.setColor(paint.getColor());
          shapeRenderer.rect(x, y, width, height);
        }
    }
  }

  public void drawRoundRect(float x,
                            float y,
                            @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                            @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height,
                            @FloatRange(from = 1.0f, to = Float.MAX_VALUE) float radius,
                            @NonNull Paint paint) {
    Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
    Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
    Validate.isTrue(radius >= 1, "radius must be greater than or equal to 1");
    Validate.isTrue(paint != null, "paint cannot be null");

    if (paint.getStyle() == Paint.Style.STROKE) {
      final float adjust = paint.getStrokeWidth() / 2;
      width -= adjust;
      height -= adjust;
    }

    final float x0 = x;
    final float x3 = x0 + width;
    final float x1 = x0 + radius;
    final float x2 = x3 - radius;

    final float y0 = y;
    final float y3 = y0 + height;
    final float y1 = y0 + radius;
    final float y2 = y3 - radius;

    prepare(paint);
    switch (paint.getStyle()) {
      case STROKE:
        startShapeRenderer(ShapeRenderer.ShapeType.Line); {
          shapeRenderer.setColor(paint.getColor());
          shapeRenderer.line(x0, y1, x0, y2);
          shapeRenderer.line(x3, y1, x3, y2);
          shapeRenderer.line(x1, y0, x2, y);
          shapeRenderer.line(x1, y3, x2, y3);

          final float kappa = 0.5522847498307933984022516322796f * radius;
          final int segments = Math.max(1, (int)(6 * (float)Math.cbrt(radius) * 0.25f));
          shapeRenderer.curve(
                  x0, y1,
                  x0, y0 + kappa,
                  x0 + kappa, y0,
                  x1, y0,
                  segments);
          shapeRenderer.curve(
                  x0, y2,
                  x0, y2 + kappa,
                  x1 - kappa, y3,
                  x1, y3,
                  segments);
          shapeRenderer.curve(
                  x2, y3,
                  x2 + kappa, y3,
                  x3, y2 + kappa,
                  x3, y2,
                  segments);
          shapeRenderer.curve(
                  x2, y0,
                  x2 + kappa, y0,
                  x3, y1 - kappa,
                  x3, y1,
                  segments);
        }

        break;
      case FILL:
      default:
        startShapeRenderer(ShapeRenderer.ShapeType.Filled); {
          final float radius2 = radius * 2;
          shapeRenderer.setColor(paint.getColor());
          shapeRenderer.rect(x0, y1, width, height - radius2);
          shapeRenderer.rect(x1, y0, width - radius2, height);
          shapeRenderer.circle(x1, y1, radius);
          shapeRenderer.circle(x2, y1, radius);
          shapeRenderer.circle(x1, y2, radius);
          shapeRenderer.circle(x2, y2, radius);
        }
    }
  }

  public void drawText(@NonNull CharSequence text,
                       float x,
                       float y,
                       @NonNull TextPaint paint) {
    Validate.isTrue(text != null, "text cannot be null");
    Validate.isTrue(paint != null, "paint cannot be null");
    BitmapFont font = paint.getFont();
    Validate.isTrue(font != null, "paint font cannot be null");
    prepare(paint);
    startBatch(); {
      font.setColor(paint.getColor());
      font.draw(batch, text, x, y);
    }
  }

  public void drawTexture(@NonNull Texture texture,
                          float x,
                          float y) {
    drawTexture(texture, x, y, Paint.DEFAULT);
  }

  public void drawTexture(@NonNull Texture texture,
                          float x,
                          float y,
                          @NonNull Paint paint) {
    Validate.isTrue(texture != null, "texture cannot be null");
    Validate.isTrue(paint != null, "paint cannot be null");
    prepare(paint);
    startBatch(); {
      batch.setColor(paint.getColor());
      batch.draw(texture, x, y);
    }
  }

  public void drawTexture(@NonNull Texture texture,
                          float x,
                          float y,
                          @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                          @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height,
                          @NonNull Paint paint) {
    Validate.isTrue(texture != null, "texture cannot be null");
    Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
    Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
    Validate.isTrue(paint != null, "paint cannot be null");
    prepare(paint);
    startBatch(); {
      batch.setColor(paint.getColor());
      batch.draw(texture, x, y, width, height);
    }
  }

}
