package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;

import org.apache.commons.lang3.Validate;

public class Paint {

    public static final Paint DEFAULT = ImmutablePaint.newImmutablePaint();

    @NonNull
    private Color color;

    @NonNull
    private Style style;

    @FloatRange(from = 1.0f, to = Float.MAX_VALUE)
    private float strokeWidth;

    public Paint() {
        _reset();
    }

    public Paint(@NonNull Paint src) {
        Validate.isTrue(src != null, "source Paint cannot be null");
        _setColor(src.getColor());
        _setStyle(src.getStyle());
        _setStrokeWidth(src.getStrokeWidth());
    }

    private void _reset() {
        _setColor(Color.WHITE);
        _setStyle(Style.FILL);
        _setStrokeWidth(1.0f);
    }

    public void reset() {
        _reset();
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

    @NonNull
    public Style getStyle() {
        return style;
    }

    private void _setStyle(@NonNull Style style) {
        Validate.isTrue(style != null, "style cannot be null");
        this.style = style;
    }

    public void setStyle(@NonNull Style style) {
        _setStyle(style);
    }

    @FloatRange(from = 1.0f, to = Float.MAX_VALUE)
    public float getStrokeWidth() {
        return strokeWidth;
    }

    private void _setStrokeWidth(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
        Validate.isTrue(strokeWidth >= 1.0f, "strokeWidth must be greater than or equal to 1.0f");
        this.strokeWidth = strokeWidth;
    }

    public void setStrokeWidth(@FloatRange(from = 1.0f, to = Float.MAX_VALUE) float strokeWidth) {
        _setStrokeWidth(strokeWidth);
    }

    @Override
    public String toString() {
        return String.format("%s: { color=%s, style=%s, strokeWidth=%f }",
                getClass().getSimpleName(), getColor(), getStyle(), getStrokeWidth());
    }

    public enum Style {
        FILL,
        STROKE
    }

}
