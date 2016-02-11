package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Widget {

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
 * Reference to the {@linkplain WidgetParent parent} of this {@link Widget}
 */
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
 */
@Nullable
private WidgetGroup.LayoutParams layoutParams;

public Widget() {
    setEnabled(true);
    setVisibility(Visibility.VISIBLE);
}

/**
 * @return {@code true} if this {@link Widget} is enabled. Interpretation varies by subclass.
 *
 * @see #setEnabled(boolean)
 */
public boolean isEnabled() {
    return enabled;
}

/**
 * @param enabled {@code true} to enable this {@link Widget}, otherwise {@code false}.
 *                Interpretation varies by subclass.
 *
 * @see #isEnabled()
 */
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

public final WidgetParent getParent() {
    return parent;
}

final void setParent(WidgetParent parent) {
    this.parent = parent;
}

public boolean hasFocus() { throw new UnsupportedOperationException(); }
public boolean hasFocusable() { throw new UnsupportedOperationException(); }

public boolean isFocused() { throw new UnsupportedOperationException(); }
public final boolean isFocusable() { throw new UnsupportedOperationException(); }

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

public Object getTag() { throw new UnsupportedOperationException(); }
public Widget getRootWidget() { throw new UnsupportedOperationException(); }

public float getX() { throw new UnsupportedOperationException(); }
public void setX(float x) { throw new UnsupportedOperationException(); }

public float getY() { throw new UnsupportedOperationException(); }
public void setY(float y) { throw new UnsupportedOperationException(); }

public final int getWidth() { throw new UnsupportedOperationException(); }
public final int getHeight() { throw new UnsupportedOperationException(); }

public final int getBottom() { throw new UnsupportedOperationException(); }
public final int getLeft() { throw new UnsupportedOperationException(); }
public final int getRight() { throw new UnsupportedOperationException(); }
public final int getTop() { throw new UnsupportedOperationException(); }

public int getPaddingBottom() { throw new UnsupportedOperationException(); }
public int getPaddingLeft() { throw new UnsupportedOperationException(); }
public int getPaddingRight() { throw new UnsupportedOperationException(); }
public int getPaddingTop() { throw new UnsupportedOperationException(); }
public void setPadding(int left, int top, int right, int bottom) { throw new UnsupportedOperationException(); }

}
