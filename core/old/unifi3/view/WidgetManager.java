package com.gmail.collinsmith70.unifi3.view;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * Defines the responsibilities for a class that will be a manager of {@link Widget} instances.
 */
public interface WidgetManager extends Iterable<Widget> {

  /**
   * Adds the specified {@link Widget} to this {@code WidgetManager}.
   *
   * @param widget {@code Widget} to add.
   */
  void addWidget(@NonNull Widget widget);

  /**
   * Removes the specified {@link Widget} from this {@code WidgetManager}.
   *
   * @param widget {@code Widget} to remove.
   */
  void removeWidget(@Nullable Widget widget);

  /**
   * {@link Collection} of all child {@link Widget} instances managed by this {@code WidgetManager}.
   *
   * @return {@code Collection} of all child {@link Widget} instances
   */
  @NonNull
  Collection<Widget> getChildren();

  /**
   * Number of children managed by this {@code WidgetManager}.
   *
   * @return Number of children managed by this {@code WidgetManager}
   */
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  int getChildrenCount();

  /**
   * {@link Widget} at the specified index (z-order) of this {@code WidgetManager}.
   *
   * @param index Index (z-order) of the {@code Widget} to retrieve.
   *
   * @return {@code Widget} at the specified index (z-order) of this {@code WidgetManager}
   *
   * @throws IllegalArgumentException if the passed index is less than 0 or greater than or equal
   *                                  to the {@linkplain #getChildrenCount number of children}.
   */
  @NonNull
  Widget getChildAt(int index);

  /**
   * Removes all managed {@link Widget} instances from this {@code WidgetManager}.
   */
  void clear();

}
