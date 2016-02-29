package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gmail.collinsmith70.unifi.drawable.Drawable;
import com.gmail.collinsmith70.unifi.util.Focusable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

public class Widget
        implements RelativeBoundary, InputProcessor, Focusable {

private final Comparator<Widget> ELEVATION_COMPARATOR = new Comparator<Widget>() {
    /**
     * Implementation of {@link Comparator#compare(Object, Object)} which compares {@code Widget}
     * instances based on their {@linkplain Widget#getElevation() elevation} (low to high).
     *
     * {@inheritDoc}
     */ @Override
    @IntRange(from = -1, to = 1) public int compare(Widget o1, Widget o2) {
        return Float.compare(o1.getElevation(), o2.getElevation());
    }
};

public Comparator<Widget> getElevationComparator() {
    return ELEVATION_COMPARATOR;
}

/**
 * {@link Set} of {@linkplain Flag flags} representing the boolean states of this {@code Widget}.
 */ private final Set<Flag> FLAGS;
/**
 * Enumeration of all boolean state fields for a {@code Widget}.
 */ private enum Flag {
    /**
     * {@code true} implies that this {@code Widget} is enabled, while {@code false} implies that it is
     * not. Interpretation on what exactly enabled means varies by subclass, but generally this effects
     * whether or not the state of the {@code Widget} is mutable.
     *
     * @see #isEnabled()
     * @see #setEnabled(boolean)
     */
    ENABLED,
    /**
     * {@code true} implies that this {@code Widget} is in {@linkplain #isDebugging() debug} mode and will
     * have {@link #onDrawDebug(Batch)} called on every {@linkplain #draw(Batch) draw}, otherwise
     * {@code false} implies that this {@code Widget} should behave normally.
     */
    DEBUGGING,
    /**
     * {@code true} implies that this {@code Widget} has an input device hovering over it
     * (i.e., the mouse cursor), otherwise {@code false}.
     */
    OVER,
    /**
     * {@code true} implies that this {@code Widget} is pressed and/or has a pressed input device
     * over it, otherwise {@code false}
     */
    DOWN,
    /**
     * {@code true} implies that this {@code Widget} may receive focus (i.e., device input),
     * otherwise {@code false}.
     */
    FOCUSABLE
}

public Widget() {
    this.FLAGS = EnumSet.noneOf(Flag.class);

    setDebugging(false);
    setEnabled(true);
    setOver(false);
    setParent(null);
    setDown(false);
    setTag(null);
    setVisibility(Visibility.VISIBLE);
}

/**
 * @return {@code true} if this {@code Widget} is in debug mode and will have
 *         {@link #onDrawDebug(Batch)} called after every {@linkplain #onDraw(Batch) draw},
 *         otherwise {@code false}
 *
 * @see #setDebugging(boolean)
 */ public boolean isDebugging() {
    return FLAGS.contains(Flag.DEBUGGING);
}
/**
 * @param debugging {@code true} to enable {@linkplain #isDebugging() debugging} for this
 *                  {@code Widget}, otherwise {@code false}
 *
 * @see #isDebugging()
 */ public void setDebugging(boolean debugging) {
    if (debugging) {
        FLAGS.add(Flag.DEBUGGING);
    } else {
        FLAGS.remove(Flag.DEBUGGING);
    }
}
public boolean isDown() {
    return FLAGS.contains(Flag.DOWN);
}
private void setDown(boolean down) {
    if (down) {
        FLAGS.add(Flag.DOWN);
    } else {
        FLAGS.remove(Flag.DOWN);
    }
}
/**
 * @return {@code true} if this {@code Widget} is enabled. Interpretation varies by subclass.
 *
 * @see #setEnabled(boolean)
 */ public boolean isEnabled() {
    return FLAGS.contains(Flag.ENABLED);

}
/**
 * @param enabled {@code true} to enable this {@code Widget}, otherwise {@code false}.
 *                Interpretation varies by subclass.
 *
 * @see #isEnabled()
 */ public void setEnabled(boolean enabled) {
    if (enabled) {
        FLAGS.add(Flag.ENABLED);
    } else {
        FLAGS.remove(Flag.ENABLED);
    }
}
/**
 * @return {@code true} if this {@code Widget} has an input cursor hovering above it (e.g., mouse
 *         cursor), otherwise {@code false}
 */ public boolean isOver() {
    return FLAGS.contains(Flag.OVER);

}
/**
 * @param over {@code true} to enable the {@linkplain #isOver() hovering} state for this
 *             {@code Widget}, otherwise {@code false}.
 *
 * @see #isOver()
 */ private void setOver(boolean over) {
    if (over) {
        FLAGS.add(Flag.OVER);
    } else {
        FLAGS.remove(Flag.OVER);
    }
}


/**
 * Floating-point representation of the z-axis location of this {@code Widget}, used to determine
 * height of a component relative to its {@linkplain #getParent() parent}.
 */ @FloatRange(from = 0.0, to = Float.MAX_VALUE) private float elevation;
/**
 * @return Floating-point representation of the z-axis location of this {@code Widget}, used to
 *         determine height of a component relative to its {@linkplain #getParent() parent}.
 */ @FloatRange(from = 0.0, to = Float.MAX_VALUE) public float getElevation() {
    return elevation;
}
/**
 * @param elevation Floating-point representation for the {@linkplain #getElevation() elevation} of
 *                  this {@code Widget}, used to determine height of a component relative to its
 *                  {@linkplain #getParent() parent}.
 */ public void setElevation(@FloatRange(from = 0.0, to = Float.MAX_VALUE) float elevation) {
    if (elevation < 0.0f) {
        throw new IllegalArgumentException(
                "elevation must be between 0.0 and " + Float.MAX_VALUE + " (inclusive)");
    }

    this.elevation = elevation;
}

/**
 * {@link WidgetGroup.LayoutParams} instance containing arguments associated with laying out this
 * {@code Widget}.
 *
 * @see #getLayoutParams()
 * @see #setLayoutParams(WidgetGroup.LayoutParams)
 */ @Nullable private WidgetGroup.LayoutParams layoutParams;
/**
 * Get the {@link WidgetGroup.LayoutParams} associated with this {@code Widget}. All widgets should
 * have layout parameters. These supply parameters to the {@linkplain #getParent() parent} of this
 * widget specifying how it should be arranged. There are many subclasses of
 * {@link WidgetGroup.LayoutParams}, and these correspond to the different subclasses of
 * {@link WidgetGroup} that are responsible for arranging their children. This method may return
 * {@code null} if this {@code Widget} is not attached to a parent {@link WidgetGroup} or
 * {@link #setLayoutParams(WidgetGroup.LayoutParams)} was not invoked successfully. When a
 * {@code Widget} is attached to a parent {@link WidgetGroup}, this method must not return
 * {@code null}.
 *
 * @return {@link WidgetGroup.LayoutParams} instance associated with this {@code Widget},
 *         or {@code null} if no parameters have been set yet
 */ @Nullable public WidgetGroup.LayoutParams getLayoutParams() {
    return layoutParams;
}
/**
 * Sets the layout parameters associated with this view. These supply parameters to the parent of
 * this {@code Widget} specifying how it should be arranged. There are many subclasses of
 * {@link WidgetGroup.LayoutParams}, and these correspond to the different subclasses of
 * {@link WidgetGroup} that are responsible for arranging their children.
 *
 * @param params layout parameters for this {@code Widget}, cannot be {@code null}
 */ public void setLayoutParams(@NonNull WidgetGroup.LayoutParams params) {
    if (params == null) {
        throw new IllegalArgumentException("params cannot be null");
    }

    this.layoutParams = params;
    switch (this.layoutParams.getWidth()) {
        case WidgetGroup.LayoutParams.FILL_PARENT:
            if (!hasParent()) {
                //throw new IllegalArgumentException("FILL_PARENT specified without parent");
                break;
            }

            setLeft(getParent().getLeft());
            setRight(getParent().getRight());
            break;
        case WidgetGroup.LayoutParams.WRAP_CONTENT:
            if (!(this instanceof WidgetGroup)) {
                throw new UnsupportedOperationException(
                        "Widget must be an instance of WidgetGroup to wrap size to content");
            }

            WidgetGroup widgetGroup = (WidgetGroup)this;
            widgetGroup.setWidth(widgetGroup.getMinWidth());
            break;
        default:
            setWidth(this.layoutParams.getWidth());
    }

    switch (this.layoutParams.getHeight()) {
        case WidgetGroup.LayoutParams.FILL_PARENT:
            if (!hasParent()) {
                //throw new IllegalArgumentException("FILL_PARENT specified without parent");
                break;
            }

            setTop(getParent().getTop());
            setBottom(getParent().getBottom());
            break;
        case WidgetGroup.LayoutParams.WRAP_CONTENT:
            if (!(this instanceof WidgetGroup)) {
                throw new UnsupportedOperationException(
                        "Widget must be an instance of WidgetGroup to wrap size to content");
            }

            WidgetGroup widgetGroup = (WidgetGroup)this;
            widgetGroup.setHeight(widgetGroup.getMinHeight());
            break;
        default:
            setHeight(this.layoutParams.getHeight());
    }
}

/**
 * {@linkplain Enum Enumeration} of all {@link Visibility} modifiers a {@code Widget} may have.
 */ public enum Visibility {
    /** {@code Widget} will be laid out and rendered normally */
    VISIBLE,
    /** {@code Widget} will be laid out normally, however it will not be drawn */
    INVISIBLE,
    /** {@code Widget} will neither be laid out or drawn (as if it is not even present) */
    GONE
}
/**
 * {@link Visibility} of this {@code Widget}. This effects how the component behaves when rendering
 * and additionally when being laid out.
 *
 * @see #getVisibility()
 * @see #setVisibility(Visibility)
 */ @NonNull private Visibility visibility;
/**
 * @return {@link Visibility} constant of this {@code Widget}
 */ @NonNull public Visibility getVisibility() {
    return visibility;
}
/**
 * @param visibility {@link Visibility} constant to apply to this {@code Widget}
 */ public void setVisibility(@NonNull Visibility visibility) {
    if (visibility == null) {
        throw new IllegalArgumentException("visibility cannot be null");
    }

    this.visibility = visibility;
}

/**
 * Draws this {@code Widget} onto the given {@link Batch}.
 *
 * @param batch {@link Batch} to draw this {@code Widget} onto
 *
 * @see #onDrawBackground(Batch)
 * @see #onDraw(Batch)
 * @see #onDrawDebug(Batch)
 */ protected void draw(@NonNull Batch batch) {
    assert batch != null : "batch should never be null";
    if (!getVisibility().equals(Visibility.VISIBLE)) {
        return;
    }

    onDrawBackground(batch);
    onDraw(batch);
    if (isDebugging()) {
        onDrawDebug(batch);
    }
}
/**
 * Called when the {@linkplain Drawable background} of this {@code Widget} should be drawn.
 *
 * @param batch {@link Batch} to draw onto
 */ public void onDrawBackground(@NonNull Batch batch) {
    if (batch == null) {
        throw new IllegalArgumentException("batch should never be null");
    }
}
/**
 * Called when the foreground of this {@code Widget} should be drawn.
 *
 * @param batch {@link Batch} to draw onto
 */ public void onDraw(@NonNull Batch batch) {
    if (batch == null) {
        throw new IllegalArgumentException("batch should never be null");
    }
}
/**
 * Called when {@link #onDraw(Batch) drawing} this widget when it is in
 * {@linkplain #isDebugging() debug} mode.
 *
 * @param batch {@link Batch} to draw debug information onto
 */ public void onDrawDebug(@NonNull Batch batch) {
    if (batch == null) {
        throw new IllegalArgumentException("batch should never be null");
    }

    final ShapeRenderer shapeRenderer = new ShapeRenderer();
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line); {
        Color color = Color.RED;
        if (!isEnabled()) {
            color = Color.LIGHT_GRAY;
        } else if (isDown() && isOver()) {
            color = Color.GREEN;
        } else if (isOver()) {
            color = Color.BLUE;
        }

        shapeRenderer.setColor(color);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        //System.out.printf("%s [%d, %d, %d, %d] [%d, %d, %d, %d]%n", getClass().getName(),
        //        getX(), getY(), getWidth(), getHeight(),
        //        getRelativeLeft(), getRelativeRight(), getRelativeTop(), getRelativeBottom());
    } shapeRenderer.end();
    shapeRenderer.dispose();
}

