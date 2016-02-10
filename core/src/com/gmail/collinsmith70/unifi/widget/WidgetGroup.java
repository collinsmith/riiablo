package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public class WidgetGroup extends Widget implements WidgetParent, WidgetManager {

public void addWidget(Widget child) { throw new UnsupportedOperationException(); }
public boolean containsWidget(Widget child) { throw new UnsupportedOperationException(); }
public boolean removeWidget(Widget child) { throw new UnsupportedOperationException(); }
public int getNumWidgets() { throw new UnsupportedOperationException(); }

public int getMarginBottom() { throw new UnsupportedOperationException(); }
public int getMarginLeft() { throw new UnsupportedOperationException(); }
public int getMarginRight() { throw new UnsupportedOperationException(); }
public int getMarginTop() { throw new UnsupportedOperationException(); }

public void requestLayout() { throw new UnsupportedOperationException(); }

/**
 * LayoutParams are used by views to tell their parents how they want to be laid out.
 * <p>
 * The base LayoutParams class just describes how big the view wants to be for both width and
 * height.
 * </p>
 * <p>
 *     For each dimension, it can specify one of:
 *     <ul>
 *         <li>
 *         {@link #FILL_PARENT}, which means that the {@linkplain Widget} wants to be as big as its
 *         {@linkplain Widget#getParent() parent} (minus padding)
 *         </li>
 *
 *         <li>
 *         {@link #WRAP_CONTENT}, which means that the {@linkplain Widget} wants to be just big enough to
 *         enclose its content (plus padding) an exact number
 *         </li>
 *     </ul>
 * </p>
 */
public static class LayoutParams {

    /**
     * {@link LayoutParams} {@linkplain #width width} of {@value} symbolizing that the
     * {@linkplain Widget component} wants to be as big as its {@linkplain #getParent parent}
     * (minus padding).
     *
     * @see #setWidth(int)
     * @see #setHeight(int)
     */
    public static final int FILL_PARENT = -1;

    /**
     * {@link LayoutParams} {@linkplain #height height} of {@value} symbolizing that the
     * {@linkplain Widget component} wants to be just big enough to enclose its content
     * (plus padding).
     *
     * @see #setWidth(int)
     * @see #setHeight(int)
     */
    public static final int WRAP_CONTENT = 0;

    /**
     * Information about how wide the {@link WidgetGroup} wants to be
     *
     * @see #getWidth()
     * @see #setWidth(int)
     */
    @IntRange(from = FILL_PARENT, to = Integer.MAX_VALUE)
    private int width;

    /**
     * Information about how tall the {@link WidgetGroup} wants to be
     *
     * @see #getHeight()
     * @see #setHeight(int)
     */
    @IntRange(from = FILL_PARENT, to = Integer.MAX_VALUE)
    private int height;

    /**
     * Constructs a new {@link LayoutParams} instance.
     *
     * @param width  the width, either {@link #WRAP_CONTENT}, {@link #FILL_PARENT}, or a fixed size
     *               in pixels
     * @param height the height, either {@link #WRAP_CONTENT}, {@link #FILL_PARENT}, or a fixed size
     *               in pixels
     */
    public LayoutParams(@IntRange(from = FILL_PARENT, to = Integer.MAX_VALUE) int width,
                        @IntRange(from = FILL_PARENT, to = Integer.MAX_VALUE) int height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * Copy constructor which constructs a new {@link LayoutParams} instance using the
     * {@linkplain LayoutParams#getWidth() width} and {@linkplain LayoutParams#getHeight() height}
     * values from the source object.
     *
     * @param source layout params to copy from
     */
    public LayoutParams(@NonNull LayoutParams source) {
        this(source.width, source.height);
    }

    /**
     * @return width this component wants to be, either {@link #WRAP_CONTENT}, {@link #FILL_PARENT},
     *         or a fixed size in pixels
     */
    @IntRange(from=FILL_PARENT, to = Integer.MAX_VALUE)
    public int getWidth() {
        return width;
    }

    /**
     * @param width non-zero positive width of this {@link LayoutParams}, or {@link #FILL_PARENT} or
     *              {@link #WRAP_CONTENT} to represent a dynamic width which is assigned upon
     *              {@linkplain WidgetParent#requestLayout() layout} of the
     *              {@linkplain #getParent()} of this {@link LayoutParams}.
     */
    public void setWidth(@IntRange(from=FILL_PARENT, to = Integer.MAX_VALUE) int width) {
        if (width < FILL_PARENT) {
            throw new IllegalArgumentException(
                    "width should range from " + FILL_PARENT + " to Integer.MAX_VALUE");
        }

        /* TODO: Assign width flag if equal to {@link #FILL_PARENT} or {@link #WRAP_CONTENT}, otherwise
         *       should width be assigned at all? Width in this sense is a disjoint boolean variable,
         *       and not a literal assignment of the width of this component (i.e., a suggestion for the
         *       parent container of this Widget). */
    }

    /**
     * @return height this component wants to be, either {@link #WRAP_CONTENT}, {@link #FILL_PARENT},
     *         or a fixed size in pixels
     */
    @IntRange(from=FILL_PARENT, to = Integer.MAX_VALUE)
    public int getHeight() {
        return height;
    }

    /**
     * @param height non-zero positive height of this {@link LayoutParams}, or {@link #FILL_PARENT}
     *               or {@link #WRAP_CONTENT} to represent a dynamic height which is assigned upon
     *              {@linkplain WidgetParent#requestLayout() layout} of the
     *              {@linkplain #getParent()} of this {@link LayoutParams}.
     */
    public void setHeight(@IntRange(from=FILL_PARENT, to = Integer.MAX_VALUE) int height) {
        if (height < FILL_PARENT) {
            throw new IllegalArgumentException(
                    "height should range from " + FILL_PARENT + " to Integer.MAX_VALUE");
        }

        /* TODO: Assign height flag if equal to {@link #FILL_PARENT} or {@link #WRAP_CONTENT}, otherwise
         *       should height be assigned at all? Height in this sense is a disjoint boolean variable,
         *       and not a literal assignment of the height of this component (i.e., a suggestion for
         *       the parent container of this Widget). */
    }

}

public static class MarginLayoutParams extends LayoutParams {

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int bottom;

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int left;

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int right;

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int top;

    public MarginLayoutParams(int width, int height) {
        super(width, height);
    }

    public MarginLayoutParams(@NonNull LayoutParams source) {
        super(source);
    }

    public MarginLayoutParams(@NonNull MarginLayoutParams source) {
        super(source);
        setMargins(
                source.getLeft(),
                source.getTop(),
                source.getRight(),
                source.getBottom());
    }

    public void setMargins(@IntRange(from = 0, to = Integer.MAX_VALUE) int left,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int top,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int right,
                           @IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        setLeft(left);
        setTop(top);
        setRight(right);
        setBottom(bottom);
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getBottom() {
        return bottom;
    }

    public void setBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) int bottom) {
        if (bottom < 0) {
            throw new IllegalArgumentException("bottom should be greater than or equal to 0");
        }

        this.bottom = bottom;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getLeft() {
        return left;
    }

    public void setLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) int left) {
        if (left < 0) {
            throw new IllegalArgumentException("left should be greater than or equal to 0");
        }

        this.left = left;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getRight() {
        return right;
    }

    public void setRight(@IntRange(from = 0, to = Integer.MAX_VALUE) int right) {
        if (right < 0) {
            throw new IllegalArgumentException("right should be greater than or equal to 0");
        }

        this.right = right;
    }

    @IntRange(from = 0, to = Integer.MAX_VALUE)
    public int getTop() {
        return top;
    }

    public void setTop(@IntRange(from = 0, to = Integer.MAX_VALUE) int top) {
        if (top < 0) {
            throw new IllegalArgumentException("top should be greater than or equal to 0");
        }

        this.top = top;
    }

}

}
