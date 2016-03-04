package com.gmail.collinsmith70.unifi.unifi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class WidgetGroup extends Widget
        implements WidgetManager {

  @NonNull
  private final Collection<Widget> CHILDREN;

  public WidgetGroup() {
    this.CHILDREN = new ArrayList<Widget>();
  }

  @Override
  public void setTranslationX(int x) {
    super.setTranslationX(x);

    x = getX();
    for (Widget child : this) {
      child.setTranslationX(x);
    }
  }

  @Override
  public void setTranslationY(int y) {
    super.setTranslationY(y);

    y = getY();
    for (Widget child : this) {
      child.setTranslationY(y);
    }
  }

  @Override
  @NonNull
  public Iterator<Widget> iterator() {
    return Iterators.unmodifiableIterator(CHILDREN.iterator());
  }

  @NonNull
  @Override
  public WidgetManager addWidget(@NonNull Widget child) {
    return null;
  }

  @Override
  public boolean containsWidget(@Nullable Widget child) {
    return false;
  }

  @Override
  public boolean removeWidget(@Nullable Widget child) {
    return false;
  }

  @Override
  public int getNumWidgets() {
    return 0;
  }

  @NonNull
  @Override
  public Collection<Widget> getChildren() {
    return null;
  }

}
