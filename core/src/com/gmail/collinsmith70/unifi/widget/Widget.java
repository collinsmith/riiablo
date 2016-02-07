package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;

public abstract class Widget {

/**
 * {@link Widget} will be sized large enough to fit all of its components
 */
public static final int WRAP_CONTENT = 0;

/**
 * {@link Widget} will be sized as large as the {@linkplain #getParent() parent} allows
 */
public static final int FILL_PARENT = -1;

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

public boolean hasFocus() { throw new UnsupportedOperationException(); }
public boolean hasFocusable() { throw new UnsupportedOperationException(); }

public boolean isFocused() { throw new UnsupportedOperationException(); }
public final boolean isFocusable() { throw new UnsupportedOperationException(); }

public final Window getWindow() { throw new UnsupportedOperationException(); }

public Object getTag() { throw new UnsupportedOperationException(); }
public final WidgetParent getParent() { throw new UnsupportedOperationException(); }
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
