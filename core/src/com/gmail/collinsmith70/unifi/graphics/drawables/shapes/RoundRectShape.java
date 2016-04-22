package com.gmail.collinsmith70.unifi.graphics.drawables.shapes;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Paint;

import org.apache.commons.lang3.Validate;

public class RoundRectShape extends RectShape {

    @FloatRange(from = 1.0f, to = Float.MAX_VALUE)
    private float radius = 1.0f;

    @Override
    public void draw(@NonNull Canvas canvas, @NonNull Paint paint) {
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), radius, paint);
    }

    public float getRadius() {
        return radius;
    }

    private void _setRadius(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float radius) {
        Validate.isTrue(radius >= 1.0f, "radius must be greater than or equal to 1.0f");
        this.radius = radius;
    }

    public void setRadius(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float radius) {
        _setRadius(radius);
    }

}
