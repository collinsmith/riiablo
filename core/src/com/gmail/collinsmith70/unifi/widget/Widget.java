package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gmail.collinsmith70.unifi.util.Drawable;
import com.gmail.collinsmith70.unifi.util.Enableable;

import java.util.Random;

public abstract class Widget implements Comparable<Widget>, Enableable {

/**
 * {@linkplain Enum Enumeration} of all {@link Visibility} modifiers a {@link Widget} may have.
 */
public enum Visibility {
    /** {@link Widget} will be laid out and rendered normally */
    VISIBLE,
    /** {@link Widget} will be laid out normally, however it will not be drawn */
    INVISIBLE,
    /** {@link Widget} will neither be laid out or drawn (as if it is not even present) */
    GONE
}

/**
 * Reference to the {@linkplain WidgetParent parent} of this {@link Widget}, or {@code null} if no
 * parent is set. This typically occurs when the {@link Widget} has been constructed, but has not
 * yet been added to a {@linkplain WidgetParent container}.
 *
 * @see #getParent()
 * @see #setParent(WidgetParent)
 */
@Nullable
private WidgetParent parent;

/**
 * {@code true} implies that this {@link Widget} is enabled, while {@code false} implies that it is
 * not. Interpretation on what exactly enabled means varies by subclass, but generally this effects
 * whether or not the state of the {@link Widget} is mutable.
 *
 * @see #isEnabled()
 * @see #setEnabled(boolean)
 */
private boolean enabled;

/**
 * {@code true} implies that this {@link Widget} is in {@linkplain #isDebugging() debug} mode and will
 * have {@link #onDrawDebug(Batch)} called on every {@linkplain #draw(Batch) draw}, otherwise
 * {@code false} implies that this {@link Widget} should behave normally.
 */
private boolean debugging;

/**
 * {@link Visibility} of this {@link Widget}. This effects how the component behaves when rendering
 * and additionally when being laid out.
 *
 * @see #getVisibility()
 * @see #setVisibility(Visibility)
 */
@NonNull
private Visibility visibility;

/**
 * {@link WidgetGroup.LayoutParams} instance containing arguments associated with laying out this
 * {@link Widget}.
 *
 * @see #getLayoutParams()
 * @see #setLayoutParams(WidgetGroup.LayoutParams)
 */
@Nullable
private WidgetGroup.LayoutParams layoutParams;

/**
 * {@link Drawable} rendered behind this {@link Widget}.
 *
 * @see #getBackground()
 * @see #setBackground(Drawable)
 */
@Nullable
private Drawable background;

/**
 * Floating-point representation of the z-axis location of this {@link Widget}, used to determine
 * height of a component relative to its {@linkplain #getParent() parent}.
 */
@FloatRange(from = 0.0, to = Float.MAX_VALUE)
private float elevation;

public Widget() {
    setDebugging(false);
    setEnabled(true);
    setVisibility(Visibility.VISIBLE);
}

/**
 * Draws this {@link Widget} onto the given {@link Batch}.
 *
 * @param batch {@link Batch} to draw this {@link Widget} onto
 *
 * @see #onDrawBackground(Batch)
 * @see #onDraw(Batch)
 * @see #onDrawDebug(Batch)
 */
final void draw(Batch batch) {
    onDrawBackground(batch);
    onDraw(batch);
    if (isDebugging()) {
        onDrawDebug(batch);
    }
}

/**
 * Called when the {@linkplain Drawable background} of this {@link Widget} should be drawn.
 *
 * @param batch {@link Batch} to draw onto
 */
public void onDrawBackground(Batch batch) {
    if (getBackground() == null) {
        return;
    }

    getBackground().draw(batch);
}

/**
 * Called when the foreground of this {@link Widget} should be drawn.
 *
 * @param batch {@link Batch} to draw onto
 */
public void onDraw(Batch batch) {}

/**
 * Called when {@link #onDraw(Batch) drawing} this widget when it is in
 * {@linkplain #isDebugging() debug} mode.
 *
 * @param batch {@link Batch} to draw debug information onto
 */
public void onDrawDebug(Batch batch) {
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    shapeRenderer.begin();
    shapeRenderer.rectLine(0, 0, getWidth(), getHeight(), 1);

    Random random = new Random();
    shapeRenderer.setColor(random.nextInt(256), random.nextInt(256), random.nextInt(256), 255);

    shapeRenderer.end();
    shapeRenderer.dispose();
}

/**
 * @return {@code true} if this {@link Widget} is in debug mode and will {@link #onDrawDebug(Batch)}
 *         called after every {@linkplain #onDraw(Batch) draw}, otherwise {@code false}
 */
public boolean isDebugging() {
    return debugging;
}

/**
 * @param debugging {@code true} to enable {@linkplain #isDebugging() debugging} for this
 *        {@link Widget}, otherwise {@code false}
 */
public void setDebugging(boolean debugging) {
    this.debugging = debugging;
}

/**
 * @return {@code true} if this {@link Widget} is enabled. Interpretation varies by subclass.
 *
 * @see #setEnabled(boolean)
 */
@Override
public boolean isEnabled() {
    return enabled;
}

/**
 * @param enabled {@code true} to enable this {@link Widget}, otherwise {@code false}.
 *                Interpretation varies by subclass.
 *
 * @see #isEnabled()
 */
@Override
public void setEnabled(boolean enabled) {
    this.enabled = enabled;
}

/**
 * @return {@link Visibility} constant of this {@link Widget}
 */
@NonNull
public Visibility getVisibility() {
    return visibility;
}

/**
 * @param visibility {@link Visibility} constant to apply to this {@link Widget}
 */
public void setVisibility(@NonNull Visibility visibility) {
    if (visibility == null) {
        throw new IllegalArgumentException("visibility cannot be null");
    }

    this.visibility = visibility;
}

/**
 * @return {@link Drawable} rendered behind this {@link Widget}
 */
@Nullable
public Drawable getBackground() {
    return background;
}

/**
 * Sets the background to a given {@link Drawable}, or removes the background in the case which
 * a {@code null} reference is passed. If the background has padding, this
 * {@linkplain Widget widget}'s padding is set to the background's padding. However, when a
 * background is removed, this {@linkplain Widget widget}'s padding isn't touched. If setting the
 * padding is desired, please use setPadding(int, int, int, int).
 *
 * @param background {@link Drawable} to render behind this {@link Widget}.
 */
public void setBackground(@Nullable Drawable background) {
    throw new UnsupportedOperationException();
}

/**
 * @return {@linkplain WidgetParent parent} containing this {@link Widget}, or {@code null} if no
 *         parent is set
 */
@Nullable
public final WidgetParent getParent() {
    return parent;
}

/**
 * @param parent {@linkplain WidgetParent parent} container of this {@link Widget}
 */
final void setParent(WidgetParent parent) {
    this.parent = parent;
}

/**
 * @return {@link Window} containing this {@link Widget}
 */
public final Window getWindow() {
    WidgetParent parent = getParent();
    while (parent != null) {
        if (parent instanceof Window) {
            return (Window)parent;
        }

        parent = getParent();
    }

    return null;
}

/**
 * Get the {@link WidgetGroup.LayoutParams} associated with this {@link Widget}. All widgets should
 * have layout parameters. These supply parameters to the {@linkplain #getParent() parent} of this
 * widget specifying how it should be arranged. There are many subclasses of
 * {@link WidgetGroup.LayoutParams}, and these correspond to the different subclasses of
 * {@link WidgetGroup} that are responsible for arranging their children. This method may return
 * {@code null} if this {@link Widget} is not attached to a parent {@link WidgetGroup} or
 * {@link #setLayoutParams(WidgetGroup.LayoutParams)} was not invoked successfully. When a
 * {@link Widget} is attached to a parent {@link WidgetGroup}, this method must not return
 * {@code null}.
 *
 * @return {@link WidgetGroup.LayoutParams} instance associated with this {@link Widget},
 *         or {@code null} if no parameters have been set yet
 */
@Nullable
public WidgetGroup.LayoutParams getLayoutParams() {
    return layoutParams;
}

/**
 * Sets the layout parameters associated with this view. These supply parameters to the parent of
 * this view specifying how it should be arranged. There are many subclasses of
 * {@link WidgetGroup.LayoutParams}, and these correspond to the different subclasses of
 * {@link WidgetGroup} that are responsible for arranging their children.
 *
 * @param params layout parameters for this {@link Widget}, cannot be {@code null}
 */
public void setLayoutParams(@NonNull WidgetGroup.LayoutParams params) {
    if (params == null) {
        throw new IllegalArgumentException("params cannot be null");
    }

    this.layoutParams = params;
}

/**
 * @return Floating-point representation of the z-axis location of this {@link Widget}, used to
 *         determine height of a component relative to its {@linkplain #getParent() parent}.
 */
@FloatRange(from = 0.0, to = Float.MAX_VALUE)
public float getElevation() {
    return elevation;
}

/**
 * @param elevation Floating-point representation for the {@linkplain #getElevation() elevation} of
 *                  this {@link Widget}, used to determine height of a component relative to its
 *                  {@linkplain #getParent() parent}.
 */
public void setElevation(@FloatRange(from = 0.0, to = Float.MAX_VALUE) float elevation) {
    if (elevation < 0.0f) {
        throw new IllegalArgumentException(
                "elevation must be between 0.0 and " + Float.MAX_VALUE + " (inclusive)");
    }

    this.elevation = elevation;
}

/**
 * Implementation of {@link Comparable#compareTo(Object)} which compares {@link Widget} instances
 * based on their {@linkplain #getElevation() elevation}.
 *
 * {@inheritDoc}
 */
@Override
public int compareTo(Widget other) {
    return Float.compare(this.getElevation(), other.getElevation());
}

public Object getTag() { throw new UnsupportedOperationException(); }
public Widget getRootWidget() { throw new UnsupportedOperationException(); }

public float getX() { throw new UnsupportedOperationException(); }
public void setX(float x) { throw new UnsupportedOperationException(); }

public float getY() { throw new UnsupportedOperationException(); }
public void setY(float y) { throw new UnsupportedOperationException(); }

public final int getWidth() { throw new UnsupportedOperationException(); }
public final int getHeight() { throw new UnsupportedOperationException(); }

}
