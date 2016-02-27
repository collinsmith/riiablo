package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class WidgetGroup extends Widget
        implements WidgetParent, WidgetManager, Iterable<Widget> {

private final Collection<Widget> CHILDREN;

public WidgetGroup() {
    this.CHILDREN = new CopyOnWriteArrayList<Widget>();
}

@Override public void requestLayout() {
    if (getLayoutParams() == null) {
        layoutChildren();
        return;
    }

    layoutHorizontal();
}
protected void layoutChildren() {
    for (Widget child : this) {
        if (!(child instanceof WidgetParent)) {
            continue;
        }

        WidgetParent widgetParent = (WidgetParent)child;
        widgetParent.requestLayout();
    }
}
private void layoutHorizontal() {
    assert getLayoutParams() != null : "LayoutParams should not be null";
    final int width = getLayoutParams().getWidth();
    switch (width) {
        case LayoutParams.FILL_PARENT:
            if (!hasParent()) {
                break;
            }

            setWidth(getParent().getWidth());
            setX(0);
            layoutVertical();
            break;
        case LayoutParams.WRAP_CONTENT:
            layoutVertical();
            setWidth(getMinWidth());
            break;
        default:
            setWidth(width);
            layoutVertical();
    }
}
private void layoutVertical() {
    assert getLayoutParams() != null : "LayoutParams should not be null";
    final int height = getLayoutParams().getHeight();
    switch (height) {
        case LayoutParams.FILL_PARENT:
            if (!hasParent()) {
                break;
            }

            setHeight(getParent().getHeight());
            setY(0);
            layoutChildren();
            break;
        case LayoutParams.WRAP_CONTENT:
            layoutChildren();
            setHeight(getMinHeight());
            break;
        default:
            setHeight(height);
            layoutChildren();
    }
}

public int getMarginBottom() { throw new UnsupportedOperationException(); }
public int getMarginLeft() { throw new UnsupportedOperationException(); }
public int getMarginRight() { throw new UnsupportedOperationException(); }
public int getMarginTop() { throw new UnsupportedOperationException(); }

@Override public void onDrawDebug(@NonNull Batch batch) {
    //super.onDrawDebug(batch);
}

@Override @NonNull public Collection<Widget> getChildren() {
    return ImmutableList.copyOf(CHILDREN);
}
@Override public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(CHILDREN.iterator());
}

@IntRange(from = 0, to = Integer.MAX_VALUE) private int minWidth;
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getMinWidth() {
    return minWidth;
}
public void setMinWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
        throw new IllegalArgumentException("height should be greater than 0");
    }

    this.minWidth = width;
}

@IntRange(from = 0, to = Integer.MAX_VALUE) private int minHeight;
@IntRange(from = 0, to = Integer.MAX_VALUE) public int getMinHeight() {
    return minHeight;
}
public void setMinHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
        throw new IllegalArgumentException("height should be greater than 0");
    }

    this.minHeight = height;
}

@Override
public void setDebugging(boolean debugging) {
    if (debugging == isDebugging()) {
        return;
    }

    super.setDebugging(debugging);
    for (Widget child : this) {
        child.setDebugging(true);
    }
}

@NonNull
@Override public WidgetManager addWidget(@NonNull Widget child) {
    if (child == null) {
        throw new IllegalArgumentException("child widget cannot be null");
    }

    CHILDREN.add(child);
    child.setParent(this);
    return this;
}
@Override public boolean containsWidget(@Nullable Widget child) {
    return child != null && CHILDREN.contains(child);
}
@Override public boolean removeWidget(@Nullable Widget child) {
    if (child == null) {
        return false;
    }

    if (child.getParent() == this) {
        child.setParent(null);
    }

    return CHILDREN.remove(child);
}
@Override public int getNumWidgets() {
    return CHILDREN.size();
}

