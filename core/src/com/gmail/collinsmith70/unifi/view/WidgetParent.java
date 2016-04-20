package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.gmail.collinsmith70.unifi.math.Point;
import com.gmail.collinsmith70.unifi.math.Rectangle;

/**
 * Defines the responsibilities for a class that will be a parent of a {@link Widget}. This is the
 * API that a {@code Widget} sees when it wants to interact with its parent.
 */
public interface WidgetParent {

  /**
   * Called when the layout of a child of this {@code WidgetParent} has been invalidated due to some
   * change. This will schedule a pass of the widget tree.
   */
  void requestLayout();

  /**
   * Indicates whether a {@linkplain #requestLayout layout was requested} on this
   * {@code WidgetParent}.
   *
   * @return {@code true} if a layout was requested, otherwise {@code false}
   */
  boolean isLayoutRequested();

  /**
   * Invalidates a child of this {@code WidgetParent} and sets the specified region as dirty and
   * needing to be redrawn.
   *
   * @param child  Child which is dirty
   * @param region Area within the child that is invalid
   */
  void invalidateChild(@NonNull Widget child, @NonNull Rectangle region);

  /**
   * Invalidates a child of this {@code WidgetParent} and sets the specified region as dirty and
   * needing to be redrawn. The child is specified using its left and top positions with the passed
   * array at indeces {@code 0} and {@code 1} respectively.
   * <p>
   * This method must return the {@linkplain #getParent parent} of this {@code WidgetParent} if the
   * specified region must be invalidated in the parent. If the specified region does not require
   * invalidation in the parent, or if the parent does not exist, this method must return
   * {@code null}
   * </p><p>
   * When this method returns a non-null value, the location array must have been updated with the
   * left and top coordinates of this {@code WidgetParent}.
   * </p>
   *
   * @param position Array of size {@code 2} containing the left and top coordinates of the child
   *                 to invalidate.
   * @param region   Area within the child that is invalid.
   *
   * @return {@linkplain #getParent parent} of this {@code WidgetParent} or {@code null} if the
   *         specified region does not require invalidation in the parent, or if the parent does
   *         not exist, this method must return
   */
  @Nullable
  WidgetParent invalidateChildInParent(@NonNull @Size(value = 2) int[] position,
                                       @NonNull Rectangle region);

  /**
   * Parent of this {@code WidgetParent}.
   *
   * @return {@code WidgetParent} of this {@code WidgetParent}, or {@code null} if this
   *         {@code WidgetParent} does not have a parent
   */
  @Nullable
  WidgetParent getParent();

  /**
   * Indicates whether or not this {@code WidgetParent} has a parent.
   *
   * @return {@code true} if this {@code WidgetParent} has a parent, otherwise {@code false}
   */
  boolean hasParent();

  /**
   * Called when a child of this parent wants focus.
   *
   * @param child   Child of this {@code WidgetParent} that wants focus. This {@link Widget} will
   *                contain the focused {@code Widget}. It is not necessarily the {@code Widget}
   *                that actually has focus.
   * @param focused {@code Widget} that is a descendant of the child that actually has focus.
   */
  void requestChildFocus(@NonNull Widget child, @NonNull Widget focused);

  /**
   * Called when a child of this {@code WidgetParent} is giving up focus. {@link #get}
   *
   * @param child {@link Widget} that is giving up focus.
   */
  void clearChildFocus(@NonNull Widget child);

  /**
   * Computes the visible part of a rectangular region defined in terms of a child {@link Widget}
   * instance's coordinates.
   * <p>
   * Returns the clipped visible part of the rectangle {@code r}, defined in the {@code child}'s
   * local coordinate system. {@code r} is modified by this method to contain the result, expressed
   * in the global coordinate system.
   * </p><p>
   * The resulting rectangle is always axis aligned.
   * </p>
   *
   * @param child  Child {@code Widget} whose rectangular visible region to compute.
   * @param r      Input {@code Rectangle}, defined in {@code child}'s coordinate system. Will be
   *               overwritten to contain the resulting visible rectangle, expressed in global
   *               coordinates.
   * @param offset Input coordinates of a point, defined in {@code child}'s coordinate system. As
   *               with {@code r}, this will be overwritten to contain the global coordinates of
   *               that point. A {@code null} value indicates that this offset is not required.
   *
   * @return {@code true} if the resulting {@code Rectangle} is not empty, otherwise {@code false}
   */
  boolean getChildVisibleRect(@NonNull Widget child,
                              @NonNull Rectangle r,
                              @Nullable Point offset);

  /**
   * Finds the nearest {@link Widget} in the specified direction that wants to take focus.
   *
   * @param focused   {@code Widget} that currently has focus.
   * @param direction {@link Widget.SimpleFocusDirection} to search.
   *
   * @return {@code Widget} that wants to take focus
   */
  @Nullable
  Widget focusSearch(@Nullable Widget focused, @Widget.SimpleFocusDirection int direction);

  /**
   * Changes the z-order of the specified child so that it's on top of all other children. This
   * change in ordering may affect the layout in order-dependent layout schemes.
   *
   * @param child Child to bring to the top of the z-order
   */
  void bringChildToFront(@NonNull Widget child);

  /**
   * Notifies this {@code WidgetParent} that the specified child has become available. This method
   * is intended primarily to be used to handle state transitions in this {@code WidgetParent} from
   * the case where there are no focusable {@link Widget} instances to the case where the first
   * focusable {@code Widget} becomes available.
   *
   * @param child {@code Widget} who has become newly focusable.
   */
  void focusableViewAvailable(@NonNull Widget child);

}
