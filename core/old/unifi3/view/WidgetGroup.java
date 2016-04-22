package com.gmail.collinsmith70.unifi3.view;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.Iterators;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class WidgetGroup extends Widget implements WidgetManager, com.gmail.collinsmith70.unifi3.view.WidgetParent {

  @NonNull
  private final List<Widget> children;

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

  @Override
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getChildrenCount() {
    return children.size();
  }

  @NonNull
  @Override
  public Widget getChildAt(int id) {
    return children.get(id);
  }

  protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
    for (Widget child : this) {
      if (child.getVisibility() != Visibility.GONE) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
      }
    }
  }

  protected void measureChild(@NonNull Widget child,
                              int parentWidthMeasureSpec,
                              int parentHeightMeasureSpec) {
    final int layout_width = child.get(LayoutParams.layout_width, LayoutParams.WRAP_CONTENT);
    final int layout_height = child.get(LayoutParams.layout_height, LayoutParams.WRAP_CONTENT);
    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            getPadding().getLeft() + getPadding().getRight(), layout_width);
    final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            getPadding().getTop() + getPadding().getBottom(), layout_height);
    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  protected void measureChildWithMargins(@NonNull Widget child,
                                         int parentWidthMeasureSpec, int widthUsed,
                                         int parentHeightMeasureSpec, int heightUsed) {
    final int layout_width = child.get(LayoutParams.layout_width, LayoutParams.WRAP_CONTENT);
    final int layout_height = child.get(LayoutParams.layout_height, LayoutParams.WRAP_CONTENT);
    final com.gmail.collinsmith70.unifi3.util.Margin margin = child.get(LayoutParams.layout_margin, com.gmail.collinsmith70.unifi3.util.Margin.EMPTY_MARGINS);
    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            getPadding().getLeft() + getPadding().getRight()
                    + margin.getLeft() + margin.getRight() + widthUsed,
            layout_width);
    final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            getPadding().getTop() + getPadding().getBottom()
                    + margin.getTop() + margin.getBottom() + heightUsed,
            layout_height);
    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  public static int getChildMeasureSpec(int measureSpec, int padding, int childDimension) {
    MeasureSpec.Mode specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    int size = Math.max(0, specSize - padding);

    int resultSize = 0;
    MeasureSpec.Mode resultMode = MeasureSpec.Mode.UNSPECIFIED;

    switch (specMode) {
      case EXACTLY:
        if (childDimension >= 0) {
          resultSize = childDimension;
          resultMode = MeasureSpec.Mode.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
          resultSize = size;
          resultMode = MeasureSpec.Mode.EXACTLY;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
          resultSize = size;
          resultMode = MeasureSpec.Mode.AT_MOST;
        }

        break;
      case AT_MOST:
        if (childDimension >= 0) {
          resultSize = childDimension;
          resultMode = MeasureSpec.Mode.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
          resultSize = size;
          resultMode = MeasureSpec.Mode.AT_MOST;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
          resultSize = size;
          resultMode = MeasureSpec.Mode.AT_MOST;
        }

        break;
      case UNSPECIFIED:
      default:
        if (childDimension >= 0) {
          resultSize = childDimension;
          resultMode = MeasureSpec.Mode.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
          resultSize = 0;
          resultMode = MeasureSpec.Mode.UNSPECIFIED;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
          resultSize = 0;
          resultMode = MeasureSpec.Mode.UNSPECIFIED;
        }

        break;
    }

    return MeasureSpec.compile(resultSize, resultMode);
  }

  protected void onSetLayoutParams(@NonNull Widget child,
                                   @NonNull @LayoutParam String param,
                                   @Nullable Object from,
                                   @Nullable Object to) {}

  public static final class LayoutParams {
    private LayoutParams() {}

    public static final int WRAP_CONTENT = -2;
    public static final int MATCH_PARENT = -1;
    public static final int FILL_PARENT = MATCH_PARENT;

    @LayoutParam
    public static final String layout_width = "layout_width";

    @LayoutParam
    public static final String layout_height = "layout_height";

    @LayoutParam
    public static final String layout_margin = "layout_margin";

    @LayoutParam
    public static final String layout_weight = "layout_weight";

    @LayoutParam
    public static final String layout_direction = "layout_direction";

  }

}
