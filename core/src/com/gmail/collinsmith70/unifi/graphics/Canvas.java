package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.math.Dimension;
import com.gmail.collinsmith70.unifi.math.ImmutableRect;
import com.gmail.collinsmith70.unifi.math.Rect;

import org.apache.commons.lang3.Validate;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class Canvas implements Disposable {

    private static final String TAG = Canvas.class.getSimpleName();

    private static final FloatBuffer FLOAT_BUFFER = BufferUtils.newFloatBuffer(16);
    private static final IntBuffer INT_BUFFER = BufferUtils.newIntBuffer(16);

    @NonNull
    private final GlState glInitialState;

    private boolean restoreGlState;
    private boolean hasBegun;

    @NonNull
    private final Dimension dimension;

    @NonNull
    protected final ShapeRenderer shapeRenderer;

    @NonNull
    protected final Batch batch;

    @NonNull
    private final Deque<State> states;

    @NonNull
    private final Rect clipBounds;

    @NonNull
    private final Rect tmp;

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
        this.glInitialState = new GlState();
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.dimension = new Dimension(width, height);
        this.states = new ArrayDeque<State>();
        this.clipBounds = new Rect();
        this.tmp = new Rect();

        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
    }

    public final int getWidth() {
        return dimension.getWidth();
    }

    public final int getHeight() {
        return dimension.getHeight();
    }

    public final void resize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                             @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        if (!dimension.equals(width, height)) {
            dimension.set(width, height);
            onResize(width, height);
        }
    }

    protected void onResize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    }

    protected final void startBatch() {
        if (!hasBegun) {
            begin();
        }

        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
        }

        if (!batch.isDrawing()) {
            batch.begin();
            onStartBatch(batch);
        }
    }

    protected void onStartBatch(@NonNull Batch batch) {
    }

    protected final void startShapeRenderer(@NonNull ShapeRenderer.ShapeType shapeType) {
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
        onStartShapeRenderer(shapeRenderer, shapeType);
    }

    protected void onStartShapeRenderer(@NonNull ShapeRenderer shapeRenderer,
                                        @NonNull ShapeRenderer.ShapeType shapeType) {
    }

    public final void flush() {
        if (batch.isDrawing()) {
            batch.flush();
        }

        if (shapeRenderer.isDrawing()) {
            shapeRenderer.flush();
        }

        onFlush();
    }

    protected void onFlush() {
    }

    public final void begin() {
        if (hasBegun) {
            return;
        }

        hasBegun = true;
        if (restoreGlState) {
            glInitialState.refresh();
        }

        onBegin();
    }

    protected void onBegin() {
    }

    public final void end() {
        if (batch.isDrawing()) {
            batch.end();
        }

        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
        }

        hasBegun = false;
        states.clear();
        if (restoreGlState) {
            glInitialState.reset();
        }

        onEnd();
    }

    protected void onEnd() {
    }

    @Override
    public final void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        glInitialState.reset();
        onDispose();
    }

    protected void onDispose() {
    }

    public final boolean isDrawing() {
        return batch.isDrawing() || shapeRenderer.isDrawing();
    }

    public boolean isRestoringGlState() {
        return restoreGlState;
    }

    public void setRestoreGlState(boolean shouldRestore) {
        this.restoreGlState = shouldRestore;
    }

    public final boolean clipRect(@NonNull Rect clipBounds) {
        Validate.isTrue(clipBounds != null, "clipBounds cannot be null");
        return clipRect(
                clipBounds.getX(),
                clipBounds.getY(),
                clipBounds.getWidth(),
                clipBounds.getHeight());
    }

    public final boolean clipRect(int x,
                                  int y,
                                  @IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                                  @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
        Validate.isTrue(width >= 0, "width must be greater than or equal to 0");
        Validate.isTrue(height >= 0, "height must be greater than or equal to 0");
        boolean isEmpty = width == 0 || height == 0;
        flush();
        if (states.isEmpty()) {
            Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
            // encoded as: left = x, top = y, right = width, bottom = height
            clipBounds.set(x, y, width, height);
        } else {
            // encoded as: left = x, top = y, right = width, bottom = height
            clipBounds.set(states.getLast().clipBounds);
            final int minX = Math.max(clipBounds.getX(), x);
            final int maxX = Math.min(clipBounds.getX() + clipBounds.getWidth(), x + width);
            final int minY = Math.max(clipBounds.getY(), y);
            final int maxY = Math.min(clipBounds.getY() + clipBounds.getHeight(), y + height);
            isEmpty = isEmpty || maxX - minX < 1 || maxY - minY < 1;
            // encoded as: left = x, top = y, right = width, bottom = height
            clipBounds.set(minX, minY, Math.max(0, maxX - minX), Math.max(0, maxY - minY));
        }

        // encoded as: left = x, top = y, right = width, bottom = height
        HdpiUtils.glScissor(clipBounds.getLeft(), clipBounds.getTop(),
                clipBounds.getRight(), clipBounds.getBottom());
        return isEmpty;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int save() {
        if (states.isEmpty()) {
            return 0;
        }

        State state = new State(clipBounds, batch.getTransformMatrix());
        states.push(state);
        return states.size() - 1;
    }

    public void restore() {
        if (states.isEmpty()) {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
            return;
        }

        State state = states.pop();
        clipBounds.set(state.clipBounds);
        // encoded as: left = x, top = y, right = width, bottom = height
        HdpiUtils.glScissor(clipBounds.getLeft(), clipBounds.getTop(),
                clipBounds.getRight(), clipBounds.getBottom());
        setTransformMatrix(state.transformMatrix);
    }

    public void restoreToCount(@IntRange(from = 0, to = Integer.MAX_VALUE) int saveCount) {
        Validate.isTrue(saveCount >= 0, "saveCount must be greater than or equal to 0");
        State state = null;
        while (states.size() > saveCount) {
            state = states.pop();
        }

        if (states.isEmpty()) {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
            return;
        }

        clipBounds.set(state.clipBounds);
        // encoded as: left = x, top = y, right = width, bottom = height
        HdpiUtils.glScissor(clipBounds.getX(), clipBounds.getY(),
                clipBounds.getWidth(), clipBounds.getHeight());
    }

    @NonNull
    public final Matrix4 getTransformMatrix() {
        return batch.getTransformMatrix();
    }

    public final void setTransformMatrix(@NonNull Matrix4 transformMatrix) {
        batch.setTransformMatrix(transformMatrix);
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        onSetTransformMatrix(transformMatrix);
    }

    protected void onSetTransformMatrix(@NonNull Matrix4 transformMatrix) {
    }

    public void translate(float dx, float dy) {
        batch.getTransformMatrix().translate(dx, dy, 0);
        Validate.isTrue(shapeRenderer.getTransformMatrix().val[Matrix4.M03] == dx);
        Validate.isTrue(shapeRenderer.getTransformMatrix().val[Matrix4.M13] == dy);
    }

    private void prepare(@NonNull Paint paint) {
        Validate.isTrue(paint != null, "paint cannot be null");
        if (isDrawing()) {
            flush();
        }

        Gdx.gl.glLineWidth(paint.getStrokeWidth());
        batch.setColor(paint.getColor());
        shapeRenderer.setColor(paint.getColor());
    }

    private void prepare(@NonNull Paint paint, @NonNull BitmapFont font) {
        Validate.isTrue(paint != null, "font cannot be null");
        prepare(paint);
        font.setColor(paint.getColor());
    }

    public void drawColor(@NonNull Color color) {
        Validate.isTrue(color != null, "color cannot be null");
        startShapeRenderer(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(0, 0, getWidth(), getHeight());
    }

    public void drawPaint(@NonNull Paint paint) {
        Validate.isTrue(paint != null, "paint cannot be null");
        prepare(paint);
        startShapeRenderer(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, getWidth(), getHeight());
    }

    public void drawRect(@NonNull Rect r,
                         @NonNull Paint paint) {
        Validate.isTrue(r != null, "r cannot be null");
        drawRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), paint);
    }

    public void drawRect(float x,
                         float y,
                         @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                         @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height,
                         @NonNull Paint paint) {
        Validate.isTrue(width >= 0, "width must be greater than or equal to 0.0f");
        Validate.isTrue(height >= 0, "height must be greater than or equal to 0.0f");
        Validate.isTrue(paint != null, "paint cannot be null");
        Gdx.app.debug(TAG, "drawRect(" + paint.getStyle() + ")");
        prepare(paint);
        switch (paint.getStyle()) {
            case STROKE:
                final float adjust = paint.getStrokeWidth() / 2;
                x += adjust;
                y += adjust;
                width -= (2 * adjust);
                height -= (2 * adjust);
                Gdx.app.debug(TAG, String.format("adjust=%f, width = %f, height = %f", adjust, width, height));
                startShapeRenderer(ShapeRenderer.ShapeType.Line);
                shapeRenderer.line(x, y - adjust, x, y + height + adjust);
                shapeRenderer.line(x + width, y - adjust, x + width, y + height + adjust);
                shapeRenderer.line(x - adjust, y, x + width + adjust, y);
                shapeRenderer.line(x - adjust, y + height, x + width + adjust, y + height);

                break;
            case FILL:
            default:
                Gdx.app.debug(TAG, String.format("width = %f, height = %f", width, height));
                startShapeRenderer(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rect(x, y, width, height);
        }
    }

    public void drawRoundRect(float x,
                              float y,
                              @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                              @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height,
                              @FloatRange(from = 1.0f, to = Float.MAX_VALUE) float radius,
                              @NonNull Paint paint) {
        Validate.isTrue(width >= 0, "width must be greater than or equal to 0.0f");
        Validate.isTrue(height >= 0, "height must be greater than or equal to 0.0f");
        Validate.isTrue(radius >= 1, "radius must be greater than or equal to 1.0f");
        Validate.isTrue(paint != null, "paint cannot be null");

        if (paint.getStyle() == Paint.Style.STROKE) {
            final float adjust = paint.getStrokeWidth() / 2;
            x += adjust;
            y += adjust;
            width -= (2 * adjust);
            height -= (2 * adjust);
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
                startShapeRenderer(ShapeRenderer.ShapeType.Line);
                shapeRenderer.line(x0, y1, x0, y2);
                shapeRenderer.line(x3, y1, x3, y2);
                shapeRenderer.line(x1, y0, x2, y);
                shapeRenderer.line(x1, y3, x2, y3);

                final float kappa = 0.5522847498307933984022516322796f * radius;
                final int segments = Math.max(1, (int) (6 * (float) Math.cbrt(radius) * 0.25f));
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

                break;
            case FILL:
            default:
                startShapeRenderer(ShapeRenderer.ShapeType.Filled);
                final float radius2 = radius * 2;
                shapeRenderer.rect(x0, y1, width, height - radius2);
                shapeRenderer.rect(x1, y0, width - radius2, height);
                shapeRenderer.circle(x1, y1, radius);
                shapeRenderer.circle(x2, y1, radius);
                shapeRenderer.circle(x1, y2, radius);
                shapeRenderer.circle(x2, y2, radius);
        }
    }

    public void drawText(@NonNull CharSequence text,
                         float x,
                         float y,
                         @NonNull Paint paint,
                         @NonNull BitmapFont font) {
        Validate.isTrue(text != null, "text cannot be null");
        Validate.isTrue(paint != null, "paint cannot be null");
        Validate.isTrue(font != null, "font cannot be null");
        prepare(paint, font);
        startBatch();
        font.draw(batch, text, x, y);
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
        startBatch();
        batch.draw(texture, x, y);
    }

    public void drawTexture(@NonNull Texture texture,
                            float x,
                            float y,
                            @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                            @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height,
                            @NonNull Paint paint) {
        Validate.isTrue(texture != null, "texture cannot be null");
        Validate.isTrue(width >= 0, "width must be greater than or equal to 0.0f");
        Validate.isTrue(height >= 0, "height must be greater than or equal to 0.0f");
        Validate.isTrue(paint != null, "paint cannot be null");
        prepare(paint);
        startBatch();
        batch.draw(texture, x, y, width, height);
    }

    private final class GlState {

        private boolean valid;

        private Rect glScissor = new Rect();
        private float glLineWidth;
        private boolean glScissorTest;

        private GlState() {
        }

        private void refresh() {
            if (valid) {
                return;
            }

            valid = true;

            glScissorTest = Gdx.gl.glIsEnabled(GL20.GL_SCISSOR_TEST);

            FLOAT_BUFFER.clear();
            Gdx.gl.glGetFloatv(GL20.GL_LINE_WIDTH, FLOAT_BUFFER);
            glLineWidth = FLOAT_BUFFER.get();

            INT_BUFFER.clear();
            Gdx.gl.glGetIntegerv(GL20.GL_SCISSOR_BOX, INT_BUFFER);
            // encoded as: left = x, top = y, right = width, bottom = height
            glScissor.set(INT_BUFFER.get(), INT_BUFFER.get(), INT_BUFFER.get(), INT_BUFFER.get());
        }

        private void reset() {
            if (!valid) {
                return;
            }

            valid = false;

            if (glScissorTest) {
                Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
            } else {
                Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
            }

            Gdx.gl.glLineWidth(glLineWidth);
            // encoded as: left = x, top = y, right = width, bottom = height
            Gdx.gl.glScissor(glScissor.getLeft(), glScissor.getTop(),
                    glScissor.getRight(), glScissor.getBottom());
        }

    }

    private static final class State {

        private final ImmutableRect clipBounds;

        private final Matrix4 transformMatrix;

        private State(@NonNull Rect clipBounds, @NonNull Matrix4 transformMatrix) {
            this.clipBounds = ImmutableRect.copyOf(clipBounds);
            this.transformMatrix = transformMatrix;
        }

    }

}