public void onTouch(int screenX, int screenY, int button, int pointer) {
    System.out.println("onTouch " + getClass().getSimpleName());
}

/**
 * Abstract {@code Object} associated with this {@code Widget}.
 */ @Nullable private Object tag;
/**
 * @return abstract {@code Object} associated with this {@code Widget}, or {@code null} if no object
 *         is associated with it
 */ @Nullable public Object getTag() {
    return tag;
}
/**
 * @param tag abstract {@code Object} to associate with this {@code Widget}, or {@code null} to
 *            disassociate the currently {@linkplain #getTag() tagged object}
 */ public void setTag(@Nullable final Object tag) {
    this.tag = tag;
}

/**
 * @return topmost {@code Widget} {@linkplain #getParent() containing} this {@code Widget}
 */ @Nullable public Widget getRootWidget() {
    Widget root = null;
    for (WidgetParent parent = getParent();
        parent != null && parent instanceof Widget;
        parent = getParent()) {
        root = (Widget)parent;
    }

    return root;
}
/**
 * @return {@link Window} containing this {@code Widget}
 */ @Nullable public final Window getWindow() {
    WidgetParent parent = getParent();
    while (parent != null) {
        if (parent instanceof Window) {
            return (Window)parent;
        }

        parent = parent.getParent();
    }

    return null;
}

