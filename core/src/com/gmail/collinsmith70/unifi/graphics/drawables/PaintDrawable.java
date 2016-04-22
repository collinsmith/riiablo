package com.gmail.collinsmith70.unifi.graphics.drawables;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.graphics.Paint;

import org.apache.commons.lang3.Validate;

public class PaintDrawable extends Drawable {

    private Paint paint;

    public PaintDrawable() {
        this(Paint.DEFAULT);
    }

    public PaintDrawable(@NonNull Paint paint) {
        _setPaint(paint);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRect(getBounds(), paint);
    }

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