@Override public boolean mouseMoved(int screenX, int screenY) {
    boolean inBounds = super.mouseMoved(screenX, screenY);
    //if (inBounds) {
        for (Widget child : this) {
            child.mouseMoved(screenX, screenY);
        }
    //}

    return inBounds;
}
@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    boolean inBounds = super.mouseMoved(screenX, screenY);
    if (inBounds) {
        for (Widget child : this) {
            child.touchDown(screenX, screenY, pointer, button);
        }
    }

    return false;
}

@Override public void onDraw(Batch batch) {
    drawChildren(batch);
}
public void drawChildren(Batch batch) {
    // TODO: Draw in order of z-index from low to high
    for (Widget child : this) {
        child.draw(batch);
    }
}

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
 */ public static class LayoutParams {

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
     *
     * @see #setWidth(int)
     * @see #setHeight(int)
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

        this.width = width;
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

        this.height = height;
        /* TODO: Assign height flag if equal to {@link #FILL_PARENT} or {@link #WRAP_CONTENT}, otherwise
         *       should height be assigned at all? Height in this sense is a disjoint boolean variable,
         *       and not a literal assignment of the height of this component (i.e., a suggestion for
         *       the parent container of this Widget). */
    }

}
/**
 * Implementation of {@link WidgetGroup.LayoutParams} which adds <a href="https://en.wikipedia.org/wiki/Margin_(typography)">
 * margins</a>. Margins define spacing on the inside of a {@linkplain Widget component} and its
 * edges.
 */ public static class MarginLayoutParams extends LayoutParams {

    /**
     * Bottom margin of this {@link WidgetGroup}
     *
     * @see #setBottom(int)
     * @see #getBottom()
     */
    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int bottom;

    /**
     * Left margin of this {@link WidgetGroup}
     *
     * @see #setLeft(int)
     * @see #getLeft()
     */
    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int left;

    /**
     * Right margin of this {@link WidgetGroup}
     *
     * @see #setRight(int)
     * @see #getRight()
     */
    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int right;

    /**
     * Top margin of this {@link WidgetGroup}
     *
     * @see #setTop(int)
     * @see #getTop()
     */
    @IntRange(from = 0, to = Integer.MAX_VALUE)
    private int top;

    /**
     * Constructs a new {@link MarginLayoutParams} instance.
     *
     * @param width  the width, either {@link #WRAP_CONTENT}, {@link #FILL_PARENT}, or a fixed size
     *               in pixels
     * @param height the height, either {@link #WRAP_CONTENT}, {@link #FILL_PARENT}, or a fixed size
     *               in pixels
     */
    public MarginLayoutParams(int width, int height) {
        super(width, height);
    }

    /**
     * Copy constructor which constructs a new {@link MarginLayoutParams} instance using the
     * {@linkplain LayoutParams#getWidth() width} and {@linkplain LayoutParams#getHeight() height}
     * values from a {@linkplain LayoutParams source} object.
     * <p>
     * Note: The margins on all sides of the created instance will be {@code 0}.
     * </p>
     *
     * @param source layout params to copy from
     */
    public MarginLayoutParams(@NonNull LayoutParams source) {
        super(source);
    }

    /**
     * Copy constructor which constructs a new {@link MarginLayoutParams} instance using the
     * {@linkplain LayoutParams#getWidth() width}, {@linkplain LayoutParams#getHeight() height}
     * and margins from a {@linkplain LayoutParams source} object.
     *
     * @param source
     *
     * @see #setMargins(int, int, int, int)
     */
    public MarginLayoutParams(@NonNull MarginLayoutParams source) {
        super(source);
        setMargins(
                source.getLeft(),
                source.getTop(),
                source.getRight(),
                source.getBottom());
    }

    /**
     * @param left   Specifies extra space on the left side of this {@link Widget}
     * @param top    Specifies extra space on the top side of this {@link Widget}
     * @param right  Specifies extra space on the right side of this {@link Widget}
     * @param bottom Specifies extra space on the bottom side of this {@link Widget}
     *
     * @see #setLeft(int)
     * @see #setTop(int)
     * @see #setRight(int)
     * @see #setBottom(int)
     */
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