@Override public boolean isFocusable() {
    return FLAGS.contains(Flag.FOCUSABLE) && getVisibility().equals(Visibility.VISIBLE);
}
@Override public boolean hasFocus() {
    return false;
}
@Override public boolean hasFocusable() {
    if (hasParent()) {
        return isFocusable() || getParent().hasFocusable();
    }

    return isFocusable();
}
@Override public void setFocusable(boolean focusable) {
    if (focusable) {
        FLAGS.add(Flag.FOCUSABLE);
    } else {
        FLAGS.remove(Flag.FOCUSABLE);
    }
}

@Nullable private WidgetParent parent;
@Nullable public WidgetParent getParent() {
    return parent;
}
public void setParent(@Nullable WidgetParent parent) {
    this.parent = parent;
}
public boolean hasParent() {
    return getParent() != null;
}

private int bottom;
private int left;
private int right;
private int top;

@Override public Boundary getRelativeParent() {
    return getParent();
}
public boolean hasRelativeParent() {
    return hasParent();
}

@Override public int getRelativeBottom() {
    return bottom;
}
@Override public int getRelativeLeft() {
    return left;
}
@Override public int getRelativeRight() {
    return right;
}
@Override public int getRelativeTop() {
    return top;
}

@Override public void setRelativeBottom(int bottom) {
    if (bottom > getRelativeTop()) {
        setRelativeTop(bottom);
    }

    this.bottom = bottom;
}
@Override public void setRelativeLeft(int left) {
    if (left > getRelativeRight()) {
        setRelativeRight(left);
    }

    this.left = left;
}
@Override public void setRelativeRight(int right) {
    if (right < getRelativeLeft()) {
        setRelativeLeft(right);
    }

    this.right = right;
}
@Override public void setRelativeTop(int top) {
    if (top < getRelativeBottom()) {
        setRelativeBottom(top);
    }

    this.top = top;
}

