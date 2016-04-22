package com.gmail.collinsmith70.unifi.graphics.drawables.shapes;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Paint;
import com.gmail.collinsmith70.unifi.math.RectF;

public class RectShape extends Shape {

    private final RectF rect = new RectF();

    @Override
    public void draw(@NonNull Canvas canvas, @NonNull Paint paint) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    @Override
    protected void onResize(@FloatRange(from = 0.0f, to = Float.MAX_VALUE) float width,
                            @FloatRange(from = 0.0f, to = Float.MAX_VALUE) float height) {
        rect.set(0, 0, width, height);
    }

    protected final RectF getRect() {
        return rect;
    }

}
