package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.gmail.collinsmith70.unifi.unifi.math.Boundary;
import com.gmail.collinsmith70.unifi.unifi.math.Dimension2D;
import com.gmail.collinsmith70.unifi.unifi.math.Point2D;

import java.util.EnumSet;
import java.util.Set;

public class Widget
        implements Bounded, Paddable, Translateable {

  /**
   * Enumeration of all boolean state fields for a {@code Widget}.
   */
  private enum Flag {
    /**
     * @see #isDebug()
     */
    DEBUG,

    /**
     * @see #isDown()
     */
    DOWN,

    /**
     * @see #isEnabled()
     */
    ENABLED,

    /**
     * @see #isFocusable()
     */
    FOCUSABLE,

    /**
     * @see #isOver()
     */
    OVER
  }

  /**
   * Enumeration of all {@code Widget} visibility states.
   */
  public enum Visibility {
    /**
     * Default {@code Widget} behavior. {@code Widget} will participate in layout, input events,
     * and render.
     */
    VISIBLE,

    /**
     * {@code Widget} will participate in layout, however it will not receive input events or
     * render.
     */
    INVISIBLE,

    /**
     * {@code Widget} will not participate in layout, receive input events, or render.
     */
    GONE
  }

  @NonNull private final Set<Flag> FLAGS;

  @Nullable private WidgetParent parent;

  @NonNull private Visibility visibility;

  private int bottom;
  private int left;
  private int right;
  private int top;

  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingBottom;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingLeft;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingRight;
  @IntRange(from = 0, to = Integer.MAX_VALUE) private int paddingTop;

  public Widget() {
    this.FLAGS = EnumSet.noneOf(Flag.class);

    setEnabled(true);
    setVisibility(Visibility.VISIBLE);
  }

  protected void draw(@NonNull final Batch batch) {
    assert batch != null : "batch should not be null";
    drawBackground(batch);
    if (isDebug()) {
      drawDebug(batch);
    }
  }

  protected void drawBackground(@NonNull final Batch batch) {
    assert batch != null : "batch should not be null";
  }

  protected void drawDebug(@NonNull final Batch batch) {
    assert batch != null : "batch should not be null";
  }

  /**
   * Parent of this {@code Widget}, typically defined as the container.
   *
   * @return Parent container of this {@code Widget}
   */
  @Nullable
  public final WidgetParent getParent() {
    return parent;
  }

  /**
   * Sets the parent container of this {@code Widget}.
   *
   * @param parent Parent container of this {@code Widget}
   */
  protected final void setParent(@Nullable final WidgetParent parent) {
    this.parent = parent;
  }

  /**
   * Checks whether or not this {@code Widget} has a parent container.
   *
   * @return {@code true} if this {@code Widget} belongs to a parent container,
   *         otherwise {@code false}
   */
  public final boolean hasParent() {
    return getParent() != null;
  }

  /**
   * {@link Window} instance containing this {@code Widget}.
   *
   * @return {@code Window} instance containing this {@code Widget}, otherwise {@code null} if this
   *         {@code Widget} has yet to be added to any {@code Window}
   */
  @Nullable
  public final Window getWindow() {
    for (WidgetParent parent = getParent(); parent != null; parent = parent.getParent()) {
      if (parent instanceof Window) {
        return (Window)parent;
      }
    }

    return null;
  }

  /**
   * {@link Visibility} state of this {@code Widget} which determines how this {@code Widget} will
   * render and behave within its {@linkplain #getParent parent} container.
   *
   * @return {@code Visibility} state of this {@code Widget}
   */
  @NonNull
  @CallSuper
  public Visibility getVisibility() {
    return visibility;
  }

  /**
   * Sets the {@link Visibility} state of this {@code Widget}.
   *
   * @param visibility {@code Visibility} state of this {@code Widget}
   */
  @CallSuper
  public void setVisibility(@NonNull final Visibility visibility) {
    if (visibility == null) {
      throw new IllegalArgumentException("visibility cannot be null");
    }

    this.visibility = visibility;
  }

  /**
   * Checks whether or not this {@code Widget} is in a debug state and will log or draw additional
   * information regarding its state.
   *
   * @return {@code true} if this {@code Widget} is in a debugging state, otherwise {@code false}
   */
  @CallSuper
  public boolean isDebug() {
    return FLAGS.contains(Flag.DEBUG);
  }

  /**
   * Sets whether or not this {@code Widget} is in a debugging state and will log or draw additional
   * information regarding its state.
   *
   * @param debug {@code true} to set this {@code Widget} in a debugging state,
   *              otherwise {@code false}
   */
  @CallSuper
  public void setDebug(final boolean debug) {
    if (debug) {
      FLAGS.add(Flag.DEBUG);
    } else {
      FLAGS.remove(Flag.DEBUG);
    }
  }

  /**
   * Checks whether or not this {@code Widget} has an input device (e.g., mouse cursor) hovering
   * over it in a pressed state. If this methods returns {@code true}, then {@link #isOver()} must
   * also return {@code true}.
   *
   * @return {@code true} if it is, otherwise {@code false}
   */
  @CallSuper
  boolean isDown() {
    assert !FLAGS.contains(Flag.DOWN) || FLAGS.contains(Flag.DOWN) == isOver();
    return FLAGS.contains(Flag.DOWN);
  }

  /**
   * Sets whether or not this {@code Widget} has an input device over it in a pressed state. If
   * {@code true} is passed into this method, then {@link #setOver(boolean)} will also be set to
   * {@code true}.
   *
   * @param down {@code true} if it is, otherwise {@code false}
   */
  private void setDown(final boolean down) {
    if (down) {
      setOver(true);
      FLAGS.add(Flag.DOWN);
    } else {
      FLAGS.remove(Flag.DOWN);
    }
  }

  /**
   * Checks whether or not this {@code Widget} is enabled. Interpretation on what exactly enabled
   * means varies by subclass, but generally this effects whether or not the state of the
   * {@code Widget} is mutable.
   *
   * @return {@code true} implies that this {@code Widget} is enabled, otherwise {@code false}
   */
  @CallSuper
  public boolean isEnabled() {
    return FLAGS.contains(Flag.ENABLED);
  }

  /**
   * Sets whether or not this {@code Widget} is enabled. Interpretation on what exactly enabled
   * means varies by subclass, but generally this effects whether or not the state of the
   * {@code Widget} is mutable.
   *
   * @param enabled {@code true} to enabled this {@code Widget}, otherwise {@code false}
   */
  @CallSuper
  public void setEnabled(final boolean enabled) {
    if (enabled) {
      FLAGS.add(Flag.ENABLED);
    } else {
      FLAGS.remove(Flag.ENABLED);
    }
  }

  /**
   * Checks whether or not this {@code Widget} is focusable and will respond to input events which
   * require focus.
   *
   * @return {@code true} if this {@code Widget} can respond to focus events,
   *         otherwise {@code false}
   */
  @CallSuper
  public boolean isFocusable() {
    return FLAGS.contains(Flag.FOCUSABLE);
  }

  /**
   * Sets whether or not this {@code Widget} is focusable and will respond to input events which
   * require focus.
   *
   * @param focusable {@code true} if this {@code Widget} should respond to focus events,
   *                  otherwise {@code false}
   */
  @CallSuper
  public void setFocusable(final boolean focusable) {
    if (focusable) {
      FLAGS.add(Flag.FOCUSABLE);
    } else {
      FLAGS.remove(Flag.FOCUSABLE);
    }
  }

  /**
   * Checks whether or not this {@code Widget} has an input device (e.g., mouse cursor) hovering
   * over.
   *
   * @return {@code true} implies that this {@code Widget} has an input device over it,
   *         otherwise {@code false}
   */
  @CallSuper
  boolean isOver() {
    return FLAGS.contains(Flag.OVER);
  }

  /**
   * Sets whether or not this {@code Widget} has an input device over it.
   *
   * @param over {@code true} if it does, otherwise {@code false}
   */
  private void setOver(final boolean over) {
    if (over) {
      FLAGS.add(Flag.OVER);
    } else {
      FLAGS.remove(Flag.OVER);
    }
  }

  /**
   * Translates the location of this {@code Widget} so that the bottom edge is the specified value
   * and the size of the {@code Widget} remains unchanged.
   *
   * @param bottom Bottom of this {@code Widget}, in pixels
   */
  public final void translateBottom(final int bottom) {
    final int height = getHeight();
    this.bottom = bottom;
    this.top = bottom + height;
  }

  /**
   * Translates the location of this {@code Widget} so that the left edge is the specified value and
   * the size of the {@code Widget} remains unchanged.
   *
   * @param left Left of this {@code Widget}, in pixels
   */
  public final void translateLeft(final int left) {
    final int width = getWidth();
    this.left = left;
    this.right = left + width;
  }

  /**
   * Translates the location of this {@code Widget} so that the right edge is the specified value
   * and the size of the {@code Widget} remains unchanged.
   *
   * @param right Right of this {@code Widget}, in pixels
   */
  public final void translateRight(final int right) {
    final int width = getWidth();
    this.right = right;
    this.left = right - width;
  }

  /**
   * Translates the location of this {@code Widget} so that the top edge is the specified value and
   * the size of the {@code Widget} remains unchanged.
   *
   * @param top Top of this {@code Widget}, in pixels
   */
  public final void translateTop(final int top) {
    final int height = getHeight();
    this.top = top;
    this.bottom = top - height;
  }

  /**
   * Translates the location of this {@code Widget} horizontally so that it is centered about the
   * given point on the {@code x}-axis and the size of the {@code Widget} remains unchanged.
   *
   * @param x Visual {@code x} position to center this {@code Widget} at on its {@code x}-axis,
   *          in pixels
   */
  public final void translateHorizontalCenter(final int x) {
    final int width = getWidth();
    translateLeft(x - (width / 2));
  }

  /**
   * Translates the location of this {@code Widget} vertically so that it is centered about the
   * given point on the {@code y}-axis and the size of the {@code Widget} remains unchanged.
   *
   * @param y Visual {@code y} position to center this {@code Widget} at on its {@code y}-axis,
   *          in pixels
   */
  public final void translateVerticalCenter(final int y) {
    final int height = getHeight();
    translateBottom(y - (height / 2));
  }

  /**
   * Translates the location of this {@code Widget} so that it is centered about the given point
   * and the size of the {@code Widget} remains unchanged.
   *
   * @param x Visual {@code x} position to center this {@code Widget} at on its {@code x}-axis,
   *          in pixels
   * @param y Visual {@code y} position to center this {@code Widget} at on its {@code y}-axis,
   *          in pixels
   */
  public final void translateCenter(final int x, final int y) {
    translateHorizontalCenter(x);
    translateVerticalCenter(y);
  }

  /**
   * Translates the location of this {@code Widget} so that it is centered about the given
   * coordinates of the specified {@link Point2D} instance.
   *
   * @param point {@code Point2D} consisting of the specified coordinates to center this
   *              {@code Widget} about
   */
  public final void translateCenter(@NonNull final Point2D point) {
    if (point == null) {
      throw new IllegalArgumentException("point cannot be null");
    }

    translateCenter(point.getX(), point.getY());
  }

  /**
   * Bottom position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Bottom of this {@code Widget}, in pixels
   */
  @Override
  public final int getBottom() {
    return bottom;
  }

  /**
   * Sets the bottom position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified bottom is greater than the current {@linkplain #getTop top} of this
   *         {@code Widget}, then the top is set to the same value as the specified bottom.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param bottom Bottom of this {@code Widget}, in pixels.
   */
  @Override
  public final void setBottom(final int bottom) {
    if (getTop() < bottom) {
      this.top = bottom;
    }

    this.bottom = bottom;
  }

  /**
   * Left position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Left of this {@code Widget}, in pixels
   */
  @Override
  public final int getLeft() {
    return left;
  }

  /**
   * Sets the left position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified left is greater than the current {@linkplain #getRight right} of this
   *         {@code Widget}, then the right is set to the same value as the specified left.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param left Left of this {@code Widget}, in pixels.
   */
  @Override
  public final void setLeft(final int left) {
    if (getRight() < left) {
      this.right = left;
    }

    this.left = left;
  }

  /**
   * Right position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Right of this {@code Widget}, in pixels
   */
  @Override
  public final int getRight() {
    return right;
  }

  /**
   * Sets the right position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified right is less than the current {@linkplain #getLeft left} of this
   *         {@code Widget}, then the left is set to the same value as the specified right.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param right Right of this {@code Widget}, in pixels.
   */
  @Override
  public final void setRight(final int right) {
    if (right < getLeft()) {
      this.left = right;
    }

    this.right = right;
  }

  /**
   * Top position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @return Top of this {@code Widget}, in pixels
   */
  @Override
  public final int getTop() {
    return top;
  }

  /**
   * Sets the top position of this {@code Widget} relative to its {@linkplain #getParent parent}.
   * <p>
   *   Note: If the specified top is less than the current {@linkplain #getBottom bottom} of this
   *         {@code Widget}, then the bottom is set to the same value as the specified top.
   * </p>
   * <p>
   *   Note: This method is meant to be called by the layout system and should not generally be
   *         called otherwise, because the property may be changed at any time by the layout.
   * </p>
   *
   * @param top Top of this {@code Widget}, in pixels.
   */
  @Override
  public final void setTop(final int top) {
    if (top < getBottom()) {
      this.bottom = top;
    }

    this.top = top;
  }

  /**
   * {@link Boundary} containing of the positions of this {@code Widget} relative to its
   * {@linkplain #getParent parent}.
   * <p>
   *   Note: Changing the sides of the returned {@code Boundary} instance will not be reflected
   *         within this {@code Widget}.
   * </p>
   *
   * @return {@code Boundary} containing the positions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Boundary getBoundary() {
    return new Boundary(getLeft(), getTop(), getRight(), getBottom());
  }

  /**
   * Populates the passed {@link Boundary} instance with the positions of this {@code Widget}
   * relative to its {@linkplain #getParent parent}.
   *
   * @param dst {@code Boundary} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getBoundary} were
   *            called.
   *
   * @return {@code Boundary} containing the positions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Boundary getBoundary(@Nullable final Boundary dst) {
    if (dst == null) {
      return getBoundary();
    }

    dst.set(getLeft(), getTop(), getRight(), getBottom());
    return dst;
  }

  /**
   * Sets the positions of all sides of this {@code Widget} relative to its
   * {@linkplain #getParent parent}.
   * <p>
   *   Precondition: {@code left <= right AND bottom <= top}
   * </p>
   *
   * @param left   Left of this {@code Widget}, in pixels.
   * @param top    Top of this {@code Widget}, in pixels.
   * @param right  Right of this {@code Widget}, in pixels.
   * @param bottom Bottom of this {@code Widget}, in pixels.
   */
  @Override
  public final void setBoundary(final int left, final int top, final int right, final int bottom) {
    if (right < left) {
      throw new IllegalArgumentException("left <= right");
    } else if (top < bottom) {
      throw new IllegalArgumentException("bottom <= top");
    }

    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  /**
   * Sets the positions of all sides of this {@code Widget} relative to its
   * {@linkplain #getParent parent} from the specified source {@link Boundary}.
   * <p>
   *   Precondition: {@code src.getLeft() <= src.getRight() AND src.getBottom() <= src.getTop()}
   * </p>
   *
   * @param src {@code Boundary} to set the side positions of this {@code Widget} to
   */
  @Override
  public final void setBoundary(@NonNull final Boundary src) {
    if (src == null) {
      throw new IllegalArgumentException("src cannot be null");
    }

    setBoundary(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  /**
   * Width of this {@code Widget}
   *
   * @return Width of this {@code Widget}, in pixels
   */
  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public final int getWidth() {
    return getRight() - getLeft();
  }

  /**
   * Height of this {@code Widget}
   *
   * @return Height of this {@code Widget}, in pixels
   */
  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public final int getHeight() {
    return getTop() - getBottom();
  }

  /**
   * {@link Dimension2D} containing the {@linkplain #getWidth width} and
   * {@linkplain #getHeight height} dimensions of this {@code Widget}.
   *
   * @return {@code Dimension2D} containing the dimensions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Dimension2D getSize() {
    return new Dimension2D(getWidth(), getHeight());
  }

  /**
   * Populates the passed {@link Dimension2D} instance with the {@linkplain #getWidth width} and
   * {@linkplain #getHeight height} dimensions of this {@code Widget}.
   *
   * @param dst {@code Dimension2D} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getSize} were
   *            called.
   *
   * @return {@code Dimension2D} containing the dimensions of this {@code Widget}
   */
  @NonNull
  @Override
  public final Dimension2D getSize(@Nullable final Dimension2D dst) {
    if (dst == null) {
      return getSize();
    }

    dst.set(getWidth(), getHeight());
    return dst;
  }

  /**
   * Checks whether or not the size of this {@code Widget} is positive. The size of a {@code Widget}
   * is said to be positive when the {@linkplain #getRight right} edge is greater than the
   * {@linkplain #getLeft left} edge, and the {@linkplain #getTop top} edge is greater than the
   * {@linkplain #getBottom bottom} edge.
   *
   * @return {@code true} if this {@code Widget} has size, otherwise {@code false}
   */
  @Override
  public final boolean hasSize() {
    return getRight() > getLeft()
            && getTop() > getBottom();
  }

  /**
   * {@code X}-axis offset of this {@code Widget} used to calculate world coordinates from relative
   * ones.
   *
   * @return {@code X}-axis offset of this {@code Widget}, in pixels
   */
  public int getTranslationX() {
    if (!hasParent()) {
      return 0;
    }

    return getParent().getX();
  }

  /**
   * {@code Y}-axis offset of this {@code Widget} used to calculate world coordinates from relative
   * ones.
   *
   * @return {@code Y}-axis offset of this {@code Widget}, in pixels
   */
  public int getTranslationY() {
    if (!hasParent()) {
      return 0;
    }

    return getParent().getY();
  }

  /**
   * {@link Point2D} containing the translation's {@linkplain #getTranslationX x} and
   * {@linkplain #getTranslationY y} coordinates of this {@code Widget}.
   *
   * @return {@code Point2D} containing the translation offset of this {@code Widget}
   */
  @Override
  @NonNull
  public final Point2D getTranslation() {
    return new Point2D(getTranslationX(), getTranslationY());
  }

  /**
   * Populates the passed {@link Point2D} instance with the translation coordinates of this
   * {@code Widget}.
   *
   * @param dst {@code Point2D} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getTranslation} were
   *            called.
   *
   * @return {@code Point2D} containing the translation offset of this {@code Widget}
   */
  @Override
  @NonNull
  public final Point2D getTranslation(@Nullable final Point2D dst) {
    if (dst == null) {
      return getTranslation();
    }

    dst.set(getTranslationX(), getTranslationY());
    return dst;
  }

  /**
   * The visual {@code x} position of this {@code Widget}, in pixels. This is equivalent to the
   * {@linkplain #getTranslationX translationX} property plus the current {@linkplain #getLeft left}
   * property.
   *
   * @return Visual {@code x} position of this {@code Widget}, in pixels.
   */
  @Override
  @CallSuper
  public int getX() {
    return getTranslationX() + getLeft();
  }

  /**
   * Sets the visual {@code x} position of this {@code Widget} relative to its parent. This is
   * equivalent to setting the {@linkplain #getLeft left} property to be the {@code x} value
   * passed in and the {@linkplain #getRight right} property to be the {@code x} value passed in
   * plus the current {@linkplain #getWidth width} of this {@code Widget}.
   *
   * @param x Visual {@code x} position of this {@code Widget} relative to its parent, in pixels
   */
  @Override
  @CallSuper
  public void setX(final int x) {
    final int width = getWidth();
    this.left = x;
    this.right = x + width;
  }

  /**
   * The visual {@code y} position of this {@code Widget}, in pixels. This is equivalent to the
   * {@linkplain #getTranslationY translationY} property plus the current
   * {@linkplain #getBottom bottom} property.
   *
   * @return Visual {@code y} position of this {@code Widget}, in pixels.
   */
  @Override
  @CallSuper
  public int getY() {
    return getTranslationY() + getBottom();
  }

  /**
   * Sets the visual {@code y} position of this {@code Widget} relative to its parent. This is
   * equivalent to setting the {@linkplain #getBottom bottom} property to be the {@code y} value
   * passed in and the {@linkplain #getTop top} property to be the {@code y} value passed in
   * plus the current {@linkplain #getHeight height} of this {@code Widget}.
   *
   * @param y Visual {@code y} position of this {@code Widget} relative to its parent, in pixels
   */
  @Override
  @CallSuper
  public void setY(final int y) {
    final int height = getHeight();
    this.bottom = y;
    this.top = y + height;
  }

  /**
   * Checks whether or not a given point lies within the boundary of this {@code Widget}.
   * <p>
   *   Note: Coordinates are to be given in relative terms to the {@linkplain #getParent parent} of
   *         this {@code Widget} (i.e., same coordinate scheme as in {@link #getBoundary()})
   * </p>
   * <p>
   *   Note: This method does not take in consideration the state of this {@code Widget}.
   * </p>
   *
   * @param x Relative location of the point on the {@code x}-axis
   * @param y Relative location of the point on the {@code y}-axis
   *
   * @return {@code true} if the coordinates are within the boundary of this {@code Widget},
   *         otherwise {@code false}
   */
  @Override
  public boolean contains(final int x, final int y) {
    return getLeft() <= x && x <= getRight()
            && getBottom() <= y && y <= getTop();
  }

  /**
   * Checks whether or not a given {@link Point2D} lies within the boundary of this {@code Widget}.
   * <p>
   *   Note: Coordinates are to be given in relative terms to the {@linkplain #getParent parent} of
   *         this {@code Widget} (i.e., same coordinate scheme as in {@link #getBoundary()})
   * </p>
   * <p>
   *   Note: This method does not take in consideration the state of this {@code Widget}.
   * </p>
   *
   * @param point {@code Point2D} instance containing the relative coordinates to test
   *
   * @return {@code true} if the coordinates represented by the {@code Point2D} are within the
   *         boundary of this {@code Widget}, otherwise {@code false}
   */
  @Override
  public boolean contains(@NonNull final Point2D point) {
    if (point == null) {
      throw new IllegalArgumentException("point cannot be null");
    }

    return contains(point.getX(), point.getY());
  }

  /**
   * {@link Point2D} containing the visual {@linkplain #getX x} and {@linkplain #getY y} coordinates
   * of this {@code Widget}.
   *
   * @return {@code Point2D} containing the position of this {@code Widget}
   */
  @Override
  @NonNull
  @CallSuper
  public final Point2D getPosition() {
    return new Point2D(getX(), getY());
  }

  /**
   * Populates the passed {@link Point2D} instance with the visual {@linkplain #getX x} and
   * {@linkplain #getY y} coordinates of this {@code Widget}.
   *
   * @param dst {@code Point2D} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getPosition} were
   *            called.
   *
   * @return {@code Point2D} containing the virtual coordinates of this {@code Widget}
   */
  @Override
  @NonNull
  @CallSuper
  public final Point2D getPosition(@Nullable Point2D dst) {
    if (dst == null) {
      return getPosition();
    }

    dst.set(getX(), getY());
    return dst;
  }

  /**
   * Sets the visual {@linkplain #getX x} and {@linkplain #getY y} coordinates of this
   * {@code Widget} relative to its {@linkplain #getParent parent}.
   *
   * @param x Visual {@code x} position of this {@code Widget} relative to its parent, in pixels
   * @param y Visual {@code y} position of this {@code Widget} relative to its parent, in pixels
   */
  @Override
  @CallSuper
  public final void setPosition(final int x, final int y) {
    setX(x);
    setY(y);
  }

  /**
   * Sets the visual {@linkplain #getX x} and {@linkplain #getY y} coordinates of this
   * {@code Widget} relative to its {@linkplain #getParent parent} from the specified source
   * {@link Point2D}.
   *
   * @param src {@code Point2D} to set the {@linkplain #getX x} and {@linkplain #getY y} coordinates
   *            of this {@code Widget} to
   */
  @Override
  @CallSuper
  public final void setPosition(@NonNull final Point2D src) {
    if (src == null) {
      throw new IllegalArgumentException("src cannot be null");
    }

    setPosition(src.getX(), src.getY());
  }

  /**
   * Bottom padding of this {@code Widget}. Padding is defined as the space between the edge of a
   * {@code Widget} and its contents.
   *
   * @return Bottom padding, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getPaddingBottom() {
    return paddingBottom;
  }

  /**
   * Sets the bottom padding for this {@code Widget}. Padding is defined as the space between the
   * edge of a {@code Widget} and its contents.
   *
   * @param paddingBottom Bottom padding, in pixels
   */
  @CallSuper
  public void setPaddingBottom(@IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingBottom) {
    if (paddingBottom < 0) {
      throw new IllegalArgumentException("paddingBottom must be greater than or equal to 0");
    }

    this.paddingBottom = paddingBottom;
  }

  /**
   * Left padding of this {@code Widget}. Padding is defined as the space between the edge of a
   * {@code Widget} and its contents.
   *
   * @return Left padding, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getPaddingLeft() {
    return paddingLeft;
  }

  /**
   * Sets the left padding for this {@code Widget}. Padding is defined as the space between the
   * edge of a {@code Widget} and its contents.
   *
   * @param paddingLeft Left padding, in pixels
   */
  @CallSuper
  public void setPaddingLeft(@IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingLeft) {
    if (paddingLeft < 0) {
      throw new IllegalArgumentException("paddingLeft must be greater than or equal to 0");
    }

    this.paddingLeft = paddingLeft;
  }

  /**
   * Right padding of this {@code Widget}. Padding is defined as the space between the edge of a
   * {@code Widget} and its contents.
   *
   * @return Right padding, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getPaddingRight() {
    return paddingRight;
  }

  /**
   * Sets the right padding for this {@code Widget}. Padding is defined as the space between the
   * edge of a {@code Widget} and its contents.
   *
   * @param paddingRight Right padding, in pixels
   */
  @CallSuper
  public void setPaddingRight(@IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingRight) {
    if (paddingRight < 0) {
      throw new IllegalArgumentException("paddingRight must be greater than or equal to 0");
    }

    this.paddingRight = paddingRight;
  }

  /**
   * Top padding of this {@code Widget}. Padding is defined as the space between the edge of a
   * {@code Widget} and its contents.
   *
   * @return Top padding, in pixels
   */
  @Override
  @CallSuper
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getPaddingTop() {
    return paddingTop;
  }

  /**
   * Sets the top padding for this {@code Widget}. Padding is defined as the space between the
   * edge of a {@code Widget} and its contents.
   *
   * @param paddingTop Top padding, in pixels
   */
  @CallSuper
  public void setPaddingTop(@IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingTop) {
    if (paddingTop < 0) {
      throw new IllegalArgumentException("paddingTop must be greater than or equal to 0");
    }

    this.paddingTop = paddingTop;
  }

  /**
   * {@link Boundary} containing the padding on each side of this {@code Widget}. Padding is defined
   * as the space between the edge of a {@code Widget} and its contents.
   *
   * @return {@code Boundary} containing the padding of this {@code Widget}
   */
  @NonNull
  @CallSuper
  public final Boundary getPadding() {
    return new Boundary(getLeft(), getTop(), getRight(), getBottom());
  }

  /**
   * Populates the passed {@link Boundary} instance with the padding of this {@code Widget}. Padding
   * is defined as the space between the edge of a {@code Widget} and its contents.
   *
   * @param dst {@code Boundary} instance to populate, otherwise if a {@code null} reference is
   *            passed, then this method would behave the same as if {@link #getPadding} were
   *            called.
   *
   * @return {@code Boundary} containing the padding of this {@code Widget}
   */
  @NonNull
  @CallSuper
  public final Boundary getPadding(@Nullable final Boundary dst) {
    if (dst == null) {
      return getPadding();
    }

    dst.set(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    return dst;
  }

  /**
   * Sets the padding on all sides of this {@code Widget}. Padding is defined as the space between
   * the edge of a {@code Widget} and its contents.
   * <p>
   *   Precondition: {@code paddingLeft >= 0 AND paddingRight >= 0 AND paddingBottom >= 0
   *                        AND paddingTop >= 0}
   * </p>
   *
   * @param paddingLeft   Left padding, in pixels
   * @param paddingTop    Top padding, in pixels
   * @param paddingRight  Right padding, in pixels
   * @param paddingBottom Bottom padding, in pixels
   */
  @CallSuper
  public final void setPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingLeft,
                               @IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingTop,
                               @IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingRight,
                               @IntRange(from = 0, to = Integer.MAX_VALUE) final int paddingBottom) {
    setPaddingLeft(paddingLeft);
    setPaddingTop(paddingTop);
    setPaddingRight(paddingRight);
    setPaddingBottom(paddingBottom);
  }

  /**
   * Sets the padding on all sides of this {@code Widget} to those of the source {@link Boundary}.
   * Padding is defined as the space between the edge of a {@code Widget} and its contents.
   * <p>
   *   Precondition: {@code src.getLeft() >= 0 AND src.getRight() >= 0 AND src.getBottom() >= 0
   *                        AND src.getTop() >= 0}
   * </p>
   *
   * @param src {@code Boundary} to copy the padding onto this {@code Widget}
   */
  @CallSuper
  public final void setPadding(@NonNull final Boundary src) {
    setPadding(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
  }

  /**
   * Sets the padding on all sides of this {@code Widget} to the specified value. This method would
   * be the same as calling {@link #setPadding(int, int, int, int)} with all the same parameter.
   * Padding is defined as the space between the edge of a {@code Widget} and its contents.
   *
   * @param padding Padding, in pixels
   */
  @CallSuper
  public final void setPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) final int padding) {
    setPadding(padding, padding, padding, padding);
  }

  /**
   * Checks whether or not at least one side of this {@code Widget} has a positive padding value.
   * Padding is defined as the space between the edge of a {@code Widget} and its contents.
   *
   * @return {@code true} if at least one side of this {@code Widget} has a positive padding value,
   *         otherwise {@code false}
   */
  @Override
  public final boolean hasPadding() {
    return getPaddingLeft() > 0 || getPaddingTop() > 0
            || getPaddingRight() > 0 || getPaddingBottom() > 0;
  }

}