@Override public void moveRelativeBottom(int bottom) {
    if (hasRelativeParent()) {
        moveBottom(getRelativeParent().getBottom() + bottom);
        return;
    }

    moveBottom(bottom);
}
@Override public void moveRelativeLeft(int left) {
    if (hasRelativeParent()) {
        moveLeft(getRelativeParent().getLeft() + left);
        return;
    }

    moveLeft(left);
}
@Override public void moveRelativeRight(int right) {
    if (hasRelativeParent()) {
        moveRight(getRelativeParent().getLeft() + right);
        return;
    }

    moveRight(right);
}
@Override public void moveRelativeTop(int top) {
    if (hasRelativeParent()) {
        moveTop(getRelativeParent().getBottom() + top);
        return;
    }

    moveTop(top);
}

@Override public int getBottom() {
    if (hasRelativeParent()) {
        return getRelativeParent().getBottom() + getRelativeBottom();
    }

    return getRelativeBottom();
}
@Override public int getLeft() {
    if (hasRelativeParent()) {
        return getRelativeParent().getLeft() + getRelativeLeft();
    }

    return getRelativeLeft();
}
@Override public int getRight() {
    if (hasRelativeParent()) {
        return getRelativeParent().getLeft() + getRelativeRight();
    }

    return getRelativeRight();
}
@Override public int getTop() {
    if (hasRelativeParent()) {
        return getRelativeParent().getBottom() + getRelativeTop();
    }

    return getRelativeTop();
}

@Override public void setBottom(int bottom) {
    if (hasRelativeParent()) {
        setRelativeBottom(bottom - getRelativeParent().getBottom());
        return;
    }

    setRelativeBottom(bottom);
}
@Override public void setLeft(int left) {
    if (hasRelativeParent()) {
        setRelativeLeft(left - getRelativeParent().getLeft());
        return;
    }

    setRelativeLeft(left);
}
@Override public void setRight(int right) {
    if (hasRelativeParent()) {
        setRelativeRight(right - getRelativeParent().getLeft());
        return;
    }

    setRelativeRight(right);
}
@Override public void setTop(int top) {
    if (hasRelativeParent()) {
        setRelativeTop(top - getRelativeParent().getBottom());
        return;
    }

    setRelativeTop(top);
}

