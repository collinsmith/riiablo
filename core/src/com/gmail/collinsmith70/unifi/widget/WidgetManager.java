package com.gmail.collinsmith70.unifi.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * A {@code WidgetManager} is an interface for managing {@link Widget} instances, namely
 * {@linkplain #addWidget(Widget) adding} and {@linkplain #removeWidget(Widget) removing}
 * them.
 */
public interface WidgetManager extends Iterable<Widget> {

  /**
   * @param child {@link Widget} to add to this {@link WidgetManager}
   * @return reference to this {@link WidgetManager} to allow for method chaining
   */
  @NonNull
  WidgetManager addWidget(@NonNull Widget child);

  /**
   * @param child {@link Widget} to remove from this {@link WidgetManager}
   * @return {@code true} if the specified {@link Widget} is managed by this {@link WidgetManager},
   * otherwise {@code false} if it was not or if the passed reference was {@code null}
   */
  boolean containsWidget(@Nullable Widget child);

  /**
   * @param child {@link Widget} to remove from this {@link WidgetManager}
   * @return {@code true} if the specified {@link Widget} was removed, otherwise {@code false} if it
   * was not or if the passed reference was {@code null}
   */
  boolean removeWidget(@Nullable Widget child);

  /**
   * @return number of {@link Widget} instances managed by this {@link WidgetManager}
   */
  int getNumWidgets();

  /**
   * @return {@link Collection} of all children of this {@link WidgetManager}
   */
  @NonNull
  Collection<Widget> getChildren();

}
