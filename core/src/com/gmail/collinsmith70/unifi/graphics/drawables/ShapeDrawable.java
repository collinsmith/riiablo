package com.gmail.collinsmith70.unifi.graphics.drawables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.graphics.Paint;
import com.gmail.collinsmith70.unifi.graphics.drawables.shapes.Shape;
import com.gmail.collinsmith70.unifi.util.Bounds;

import org.apache.commons.lang3.Validate;

public class ShapeDrawable extends Drawable {

    @Nullable
    private Shape shape;

    @NonNull
    private Paint paint;

    public ShapeDrawable() {
        _setPaint(Paint.DEFAULT);
    }

    public ShapeDrawable(@NonNull Shape shape) {
        this();
        _setShape(shape);
    }

    @Override
    protected void onBoundsChange(@NonNull Bounds bounds) {
        super.onBoundsChange(bounds);
        updateShape();
    }

    private void updateShape() {
        if (shape != null) {
            final Bounds bounds = getBounds();
            shape.resize(bounds.getWidth(), bounds.getHeight());
        }
    }

    protected void onDraw(@NonNull Shape shape, @NonNull Canvas canvas, @NonNull Paint paint) {
        shape.draw(canvas, paint);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Bounds bounds = getBounds();
        final int saveCount = canvas.save();
        canvas.translate(bounds.getX(), bounds.getY());
        canvas.clipRect(getBounds());
        onDraw(shape, canvas, paint);
        canvas.restoreToCount(saveCount);
    }

    @Nullable
    public Shape getShape() {
        return shape;
    }

    private void _setShape(@NonNull Shape shape) {
        Validate.isTrue(shape != null, "shape cannot be null");
        this.shape = shape;
    }

    public void setShape(@NonNull Shape shape) {
        _setShape(shape);
    }

    @NonNull
    public Paint getPaint() {
        return paint;
    }

    private void _setPaint(@NonNull Paint paint) {
        Validate.isTrue(paint != null, "paint cannot be null");
        this.paint = paint;
    }

    public void setPaint(@NonNull Paint paint) {
        _setPaint(paint);
    }

}
