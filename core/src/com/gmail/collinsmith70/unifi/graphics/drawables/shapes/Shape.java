package com.gmail.collinsmith70.unifi.graphics.drawables.shapes;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Paint;

import org.apache.commons.lang3.Validate;

public abstract class Shape {

    @FloatRange(from = 0.0f, to = Float.MAX_VALUE)
    private float width;

    @FloatRange(from = 0.0f, to = Float.MAX_VALUE)
    private float height;

    public abstract void draw(@NonNull Canvas canvas, @NonNull Paint paint);

    public final float getWidth() {
        return width;
    }

    public final float getHeight() {
        return height;
    }

    public final void resize(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                             @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height) {
        Validate.isTrue(width >= 0.0f, "width must be greater than or equal to 0.0f");
        Validate.isTrue(height >= 0.0f, "height must be greater than or equal to 0.0f");
        if (getWidth() != width || getHeight() != height) {
            this.width = width;
            this.height = height;
            onResize(width, height);
        }
    }

    protected void onResize(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                            @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height) {
    }

}
