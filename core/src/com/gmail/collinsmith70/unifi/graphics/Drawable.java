package com.gmail.collinsmith70.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.content.res.Resources;
import com.gmail.collinsmith70.unifi.content.res.TypedArray;
import com.gmail.collinsmith70.unifi.graphics.drawables.ColorDrawable;
import com.gmail.collinsmith70.unifi.graphics.drawables.ShapeDrawable;
import com.gmail.collinsmith70.unifi.graphics.drawables.TextureDrawable;
import com.gmail.collinsmith70.unifi.util.AttributeDecl;
import com.gmail.collinsmith70.unifi.util.AttributeSet;
import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;
import com.gmail.collinsmith70.unifi.util.Padded;
import com.gmail.collinsmith70.unifi.util.Padding;
import com.gmail.collinsmith70.unifi.util.Xml;

import org.apache.commons.lang3.Validate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public abstract class Drawable implements Bounded, Disposable, Padded {

    public static final int INVALID_SIZE = -1;

    @NonNull
    private Bounds bounds = new Bounds() {
        @Override
        protected void onChange() {
            invalidate();
            onBoundsChange(this);
        }
    };

    @NonNull
    private Padding padding = new Padding() {
        @Override
        protected void onChange() {
            invalidate();
            onPaddingChange(this);
        }
    };

    public abstract void draw(@NonNull Canvas canvas);

    public static Drawable createFromXml(@NonNull Resources res,
                                         @NonNull XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return createFromXml(res, parser, null);
    }

    public static Drawable createFromXml(@NonNull Resources res,
                                         @NonNull XmlPullParser parser,
                                         @Nullable Resources.Theme theme)
            throws IOException, XmlPullParserException {
        Validate.isTrue(res != null, "res cannot be null");
        Validate.isTrue(parser != null, "parser cannot be null");
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT
                && eventType != XmlPullParser.START_TAG) {
        }

        if (eventType != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        Drawable drawable = createFromXmlInner(res, parser, attrs, theme);
        if (drawable == null) {
            throw new RuntimeException("Unrecognized initial tag: " + parser.getName());
        }

        return drawable;
    }

    public static Drawable createFromXmlInner(@NonNull Resources res,
                                              @NonNull XmlPullParser parser,
                                              @NonNull AttributeSet attrs)
            throws IOException, XmlPullParserException {
        return createFromXmlInner(res, parser, attrs, null);
    }

    public static Drawable createFromXmlInner(@NonNull Resources res,
                                              @NonNull XmlPullParser parser,
                                              @NonNull AttributeSet attrs,
                                              @Nullable Resources.Theme theme)
            throws IOException, XmlPullParserException {
        final Drawable drawable;
        final String name = parser.getName();
        if (name.equals("color")) {
            drawable = new ColorDrawable();
        } else if (name.equals("shape")) {
            drawable = new ShapeDrawable();
        } else if (name.equals("texture")) {
            drawable = new TextureDrawable();
        } else {
            throw new XmlPullParserException(
                    parser.getPositionDescription() + ": invalid drawable tag " + name);
        }

        drawable.inflate(res, parser, attrs, theme);
        return drawable;
    }

    public void inflate(@NonNull Resources res,
                        @NonNull XmlPullParser parser,
                        @Nullable AttributeSet attrs) {
        inflate(res, parser, attrs, null);
    }

    public void inflate(@NonNull Resources res,
                        @NonNull XmlPullParser parser,
                        @Nullable AttributeSet attrs,
                        @Nullable Resources.Theme theme) {

    }

    static TypedArray obtainAttributes(@NonNull Resources res,
                                       @Nullable Resources.Theme theme,
                                       @NonNull AttributeSet set,
                                       @NonNull AttributeDecl[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }

        return theme.obtainStyledAttributes(set, attrs);
    }

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    public int getIntrinsicWidth() {
        return INVALID_SIZE;
    }

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    public int getIntrinsicHeight() {
        return INVALID_SIZE;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getMinimumWidth() {
        return Math.max(0, getIntrinsicWidth());
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getMinimumHeight() {
        return Math.max(0, getIntrinsicHeight());
    }

    public void invalidate() {
    }

    protected void onBoundsChange(@NonNull Bounds bounds) {
    }

    @Override
    @NonNull
    public Bounds getBounds() {
        return bounds;
    }

    @NonNull
    @Override
    public Bounds getBounds(@Nullable Bounds dst) {
        if (dst == null) {
            return new Bounds(bounds);
        }

        dst.set(bounds);
        return dst;
    }

    @Override
    public void setBounds(@NonNull Bounds src) {
        Validate.isTrue(src != null, "source Bounds cannot be null");
        bounds.set(src);
    }

    @Override
    public boolean hasBounds() {
        return bounds.isEmpty();
    }

    protected void onPaddingChange(@NonNull Padding padding) {
    }

    @Override
    @NonNull
    public Padding getPadding() {
        return padding;
    }

    @NonNull
    @Override
    public Padding getPadding(@Nullable Padding dst) {
        if (dst == null) {
            return new Padding(padding);
        }

        dst.set(padding);
        return dst;
    }

    @Override
    public void setPadding(@NonNull Padding src) {
        Validate.isTrue(src != null, "source Padding cannot be null");
        padding.set(src);
    }

    @Override
    public boolean hasPadding() {
        return padding.isEmpty();
    }

    @Override
    public void dispose() {

    }

    public boolean canApplyTheme() {
        return false;
    }

    public void applyTheme(@NonNull Resources.Theme theme) {
    }

    public ConstantState getConstantState() {
        return null;
    }

    public static abstract class ConstantState {

        public abstract Drawable newDrawable();

        public Drawable newDrawable(@Nullable Resources res) {
            return newDrawable();
        }

        public Drawable newDrawable(@Nullable Resources res, @Nullable Resources.Theme theme) {
            return newDrawable(res);
        }

        public boolean canApplyTheme() {
            return false;
        }

    }

}
