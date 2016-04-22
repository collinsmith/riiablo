package com.gmail.collinsmith70.unifi.graphics.drawables;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.graphics.Paint;

import org.apache.commons.lang3.Validate;

public class ColorDrawable extends Drawable {

    private static final Paint paint = new Paint();

    @NonNull
    private Color color;

    public ColorDrawable() {
        this(Paint.DEFAULT.getColor());
    }

    public ColorDrawable(@NonNull Color color) {
        _setColor(color);
    }

    @NonNull
    public Color getColor() {
        return color;
    }

    private void _setColor(@NonNull Color color) {
        Validate.isTrue(color != null, "color cannot be null");
        this.color = color;
    }

    public void setColor(@NonNull Color color) {
        _setColor(color);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        paint.setColor(color);
        canvas.drawRect(getBounds(), paint);
    }

}
