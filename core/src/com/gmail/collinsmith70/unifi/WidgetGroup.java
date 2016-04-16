package com.gmail.collinsmith70.unifi;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Paint;
import com.gmail.collinsmith70.unifi.graphics.drawable.Drawable;
import com.gmail.collinsmith70.unifi.graphics.drawable.ShapeDrawable;
import com.gmail.collinsmith70.unifi.graphics.drawable.shape.RectangularShape;
import com.gmail.collinsmith70.unifi.util.LengthUnit;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public abstract class WidgetGroup extends Widget
        implements WidgetParent {

  public static final class LayoutParams {

    /**
     * Determines the width of a {@code Widget} within its parent. Width should be given as one of:
     * <ul>
     *   <li>{@code wrap_content} - (default) {@code Widget} is only large enough to fit its
     *                              children</li>
     *   <li>{@code match_parent} - {@code Widget} will be the same size as its parent (excluding
     *                              padding)</li>
     *   <li>{@code [0-9]+\w*(px|dp|mm|cm|m)} - {@code Widget} will be sized according to the
     *                                          {@linkplain LengthUnit#toPixels(String) specified
     *                                          value}</li>
     * </ul>
     */
    @LayoutParam
    public static final String layout_width = "layout_width";

    /**
     * Determines the height of a {@code Widget} within its parent. Height should be given as one
     * of:
     *
     * <ul>
     *   <li>{@code wrap_content} - (default) {@code Widget} is only large enough to fit its
     *                              children</li>
     *   <li>{@code match_parent} - {@code Widget} will be the same size as its parent (excluding
     *                              padding)</li>
     *   <li>{@code [0-9]+\w*(px|dp|mm|cm|m)} - {@code Widget} will be sized according to the
     *                                          {@linkplain LengthUnit#toPixels(String) specified
     *                                          value}</li>
     * </ul>
     */
    @LayoutParam
    public static final String layout_height = "layout_height";

    /**
     * Determines the weight a {@code Widget} should be given within its parent. Specified as a
     * floating point value between {@code 0.0} and {@code 1.0} (inclusive). Value used will
     * determine the ratio of unused space in the parent to allocate to the component.
     */
    @LayoutParam
    public static final String layout_weight = "layout_weight";

  }

  public enum Gravity {
    /**
     * Gravity flag for top/up on the {@code y}-axis
     */
    TOP,

    /**
     * Gravity flag for bottom/down on the {@code y}-axis
     */
    BOTTOM,

    /**
     * Gravity flag for left on the {@code x}-axis
     */
    LEFT,

    /**
     * Gravity flag for right on the {@code x}-axis
     */
    RIGHT,

    /**
     * Gravity flag for vertical center (middle of {@linkplain #TOP top} and
     * {@linkplain #BOTTOM bottom) on the {@code y}-axis
     */
    CENTER_VERTICAL,

    /**
     * Gravity flag for horizontal center (middle of {@linkplain #LEFT left} and
     * {@linkplain #RIGHT right)) on the {@code x}-axis
     */
    CENTER_HORIZONTAL
  }

  /**
   * Gravity constant representing both {@linkplain Gravity#CENTER_VERTICAL vertical} and
   * {@linkplain Gravity#CENTER_HORIZONTAL horizontal} centers on the {@code x}- and {@code y}-axes
   */
  public static final ImmutableSet<Gravity> CENTER
          = Sets.immutableEnumSet(Gravity.CENTER_VERTICAL, Gravity.CENTER_HORIZONTAL);

  private static final ImmutableSet<Gravity> DEFAULT_GRAVITY
          = ImmutableSet.of(Gravity.TOP, Gravity.LEFT);

  @NonNull
  private final Collection<Widget> children;

  @Nullable
  private Set<Gravity> gravity;

  private static final Drawable DEFAULT_DEBUG_DRAWABLE;
  static {
    ShapeDrawable shapeDrawable = new ShapeDrawable(new RectangularShape());
    shapeDrawable.setPaint(new Paint().setColor(Color.DARK_GRAY));
    DEFAULT_DEBUG_DRAWABLE = shapeDrawable;
  }

  public WidgetGroup() {
    this.children = new ArrayList<Widget>();
    setDebug(DEFAULT_DEBUG_DRAWABLE);
  }

  @Override
  protected void onDraw(@NonNull final Canvas canvas) {
    drawChildren(canvas);
  }

  /**
   * Called when this {@code Widget} should draw its children content onto the passed {@link Canvas}.
   *
   * @param canvas {@code Canvas} instance to render onto
   */
  protected void drawChildren(@NonNull final Canvas canvas) {
    assert canvas != null : "canvas should not be null";
    for (Widget child : this) {
      child.draw(canvas);
    }
  }

  @Override
  protected void setInvalidated(final boolean invalidated) {
    if (children == null) {
      super.setInvalidated(invalidated);
      return;
    }

    if (invalidated == isInvalidated()) {
      return;
    }

    super.setInvalidated(invalidated);
    if (invalidated) {
      for (Widget child : this) {
        child.invalidate();
      }
    }
  }

  /**
   * Gravity of this {@code WidgetGroup}, controlling how children are aligned.
   *
   * @return Gravity of this {@code WidgetGroup}
   */
  @NonNull
  @CallSuper
  public ImmutableSet<Gravity> getGravity() {
    if (gravity == null) {
      return DEFAULT_GRAVITY;
    }

    return ImmutableSet.copyOf(gravity);
  }

  /**
   * Sets the gravity of this {@code WidgetGroup} which controls how child {@link Widget} instances
   * are aligned.
   *
   * @param gravity Gravity of this {@code WidgetGroup}
   */
  @CallSuper
  public void setGravity(@Nullable final Set<Gravity> gravity) {
    this.gravity = gravity;
  }

  /**
   * Sets the gravity of this {@code WidgetGroup} which controls how child {@link Widget} instances
   * are aligned.
   *
   * @param gravity   First gravity argument of this {@code WidgetGroup}
   * @param gravities Remaining gravity arguments of this {@code WidgetGroup}
   */
  @CallSuper
  public void setGravity(@NonNull final Gravity gravity, @NonNull final Gravity... gravities) {
    if (gravity == null) {
      throw new IllegalArgumentException("gravity cannot be null");
    }

    // EnumSet does not permit {@code null} elements, so no need to validate here
    this.gravity = EnumSet.of(gravity, gravities);
  }

  /**
   * Immutable {@link Iterator} which iterates through each child {@link Widget} belonging to this
   * {@code WidgetGroup}.
   *
   * @return Immutable {@code Iterator} which iterates through each child {@link Widget}
   */
  @Override
  @NonNull
  public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  /**
   * Adds the given {@link Widget} to this {@code WidgetGroup}, setting the {@linkplain #getParent
   * parent} of that {@code Widget} to this and removing it from its current parent (if any).
   *
   * @param widget {@code Widget} to add to this {@code WidgetGroup} container
   */
  @Override
  @CallSuper
  public void addWidget(@NonNull final Widget widget) {
    if (widget == null) {
      throw new IllegalArgumentException("child widget cannot be null");
    }

    children.add(widget);
    widget.setParent(this);
    requestLayout();
  }

  /**
   * Checks whether or not the given {@link Widget} belongs to this {@code WidgetGroup}.
   *
   * @param widget {@code Widget} to check
   * @return {@code true} if the given {@link Widget} belongs to this {@code WidgetGroup},
   * otherwise {@code false}
   */
  @Override
  @CallSuper
  public boolean containsWidget(@Nullable Widget widget) {
    assert !children.contains(widget) || widget.getParent() == this;
    return widget != null && children.contains(widget);
  }

  /**
   * Removes the given {@link Widget} from this {@code WidgetGroup} if it belongs to it.
   *
   * @param widget {@code Widget} to remove from this {@code WidgetGroup} container
   * @return {@code true} if the {@code Widget} was successfully removed, otherwise {@code false}
   */
  @Override
  @CallSuper
  public boolean removeWidget(@Nullable Widget widget) {
    if (widget == null) {
      return false;
    } else if (widget.getParent() != this) {
      assert !containsWidget(widget)
              : "widget parent is not this WidgetGroup, so this WidgetGroup should not contain it";
      return false;
    }

    widget.setParent(null);
    boolean removed = children.remove(widget);
    requestLayout();
    assert removed : "widget parent was this WidgetGroup but was not a child";
    return removed;
  }

  /**
   * Total number of {@link Widget} instances contained by this {@code WidgetGroup}.
   *
   * @return Number of child {@code Widget} instances contained by this {@code WidgetGroup}
   */
  @Override
  @CallSuper
  public int getNumWidgets() {
    return children.size();
  }

  /**
   * Immutable view of all children {@link Widget} instances belonging to this {@code WidgetGroup}
   * container.
   *
   * @return Immutable view of all children {@link Widget} instances
   */
  @NonNull
  @CallSuper
  @Override
  public Collection<Widget> getChildren() {
    return Collections.unmodifiableCollection(children);
  }

  @Override
  public void dispose() {
    super.dispose();
    for (Widget child : this) {
      child.dispose();
    }
  }

  @Override
  public final void requestLayout() {
    System.out.println("WidgetGroup#requestLayout();");
    layout();
  }

  /**
   * Lays out this {@code WidgetGroup} and sets the sizing.
   */
  private void layout() {
    double layout_width, layout_height;
    for (Widget child : this) {
      layout_width = LengthUnit.parse(child.get(LayoutParams.layout_width).toString());
      layout_height = LengthUnit.parse(child.get(LayoutParams.layout_height).toString());
      if (layout_width == LengthUnit.MATCH_PARENT) {
        child.setLeft(getPaddingLeft());
        child.setRight(getWidth() - getPaddingRight());
      } else if (layout_width > 0) {
        //System.out.println("layout_width = " + LengthUnit.toPixels(layout_width));
        child.setRight(child.getLeft() + (int) layout_width);
      }

      if (layout_height == LengthUnit.MATCH_PARENT) {
        child.setBottom(getPaddingBottom());
        child.setTop(getHeight() - getPaddingTop());
      } else if (layout_height > 0) {
        //System.out.println("layout_height = " + LengthUnit.toPixels(layout_height));
        child.setBottom(child.getTop() - (int) layout_height);
      }
    }

    layoutChildren();

    for (Widget child : this) {
      layout_width = LengthUnit.parse(child.get(LayoutParams.layout_width).toString());
      layout_height = LengthUnit.parse(child.get(LayoutParams.layout_height).toString());
      if (layout_width == LengthUnit.WRAP_CONTENT) {
        child.setRight(child.getLeft() + Math.max(child.getPreferredWidth(), child.getMinWidth())
                + child.getPaddingLeft() + child.getPaddingRight());
      }

      if (layout_height == LengthUnit.WRAP_CONTENT) {
        child.setBottom(child.getTop() - Math.max(child.getPreferredHeight(), child.getMinHeight())
                - child.getPaddingTop() - child.getPaddingBottom());
      }
    }

    invalidate();
  }

  /**
   * Lays out the children of this {@code WidgetGroup}. This method is intended to be overridden by
   * subclasses who have specific specifications on how their children should be laid out.
   */
  @CallSuper
  public void layoutChildren() {
    for (Widget child : this) {
      if (child instanceof WidgetGroup) {
        ((WidgetGroup) child).layout();
      }
    }
  }

}