@Override public void moveBottom(int bottom) {
    setY(bottom);
}
@Override public void moveLeft(int left) {
    setX(left);
}
@Override public void moveRight(int right) {
    setX(right - getWidth());
}
@Override public void moveTop(int top) {
    setY(top - getHeight());
}

@Override public int getX() {
    return getLeft();
}
@Override public int getY() {
    return getBottom();
}
@Override @IntRange(from = 0, to = Integer.MAX_VALUE) public int getWidth() {
    return getRight() - getLeft();
}
@Override @IntRange(from = 0, to = Integer.MAX_VALUE) public int getHeight() {
    return getTop() - getBottom();
}

@Override public void setX(int x) {
    final int width = getWidth();
    setLeft(x);
    setWidth(width);
}
@Override public void setY(int y) {
    final int height = getHeight();
    setBottom(y);
    setHeight(height);
}
@Override public void setWidth(@IntRange(from = 0, to = Integer.MAX_VALUE) int width) {
    if (width < 0) {
        throw new IllegalArgumentException(
                "width should be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    setRight(getLeft() + width);
}
@Override public void setHeight(@IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    if (height < 0) {
        throw new IllegalArgumentException(
                "height should be between 0 and " + Integer.MAX_VALUE + " (inclusive)");
    }

    setTop(getBottom() + height);
}

@Override public void setBounds(int left, int right, int top, int bottom) {
    if (right > left) {
        throw new IllegalArgumentException(
                "left should be less than or equal to right (" + right + ")");
    } else if (bottom > top) {
        throw new IllegalArgumentException(
                "bottom should be less than or equal to top (" + top + ")");
    }

    setLeft(left);
    setRight(right);
    setTop(top);
    setBottom(bottom);
}
@Override public void setRelativeBounds(int left, int right, int top, int bottom) {
    if (right > left) {
        throw new IllegalArgumentException(
            "left should be less than or equal to right (" + right + ")");
    } else if (bottom > top) {
        throw new IllegalArgumentException(
                "bottom should be less than or equal to top (" + top + ")");
    }

    setRelativeLeft(left);
    setRelativeRight(right);
    setRelativeTop(top);
    setRelativeBottom(bottom);
}
@Override public boolean inBounds(int x, int y) {
    return getLeft() <= x && x <= getRight()
            && getBottom() <= y && y <= getTop();
}
@Override public boolean hasSize() {
    return getRelativeRight() > getRelativeLeft()
            && getRelativeTop() > getRelativeBottom();
}
@Override public void setPosition(int x, int y) {
    setX(x);
    setY(y);
}
@Override public void setSize(@IntRange(from = 0, to = Integer.MAX_VALUE) int width,
                              @IntRange(from = 0, to = Integer.MAX_VALUE) int height) {
    setWidth(width);
    setHeight(height);
}

@Override public boolean mouseMoved(int screenX, int screenY) {
    boolean inBounds = inBounds(screenX, screenY);
    setOver(inBounds);
    //System.out.printf("moving (%d, %d)%n", screenX, screenY);
    return inBounds;
}
@Override public boolean keyDown(int keycode) {
    return false;
}
@Override public boolean keyUp(int keycode) {
    return false;
}
@Override public boolean keyTyped(char character) {
    return false;
}
@Override public boolean scrolled(int amount) {
    return false;
}
@Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (!isEnabled()) {
        return false;
    }

    boolean inBounds = inBounds(screenX, screenY);
    if (inBounds) {
        setDown(true);
    }

    return inBounds;
}
@Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (!isEnabled()) {
        return false;
    }

    if (isDown()) {
        setDown(false);
        if (inBounds(screenX, screenY)) {
            onTouch(screenX, screenY, pointer, button);
        }

        return true;
    }

    return false;
}
@Override public boolean touchDragged(int screenX, int screenY, int pointer) {
    if (!isEnabled()) {
        return false;
    }

    boolean inBounds = inBounds(screenX, screenY);
    setOver(inBounds);
    //System.out.printf("dragging (%d, %d)%n", screenX, screenY);
    return inBounds;
}

}
