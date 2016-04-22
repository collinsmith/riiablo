package com.gmail.collinsmith70.unifi3.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.gmail.collinsmith70.unifi3.math.Point;

/**
 * Defines the responsibilities for a class that will be a parent of a {@link Widget}. This is the
 * API that a {@code Widget} sees when it wants to interact with its parent.
 */
public interface WidgetParent extends WidgetManager {

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
  void invalidateChild(@NonNull Widget child, @NonNull com.gmail.collinsmith70.unifi3.math.Rectangle region);

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
                                       @NonNull com.gmail.collinsmith70.unifi3.math.Rectangle region);

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
                              @NonNull com.gmail.collinsmith70.unifi3.math.Rectangle r,
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

  /**
   * Attempts to display a context menu for the specified child or its ancestors.
   *
   * @param child Source {@link Widget} where the context menu request was first invoked.
   *
   * @return {@code true} if the request was successful and a context menu was displayed,
   *         otherwise {@code false}
   */
  boolean showContextMenuForChild(@NonNull Widget child);

  /**
   * Gives the {@code WidgetParent} the opportunity to populate the passed {@link ContextMenu},
   * which in turn propagates the request recursively to its own parent (if any).
   *
   * @param menu {@code ContextMenu} to populate.
   */
  void createContextMenu(@NonNull ContextMenu menu);

  /**
   * Starts an action mode for the specified {@link Widget}.
   *
   * @param source   Source {@code Widget} where the action mode was first invoked.
   * @param callback Callback that will handle lifecycle events for the action mode.
   *
   * @return New action mode if it was started, otherwise {@code null}
   */
  @Nullable
  ActionMode startActionModeForChild(@NonNull Widget source, @NonNull ActionMode.Callback callback);

  /**
   * Called when a child {@link Widget} instance's drawable state has changed.
   *
   * @param child Child whose drawable state has changed.
   */
  void childDrawableStateChanged(@NonNull Widget child);

  /**
   * Called when a child does not want this {@code WidgetParent} and its ancestors to intercept
   * touch events with {@link WidgetGroup#onInterceptTouchEvent}.
   *
   * @param disallowIntercept {@code true} if the child does not want the parent to intercept
   *                          touch events, otherwise {@code false}.
   */
  void requestDisallowInterceptTouchEvent(boolean disallowIntercept);

  /**
   * Called when a child of this {@code WidgetParent} wants a particular region to be positioned
   * onto the screen. Subclasses can trust that:
   * <ul>
   *   <li>the child will be a direct child of this {@code WidgetParent}, and</li>
   *   <li>the rectangle will be given in the child's coordinates</li>
   * </ul>
   * <p>
   * Implementations must:
   * <ul>
   *   <li>ensure nothing will change if the rectangle is already visible, and</li>
   *   <li>ensure the viewport will be scrolled only just enough to make the rectangle visible</li>
   * </ul>
   * </p>
   *
   * @param child     Direct child making the request.
   * @param rectangle Rectangle, given in {@code child}'s coordinates, that the child wishes to be
   *                  on screen.
   * @param immediate {@code true} to forbid animated or delayed scrolling, otherwise {@code false}.
   *
   * @return {@code true} if the {@code WidgetParent} scrolled to handle the request,
   *         otherwise {@code false}
   */
  boolean requestChildRectangleOnScreen(@NonNull Widget child,
                                        @NonNull com.gmail.collinsmith70.unifi3.math.Rectangle rectangle,
                                        boolean immediate);

  /**
   * Called when a child {@link Widget}, or a member of its subtree, now has or is no longer
   * tracking a transient state.
   * <p>
   * "Transient state" is any state that a {@code Widget} might hold that is not expected to be
   * reflected in the data model that the {@code Widget} currently presents. This state only affects
   * the presentation to the user within the {@code Widget} itself, such as the current state of
   * animations in progress or the state of a text selection operation.
   * </p><p>
   * Transient state is useful for hinting to other components of the {@code Widget} system that a
   * particular {@code Widget} is tracking something complex but encapsulated. A {@link ListWidget},
   * for example, may acknowledge that list item {@code Widget}s with transient states should be
   * preserved within their position or stable item ID instead of treating that {@code Widget} as
   * trivially replaceable by the backing adapter. This allows adapter implementations to be simpler
   * instead of needing to track the state of item {@code Widget} animations in progress such that
   * they could be restored in the event of an unexpected recycling and rebinding of attached
   * item {@code Widget}s.
   * </p>
   *
   * @param child             Child {@code Widget} whose state has changed.
   * @param hasTransientState {@code true} if this child has transient state,
   *                          otherwise {@code false}.
   */
  void childHasTransientStateChanged(@NonNull Widget child,
                                     boolean hasTransientState);

  /**
   * Indicates whether or not this {@code WidgetParent} can resolve the layout direction.
   * See {@link Widget#setLayoutDirection}
   *
   * @return {@code true} if this {@code WidgetParent} can resolve the layout direction,
   *         otherwise {@code false}
   */
  boolean canResolveLayoutDirection();

  /**
   * Indicates whether or not this {@code WidgetParent} instance's layout direction is resolved.
   * See {@link Widget#setLayoutDirection}
   *
   * @return {@code true} if this {@code WidgetParent} instance's layout direction is resolved,
   *         otherwise {@code false}
   */
  boolean isLayoutDirectionResolved();

  /**
   * {@link Widget.LayoutDirection} of this {@code WidgetParent}.
   * See {@link Widget#getLayoutDirection}
   *
   * @return {@link Widget.LayoutDirection} constant representing the layout direction of this
   *         {@code WidgetParent}
   */
  @Widget.LayoutDirection
  int getLayoutDirection();

  /**
   * Indicates whether or not this {@code WidgetParent} can resolve the text alignment.
   * See {@link Widget#setTextAlignment}
   *
   * @return {@code true} if this {@code WidgetParent} can resolve the text alignment,
   *         otherwise {@code false}
   */
  boolean canResolveTextAlignment();

  /**
   * Indicates whether or not this {@code WidgetParent} instance's text alignment is resolved.
   * See {@link Widget#setTextAlignment}
   *
   * @return {@code true} if this {@code WidgetParent} instance's text alignment is resolved,
   *         otherwise {@code false}
   */
  boolean isTextAlignmentResolved();

  /**
   * {@link Widget.TextAlignment} of this {@code WidgetParent}.
   * See {@link Widget#getTextAlignent}
   *
   * @return {@link Widget.TextAlignment} constant representing the text alignment of this
   *         {@code WidgetParent}
   */
  @Widget.TextAlignment
  int getTextAlignment();

  boolean onStartNestedScroll(@NonNull Widget child,
                              @NonNull Widget target,
                              @Widget.ScrollAxes int scrollAxes);

  void onNestedScrollAccepted(@NonNull Widget child,
                              @NonNull Widget target,
                              @Widget.ScrollAxes int scrollAxes);

  void onStopNestedScroll(@NonNull Widget target);

  void onNestedScroll(@NonNull Widget target,
                      int dxConsumed, int dyConsumed,
                      int dxUnconsumed, int dyUnconsumed);

  void onNestedPreScroll(@NonNull Widget target,
                         int dx, int dy,
                         @NonNull @Size(value = 2) int[] consumed);

  boolean onNestedFling(@NonNull Widget target,
                        float velocityX, float velocityY,
                        boolean consumed);

  boolean onNestedPreFling(@NonNull Widget target,
                           float velocityX, float velocityY);














































}
