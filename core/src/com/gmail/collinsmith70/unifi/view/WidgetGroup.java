package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.Iterators;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public abstract class WidgetGroup extends Widget implements WidgetManager, WidgetParent {

  @NonNull
  private final Collection<Widget> children;

  public WidgetGroup() {
    this.children = new ArrayList<Widget>();
  }

  @Override
  public final Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(children.iterator());
  }

  @Override
  public final void addWidget(@NonNull Widget widget) {
    Validate.isTrue(widget != null, "widget cannot be null");
    children.add(widget);
    widget.setParent(this);
    widget.setAttachInfo(getAttachInfo());
    invalidate();
  }

  @Override
  public final void removeWidget(@Nullable Widget widget) {
    if (widget == null) {
      return;
    }

    _removeWidget(widget);
    invalidate();
  }

  private void _removeWidget(@NonNull Widget widget) {
    Validate.isTrue(widget != null, "widget cannot be null");
    children.remove(widget);
    widget.setParent(null);
    widget.setAttachInfo(null);
  }

  @Override
  public final void clear() {
    for (Widget child : this) {
      _removeWidget(child);
    }

    invalidate();
  }

  @NonNull
  @Override
  public final Collection<Widget> getChildren() {
    return Collections.unmodifiableCollection(children);
  }
}
