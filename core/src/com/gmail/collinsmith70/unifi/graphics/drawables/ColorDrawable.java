package com.gmail.collinsmith70.unifi.graphics.drawables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.gmail.collinsmith70.unifi.content.res.Resources;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.graphics.Paint;
import com.gmail.collinsmith70.unifi.util.AttributeSet;

import org.apache.commons.lang3.Validate;
import org.xmlpull.v1.XmlPullParser;

public class ColorDrawable extends Drawable {

    private static final Paint paint = new Paint();

    @NonNull
    private ColorState state;

    public ColorDrawable() {
        this(Paint.DEFAULT.getColor());
        this.state = new ColorState();
    }

    public ColorDrawable(@NonNull Color color) {
        this.state = new ColorState();
        _setColor(color);
    }

    @NonNull
    public Color getColor() {
        return state.color;
    }

    private void _setColor(@NonNull Color color) {
        Validate.isTrue(color != null, "color cannot be null");
        state.color = color;
    }

    public void setColor(@NonNull Color color) {
        if (getColor() != color) {
            _setColor(color);
            invalidate();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        paint.setColor(state.color);
        canvas.drawRect(getBounds(), paint);
    }

    @Override
    public void inflate(@NonNull Resources res,
                        @NonNull XmlPullParser parser,
                        @Nullable AttributeSet attrs) {
        super.inflate(res, parser, attrs);

    }

    private void updateState() {

    }

    @Override
    public boolean canApplyTheme() {
        return state.canApplyTheme() || super.canApplyTheme();
    }

    @Override
    public void applyTheme(@NonNull Resources.Theme theme) {
        super.applyTheme(theme);

        final ColorState state = getConstantState();
        if (state == null || !state.canApplyTheme()) {
            return;
        }


    }

    @Override
    public ColorState getConstantState() {
        return state;
    }

    private ColorDrawable(@NonNull ColorState src) {
        Validate.isTrue(src != null, "source ColorState cannot be null");
        this.state = src;
    }

    private static ColorDrawable copyOf(@NonNull ColorState src) {
        return new ColorDrawable(src);
    }

    final static class ColorState extends ConstantState {

        @NonNull
        Color color;

        @Nullable
        AttributeSet themeAttrs;

        ColorState() {
        }

        ColorState(@NonNull ColorState src) {
            Validate.isTrue(src != null, "source ColorState cannot be null");
            this.color = src.color;
            this.themeAttrs = src.themeAttrs;
        }

        @Override
        public boolean canApplyTheme() {
            return themeAttrs != null;
        }

        @Override
        public Drawable newDrawable() {
            return ColorDrawable.copyOf(this);
        }

        @Override
        public Drawable newDrawable(@Nullable Resources res) {
            return ColorDrawable.copyOf(this);
        }

    }

}
