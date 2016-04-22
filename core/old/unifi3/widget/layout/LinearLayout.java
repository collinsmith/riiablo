package com.gmail.collinsmith70.unifi3.widget.layout;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LinearLayout extends com.gmail.collinsmith70.unifi3.view.WidgetGroup {

  @IntDef(flag = true,
          value = {
                  DividerModes.DIVIDER_MODE_NONE,
                  DividerModes.DIVIDER_MODE_BEGINNING,
                  DividerModes.DIVIDER_MODE_MIDDLE,
                  DividerModes.DIVIDER_MODE_END
          })
  @Retention(RetentionPolicy.SOURCE)
  public @interface DividerMode {}

  public static final class DividerModes {
    public static final int DIVIDER_MODE_NONE      = 0;
    public static final int DIVIDER_MODE_BEGINNING = 1 << 0;
    public static final int DIVIDER_MODE_MIDDLE    = 1 << 1;
    public static final int DIVIDER_MODE_END       = 1 << 2;
    public static final int DIVIDER_MODE_ALL
            = DIVIDER_MODE_BEGINNING | DIVIDER_MODE_MIDDLE | DIVIDER_MODE_END;
  }

  private Orientation orientation;

  private int gravity;

  private int totalLength;

  private float weightSum;

  private boolean useLargestChild;

  @NonNull
  private com.gmail.collinsmith70.unifi3.graphics.Drawable divider;
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int dividerWidth;
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int dividerHeight;
  @DividerMode
  private int dividerMode;
  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int dividerPadding;

  public LinearLayout() {
    setOrientation(Orientation.HORIZONTAL);
    setGravity(com.gmail.collinsmith70.unifi3.view.Gravity.LEFT | com.gmail.collinsmith70.unifi3.view.Gravity.TOP);
    weightSum = -1.0f;
    useLargestChild = false;
    setDivider(null);
    setDividerMode(DividerModes.DIVIDER_MODE_NONE);
    setDividerPadding(0);
  }

  @DividerMode
  public int getDividerMode() {
    return dividerMode;
  }

  public void setDividerMode(@DividerMode int dividerMode) {
    dividerMode &= DividerModes.DIVIDER_MODE_ALL;
    if (getDividerMode() == dividerMode) {
      return;
    }

    this.dividerMode = dividerMode;
    requestLayout();
  }

  @NonNull
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(@NonNull Orientation orientation) {
    Validate.isTrue(orientation != null, "orientation cannot be null");
    this.orientation = orientation;
  }

  public void setGravity(int gravity) {
    this.gravity = gravity;
  }

  public com.gmail.collinsmith70.unifi3.graphics.Drawable getDivider() {
    return divider;
  }

  public void setDivider(@Nullable com.gmail.collinsmith70.unifi3.graphics.Drawable divider) {
    if (getDivider() == divider) {
      return;
    }

    this.divider = divider;
    if (divider != null) {
      this.dividerWidth = divider.getIntrinsicWidth();
      this.dividerHeight = divider.getIntrinsicHeight();
    } else {
      this.dividerWidth = 0;
      this.dividerHeight = 0;
    }

    requestLayout();
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getDividerPadding() {
    return dividerPadding;
  }

  public void setDividerPadding(@IntRange(from = 0, to = Integer.MAX_VALUE) int padding) {
    Validate.isTrue(padding >= 0, "padding should be greater than or equal to 0");
    this.dividerPadding = padding;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getDividerWidth() {
    return dividerWidth;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getDividerHeight() {
    return dividerHeight;
  }

  @Override
  protected void onDraw(@NonNull com.gmail.collinsmith70.unifi3.graphics.Canvas canvas) {
    super.onDraw(canvas);
    if (getDivider() == null) {
      return;
    }

    switch (getOrientation()) {
      case VERTICAL:
        drawDividersVertical(canvas);
        break;
      case HORIZONTAL:
      default:
        drawDividersHorizontal(canvas);
        break;
    }
  }

  void drawDividersVertical(@NonNull com.gmail.collinsmith70.unifi3.graphics.Canvas canvas) {
    final int count = getChildrenCount();
    for (int i = 0; i < count; i++) {
      final com.gmail.collinsmith70.unifi3.view.Widget child = getChildAt(i);
      if (child != null && child.getVisibility() != Visibility.GONE) {
        if (hasDividerBeforeChildAt(i)) {
          final com.gmail.collinsmith70.unifi3.util.Margin margin = child.get(LayoutParams.layout_margin, com.gmail.collinsmith70.unifi3.util.Margin.EMPTY_MARGINS);
          final int top = child.getBounds().getTop() - margin.getTop() - getDividerHeight();
          drawHorizontalDivider(canvas, top);
        }
      }
    }

    if (hasDividerBeforeChildAt(count)) {
      final com.gmail.collinsmith70.unifi3.view.Widget child = getChildAt(count - 1);
      int bottom = 0;
      if (child == null) {
        bottom = getBounds().getHeight() - getPadding().getBottom() - getDividerHeight();
      } else {
        final com.gmail.collinsmith70.unifi3.util.Margin margin = child.get(LayoutParams.layout_margin, com.gmail.collinsmith70.unifi3.util.Margin.EMPTY_MARGINS);
        bottom = child.getBounds().getBottom() + margin.getBottom();
      }

      drawHorizontalDivider(canvas, bottom);
    }
  }

  void drawHorizontalDivider(@NonNull com.gmail.collinsmith70.unifi3.graphics.Canvas canvas, int top) {
    divider.getBounds().set(getPadding().getLeft() + dividerPadding,
            top + dividerHeight,
            getBounds().getWidth() - getPadding().getRight() - dividerPadding,
            top);
    divider.draw(canvas);
  }

  void drawDividersHorizontal(@NonNull com.gmail.collinsmith70.unifi3.graphics.Canvas canvas) {
    final int count = getChildrenCount();
    for (int i = 0; i < count; i++) {
      final com.gmail.collinsmith70.unifi3.view.Widget child = getChildAt(i);
      if (child != null && child.getVisibility() != Visibility.GONE) {
        if (hasDividerBeforeChildAt(i)) {
          final com.gmail.collinsmith70.unifi3.util.Margin margin = child.get(LayoutParams.layout_margin, com.gmail.collinsmith70.unifi3.util.Margin.EMPTY_MARGINS);
          final int position = child.getBounds().getLeft() - margin.getLeft() - getDividerWidth();
          drawVerticalDivider(canvas, position);
        }
      }
    }

    if (hasDividerBeforeChildAt(count)) {
      final com.gmail.collinsmith70.unifi3.view.Widget child = getChildAt(count - 1);
      int position;
      if (child == null) {
        position = getBounds().getWidth() - getPadding().getRight() - getDividerWidth();
      } else {
        final com.gmail.collinsmith70.unifi3.util.Margin margin = child.get(LayoutParams.layout_margin, com.gmail.collinsmith70.unifi3.util.Margin.EMPTY_MARGINS);
        position = child.getBounds().getRight() + margin.getRight();
      }

      drawVerticalDivider(canvas, position);
    }
  }

  void drawVerticalDivider(@NonNull com.gmail.collinsmith70.unifi3.graphics.Canvas canvas, int left) {
    divider.getBounds().set(left,
            getBounds().getHeight() - getPadding().getBottom() - dividerPadding,
            left + dividerWidth,
            getPadding().getTop() + dividerPadding);
    divider.draw(canvas);
  }

  protected boolean hasDividerBeforeChildAt(int id) {
    if (id == 0) {
      return (getDividerMode() & DividerModes.DIVIDER_MODE_BEGINNING) != 0;
    } else if (id == getChildrenCount()) {
      return (getDividerMode() & DividerModes.DIVIDER_MODE_END) != 0;
    } else if ((getDividerMode() & DividerModes.DIVIDER_MODE_MIDDLE) != 0) {
      boolean hasVisibleWidgetBefore = false;
      for (int i = id - 1; i >= 0; i--) {
        if (getChildAt(i).getVisibility() != Visibility.GONE) {
          hasVisibleWidgetBefore = true;
          break;
        }
      }

      return hasVisibleWidgetBefore;
    }

    return false;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    switch (orientation) {
      case VERTICAL:
        measureVertical(widthMeasureSpec, heightMeasureSpec);
        break;
      case HORIZONTAL:
      default:
        measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        break;
    }
  }

  void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
    totalLength = 0;
    int maxWidth = 0;
    int childState = 0;
    int alternativeMaxWidth = 0;
    int weightedMaxWidth = 0;
    boolean allFillParent = true;
    float totalWeight = 0;

    final int count = getChildrenCount();

    final MeasureSpec.Mode widthMode = MeasureSpec.getMode(widthMeasureSpec);
    final MeasureSpec.Mode heightMode = MeasureSpec.getMode(heightMeasureSpec);

    boolean matchWidth = false;
    boolean skippedMeasure = false;

    int largestChildHeight = Integer.MIN_VALUE;

    // See how tall everyone is. Also remember max width.
    for (int i = 0; i < count; ++i) {
      final com.gmail.collinsmith70.unifi3.view.Widget child = getChildAt(i);

      if (child == null) {
        totalLength += measureNullChild(i);
        continue;
      }

      if (child.getVisibility() == com.gmail.collinsmith70.unifi3.view.Widget.Visibility.GONE) {
        i += getChildrenSkipCount(child, i);
        continue;
      }

      if (hasDividerBeforeChildAt(i)) {
        totalLength += dividerHeight;
      }

      int width = child.get(LayoutParams.layout_width, LayoutParams.WRAP_CONTENT);
      int height = child.get(LayoutParams.layout_height, LayoutParams.WRAP_CONTENT);
      com.gmail.collinsmith70.unifi3.util.Margin margin = child.get(LayoutParams.layout_margin, com.gmail.collinsmith70.unifi3.util.Margin.EMPTY_MARGINS);
      float weight = child.get(LayoutParams.layout_weight, 0.0f);

      totalWeight += weight;

      if (heightMode == MeasureSpec.Mode.EXACTLY && height == 0 && weight > 0) {
        // Optimization: don't bother measuring children who are going to use
        // leftover space. These views will get measured again down below if
        // there is any leftover space.
        final int totalLength = this.totalLength;
        this.totalLength
                = Math.max(totalLength, totalLength + margin.getTop() + margin.getBottom());
        skippedMeasure = true;
      } else {
        int oldHeight = Integer.MIN_VALUE;

        if (height == 0 && weight > 0) {
          // heightMode is either UNSPECIFIED or AT_MOST, and this
          // child wanted to stretch to fill available space.
          // Translate that to WRAP_CONTENT so that it does not end up
          // with a height of 0
          oldHeight = 0;
          height = LayoutParams.WRAP_CONTENT;
        }

        // Determine how big this child would like to be. If this or
        // previous children have given a weight, then we allow it to
        // use all available space (and we will shrink things later
        // if needed).
        measureChildBeforeLayout(
                child, i, widthMeasureSpec, 0, heightMeasureSpec,
                totalWeight == 0 ? this.totalLength : 0);

        if (oldHeight != Integer.MIN_VALUE) {
          height = oldHeight;
        }

        final int childHeight = child.getMeasuredHeight();
        final int totalLength = this.totalLength;
        this.totalLength = Math.max(totalLength, totalLength + childHeight + margin.getTop() +
                margin.getBottom() + getNextLocationOffset(child));

        if (useLargestChild) {
          largestChildHeight = Math.max(childHeight, largestChildHeight);
        }
      }

      boolean matchWidthLocally = false;
      if (widthMode != MeasureSpec.Mode.EXACTLY && width == LayoutParams.MATCH_PARENT) {
        // The width of the linear layout will scale, and at least one
        // child said it wanted to match our width. Set a flag
        // indicating that we need to remeasure at least that view when
        // we know our width.
        matchWidth = true;
        matchWidthLocally = true;
      }

      final int horizontalMargins = margin.getLeft() + margin.getRight();
      final int measuredWidth = child.getMeasuredWidth() + horizontalMargins;
      maxWidth = Math.max(maxWidth, measuredWidth);
      childState = childState | child.getMeasuredState();

      allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
      if (lp.weight > 0) {
                /*
                 * Widths of weighted Widgets are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
        weightedMaxWidth = Math.max(weightedMaxWidth,
                matchWidthLocally ? horizontalMargins : measuredWidth);
      } else {
        alternativeMaxWidth = Math.max(alternativeMaxWidth,
                matchWidthLocally ? horizontalMargins : measuredWidth);
      }

      i += getChildrenSkipCount(child, i);
    }

    if (mTotalLength > 0 && hasDividerBeforeChildAt(count)) {
      mTotalLength += mDividerHeight;
    }

    if (useLargestChild &&
            (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED)) {
      mTotalLength = 0;

      for (int i = 0; i < count; ++i) {
        final com.gmail.collinsmith70.unifi3.view.Widget child = getVirtualChildAt(i);

        if (child == null) {
          mTotalLength += measureNullChild(i);
          continue;
        }

        if (child.getVisibility() == GONE) {
          i += getChildrenSkipCount(child, i);
          continue;
        }

        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                child.getLayoutParams();
        // Account for negative margins
        final int totalLength = mTotalLength;
        mTotalLength = Math.max(totalLength, totalLength + largestChildHeight +
                lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
      }
    }

    // Add in our padding
    mTotalLength += mPaddingTop + mPaddingBottom;

    int heightSize = mTotalLength;

    // Check against our minimum height
    heightSize = Math.max(heightSize, getSuggestedMinimumHeight());

    // Reconcile our calculated size with the heightMeasureSpec
    int heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0);
    heightSize = heightSizeAndState & MEASURED_SIZE_MASK;

    // Either expand children with weight to take up available space or
    // shrink them if they extend beyond our current bounds. If we skipped
    // measurement on any children, we need to measure them now.
    int delta = heightSize - mTotalLength;
    if (skippedMeasure || delta != 0 && totalWeight > 0.0f) {
      float weightSum = mWeightSum > 0.0f ? mWeightSum : totalWeight;

      mTotalLength = 0;

      for (int i = 0; i < count; ++i) {
        final com.gmail.collinsmith70.unifi3.view.Widget child = getVirtualChildAt(i);

        if (child.getVisibility() == com.gmail.collinsmith70.unifi3.view.Widget.GONE) {
          continue;
        }

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();

        float childExtra = lp.weight;
        if (childExtra > 0) {
          // Child said it could absorb extra space -- give him his share
          int share = (int) (childExtra * delta / weightSum);
          weightSum -= childExtra;
          delta -= share;

          final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                  mPaddingLeft + mPaddingRight +
                          lp.leftMargin + lp.rightMargin, lp.width);

          // TODO: Use a field like lp.isMeasured to figure out if this
          // child has been previously measured
          if ((lp.height != 0) || (heightMode != MeasureSpec.EXACTLY)) {
            // child was measured once already above...
            // base new measurement on stored values
            int childHeight = child.getMeasuredHeight() + share;
            if (childHeight < 0) {
              childHeight = 0;
            }

            child.measure(childWidthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
          } else {
            // child was skipped in the loop above.
            // Measure for this first time here      
            child.measure(childWidthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(share > 0 ? share : 0,
                            MeasureSpec.EXACTLY));
          }

          // Child may now not fit in vertical dimension.
          childState = combineMeasuredStates(childState, child.getMeasuredState()
                  & (MEASURED_STATE_MASK>>MEASURED_HEIGHT_STATE_SHIFT));
        }

        final int horizontalMargins =  margin.getLeft() + margin.getRight();
        final int measuredWidth = child.getMeasuredWidth() + horizontalMargins;
        maxWidth = Math.max(maxWidth, measuredWidth);

        boolean matchWidthLocally = widthMode != MeasureSpec.EXACTLY &&
                lp.width == LayoutParams.MATCH_PARENT;

        alternativeMaxWidth = Math.max(alternativeMaxWidth,
                matchWidthLocally ? horizontalMargins : measuredWidth);

        allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;

        final int totalLength = mTotalLength;
        mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredHeight() +
                lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
      }

      // Add in our padding
      mTotalLength += mPaddingTop + mPaddingBottom;
      // TODO: Should we recompute the heightSpec based on the new total length?
    } else {
      alternativeMaxWidth = Math.max(alternativeMaxWidth,
              weightedMaxWidth);


      // We have no limit, so make all weighted views as tall as the largest child.
      // Children will have already been measured once.
      if (useLargestChild && heightMode != MeasureSpec.EXACTLY) {
        for (int i = 0; i < count; i++) {
          final com.gmail.collinsmith70.unifi3.view.Widget child = getVirtualChildAt(i);

          if (child == null || child.getVisibility() == com.gmail.collinsmith70.unifi3.view.Widget.GONE) {
            continue;
          }

          final LinearLayout.LayoutParams lp =
                  (LinearLayout.LayoutParams) child.getLayoutParams();

          float childExtra = lp.weight;
          if (childExtra > 0) {
            child.measure(
                    MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(largestChildHeight,
                            MeasureSpec.EXACTLY));
          }
        }
      }
    }

    if (!allFillParent && widthMode != MeasureSpec.EXACTLY) {
      maxWidth = alternativeMaxWidth;
    }

    maxWidth += mPaddingLeft + mPaddingRight;

    // Check against our minimum width
    maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

    setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            heightSizeAndState);

    if (matchWidth) {
      forceUniformWidth(count, heightMeasureSpec);
    }
  }

  int getChildrenSkipCount(com.gmail.collinsmith70.unifi3.view.Widget child, int id) {
    return 0;
  }

  int getLocationOffset(com.gmail.collinsmith70.unifi3.view.Widget child) {
    return 0;
  }

  int getNextLocationOffset(com.gmail.collinsmith70.unifi3.view.Widget child) {
    return 0;
  }

  int measureNullChild(int id) {
    return 0;
  }

  void measureChildBeforeLayout(com.gmail.collinsmith70.unifi3.view.Widget child, int id,
                                int widthMeasureSpec, int totalWidth,
                                int heightMeasureSpec, int totalHeight) {
    measureChildWithMargins(child,
            widthMeasureSpec, totalWidth,
            heightMeasureSpec, totalHeight);
  }

  private void forceUniformWidth(int count, int heightMeasureSpec) {
    // Pretend that the linear layout has an exact size.
    int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
            MeasureSpec.EXACTLY);
    for (int i = 0; i< count; ++i) {
      final com.gmail.collinsmith70.unifi3.view.Widget child = getVirtualChildAt(i);
      if (child.getVisibility() != GONE) {
        LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams)child.getLayoutParams());

        if (lp.width == LayoutParams.MATCH_PARENT) {
          // Temporarily force children to reuse their old measured height
          // FIXME: this may not be right for something like wrapping text?
          int oldHeight = lp.height;
          lp.height = child.getMeasuredHeight();

          // Remeasue with new dimensions
          measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
          lp.height = oldHeight;
        }
      }
    }
  }
  
  public enum Orientation {
    HORIZONTAL,
    VERTICAL
  }

}
