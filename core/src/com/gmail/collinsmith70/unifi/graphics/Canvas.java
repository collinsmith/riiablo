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
    IntBuffer intBuffer = IntBuffer.allocate(4);
    Gdx.gl.glGetIntegerv(GL20.GL_SCISSOR_BOX, intBuffer);
    return new Rectangle(intBuffer.get(), intBuffer.get(), intBuffer.get(), intBuffer.get());
  }

  @NonNull
  private Rectangle getClip(@Nullable Rectangle dst) {
    if (dst == null) {
      return getClip();
    }

    IntBuffer intBuffer = IntBuffer.allocate(4);
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
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
    Gdx.gl.glScissor(x, y, width, height);
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
  }

  private void prepare(@NonNull Paint paint) {
    Validate.isTrue(paint != null, "paint cannot be null");
    Gdx.gl.glLineWidth(paint.getStrokeWidth());
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
    ShapeRenderer.ShapeType shapeType;
    switch (paint.getStyle()) {
      case FILL:
        shapeType = ShapeRenderer.ShapeType.Filled;
        break;
      case STROKE:
        shapeType = ShapeRenderer.ShapeType.Line;
        break;
      default:
        shapeType = ShapeRenderer.ShapeType.Filled;
    }

    prepare(paint);
    startShapeRenderer(shapeType); {
      shapeRenderer.setColor(paint.getColor());
      shapeRenderer.rect(x, y, width, height);
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
    prepare(paint);
    switch (paint.getStyle()) {
      case STROKE:
        startShapeRenderer(ShapeRenderer.ShapeType.Line); {
          final float radius2 = radius * 2;
          shapeRenderer.setColor(paint.getColor());
          shapeRenderer.line(x, y + radius, x, y + height - radius2);
          shapeRenderer.line(x + width, y + radius, x + width, y + height - radius2);
          shapeRenderer.line(x + radius, y, x + width - radius2, y);
          shapeRenderer.line(x + radius, y + height, x + width - radius2, y + height);

          final float kappa = 0.5522847498307933984022516322796f;
          final int segments = Math.max(1, (int)(6 * (float)Math.cbrt(radius) * 0.25f));
        }

        break;
      case FILL:
      default:
        startShapeRenderer(ShapeRenderer.ShapeType.Filled); {
          final float radius2 = radius * 2;
          shapeRenderer.setColor(paint.getColor());
          shapeRenderer.rect(x, y + radius, width, height - radius2);
          shapeRenderer.rect(x + radius, y, width - radius2, height);
          shapeRenderer.circle(x + radius, y + radius, radius);
          shapeRenderer.circle(x + width - radius, y + radius, radius);
          shapeRenderer.circle(x + radius, y + height - radius, radius);
          shapeRenderer.circle(x + width - radius, y + height - radius, radius);
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
    font.setColor(paint.getColor());
    font.draw(batch, text, x, y);
  }

  public void drawTexture(@NonNull Texture texture,
                          float x,
                          float y) {
    Validate.isTrue(texture != null, "texture cannot be null");
    startBatch(); {
      batch.draw(texture, x, y);
    }
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

}
