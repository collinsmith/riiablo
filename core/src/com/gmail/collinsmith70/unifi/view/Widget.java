package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;
import com.gmail.collinsmith70.unifi.util.Layoutable;
import com.gmail.collinsmith70.unifi.util.LongSparseLongArray;
import com.gmail.collinsmith70.unifi.util.Padded;
import com.gmail.collinsmith70.unifi.util.Padding;

import org.apache.commons.lang3.Validate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Widget implements Bounded, Drawable, Layoutable, Padded {

  private static final boolean IGNORE_MEASURE_CACHE = false;

  @StringDef
  @Documented
  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
  public @interface LayoutParam {}

  @NonNull
  private final Set<Flag> FLAGS;

  @NonNull
  private final Map<String, Object> LAYOUT_PARAMS;

  @Nullable
  private WidgetParent parent;

  @Nullable
  private AttachInfo attachInfo;

  private int measuredWidth;
  private int measuredHeight;

  private LongSparseLongArray measureCache;
  private int oldWidthMeasureSpec;
  private int oldHeightMeasureSpec;

  private int minimumWidth;
  private int minimumHeight;

  @NonNull
  private Bounds bounds;

  @NonNull
  private Padding padding;

  @NonNull
  private Visibility visibility;

  @Nullable
  private Drawable background;

  @Nullable
  private Drawable foreground;

  @Nullable
  private Drawable overlay;

  @Nullable
  private Drawable debug;

  public Widget() {
    this.FLAGS = EnumSet.noneOf(Flag.class);
    this.LAYOUT_PARAMS = new HashMap<String, Object>();

    this.bounds = new Bounds() {
      @Override
      protected void onChange() {
        invalidate();
      }
    };

    this.padding = new Padding() {
      @Override
      protected void onChange() {
        invalidate();
      }
    };

    _setVisibility(Visibility.VISIBLE);
  }

  public void invalidate() {

  }

  @Override
  public final void draw(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    drawBackground(canvas);
    drawForeground(canvas);
    drawOverlay(canvas);
    drawDebug(canvas);
  }

  protected void drawBackground(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable background = getBackground();
    if (background != null) {
      background.draw(canvas);
    }
  }

  protected void drawForeground(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable foreground = getForeground();
    if (foreground != null) {
      foreground.draw(canvas);
    }
  }

  protected void drawOverlay(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable overlay = getOverlay();
    if (overlay != null) {
      overlay.draw(canvas);
    }
  }

  protected void drawDebug(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    Drawable debug = getDebug();
    if (debug != null) {
      debug.draw(canvas);
    }
  }

  public final void put(@NonNull @LayoutParam String layoutParam,
                        @Nullable Object value) {
    Validate.isTrue(layoutParam != null, "layoutParam cannot be null");
    Validate.isTrue(!layoutParam.isEmpty(), "layoutParam cannot be empty");
    final Object curValue = get(layoutParam);
    if (curValue == null && curValue == value && containsKey(layoutParam)) {
      return;
    } else if (curValue != null && curValue == value) {
      return;
    }

    LAYOUT_PARAMS.put(layoutParam, value);
    invalidate();
  }

  @Nullable
  public final <E> E get(@Nullable @LayoutParam String layoutParam) {
    return (E)LAYOUT_PARAMS.get(layoutParam);
  }

  @Nullable
  public final <E> E getOrDefault(@Nullable @LayoutParam String layoutParam,
                                  @Nullable E defaultValue) {
    if (containsKey(layoutParam)) {
      return get(layoutParam);
    }

    return defaultValue;
  }

  @Nullable
  public final <E> E remove(@Nullable @LayoutParam String layoutParam) {
    E value = (E)LAYOUT_PARAMS.remove(layoutParam);
    invalidate();
    return value;
  }

  public final boolean containsKey(@Nullable @LayoutParam String layoutParam) {
    return LAYOUT_PARAMS.containsKey(layoutParam);
  }

  @NonNull
  public Visibility getVisibility() {
    return visibility;
  }

  private void _setVisibility(@NonNull Visibility visibility) {
    Validate.isTrue(visibility != null, "visibility cannot be null");
    this.visibility = visibility;
  }

  public void setVisibility(@NonNull Visibility visibility) {
    _setVisibility(visibility);
  }

  @Override
  public void requestLayout() {
    if (measureCache != null) {
      measureCache.clear();
    }

    if (attachInfo != null && attachInfo.widgetRequestingLayout == null) {
      Window window = getWindow();
      if (window != null && window.isLayingOut()) {
        if (!window.requestLayoutDuringLayout(this)) {
          return;
        }
      }

      attachInfo.widgetRequestingLayout = this;
    }

    if (hasParent() && parent.isLayoutRequested()) {
      parent.requestLayout();
    }

    if (attachInfo != null && attachInfo.widgetRequestingLayout == this) {
      attachInfo.widgetRequestingLayout = null;
    }
  }

  @Nullable
  final AttachInfo getAttachInfo() {
    return attachInfo;
  }

  final void setAttachInfo(@Nullable AttachInfo attachInfo) {
    this.attachInfo = attachInfo;
  }

  final boolean hasAttachInfo() {
    return getAttachInfo() != null;
  }

  @Nullable
  public final WidgetParent getParent() {
    return parent;
  }

  final void setParent(@Nullable WidgetParent parent) {
    if (hasParent()) {
      throw new RuntimeException("widget " + this + " already has a parent");
    }

    this.parent = parent;
  }

  public final boolean hasParent() {
    return getParent() != null;
  }

  @Nullable
  public final Window getWindow() {
    if (attachInfo == null) {
      return null;
    }

    return attachInfo.window;
  }

  @Nullable
  public final Widget getRootWidget() {
    if (attachInfo == null) {
      return null;
    }

    return attachInfo.rootWidget;
  }

  @NonNull
  @Override
  public final Bounds getBounds() {
    return bounds;
  }

  @NonNull
  @Override
  public final Bounds getBounds(@Nullable Bounds dst) {
    if (dst == null) {
      return new Bounds(bounds);
    }

    dst.set(bounds);
    return dst;
  }

  @Override
  public final void setBounds(@NonNull Bounds bounds) {
    Validate.isTrue(bounds != null, "bounds cannot be null");
    this.bounds = bounds;
  }

  @NonNull
  @Override
  public final Padding getPadding() {
    return padding;
  }

  @NonNull
  @Override
  public final Padding getPadding(@Nullable Padding dst) {
    if (dst == null) {
      return new Padding(padding);
    }

    dst.set(padding);
    return dst;
  }

  @Override
  public final void setPadding(@NonNull Padding padding) {
    Validate.isTrue(padding != null, "padding cannot be null");
    this.padding = padding;
  }

  @Nullable
  public Drawable getBackground() {
    return background;
  }

  public void setBackground(@Nullable Drawable background) {
    if (getBackground() != background) {
      this.background = background;
      invalidate();
    }
  }

  @Nullable
  public Drawable getForeground() {
    return foreground;
  }

  public void setForeground(@Nullable Drawable foreground) {
    if (getForeground() != foreground) {
      this.foreground = foreground;
      invalidate();
    }
  }

  @Nullable
  public Drawable getOverlay() {
    return overlay;
  }

  public void setOverlay(@Nullable Drawable overlay) {
    if (getOverlay() != overlay) {
      this.overlay = overlay;
      invalidate();
    }
  }

  @Nullable
  public Drawable getDebug() {
    return debug;
  }

  public void setDebug(@Nullable Drawable debug) {
    if (getDebug() != debug) {
      this.debug = debug;
      invalidate();
    }
  }

  public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
    long key = (long)widthMeasureSpec << Integer.SIZE | (long) heightMeasureSpec & 0xFFFFFFFFL;
    if (measureCache == null) {
      measureCache = new LongSparseLongArray(2);
    }

    final boolean forceLayout = FLAGS.contains(Flag.FORCE_LAYOUT);
    final boolean isExactly = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.Mode.EXACTLY
            && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.Mode.EXACTLY;
    final boolean matchingSize = isExactly
            && getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec)
            && getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec);
    if (forceLayout || !matchingSize
            && (widthMeasureSpec != oldWidthMeasureSpec
            || heightMeasureSpec != oldHeightMeasureSpec)) {
      FLAGS.remove(Flag.MEASURED_DIMENSION_SET);

      int cacheId = forceLayout ? -1 : measureCache.indexOfKey(key);
      if (cacheId < 0 || IGNORE_MEASURE_CACHE) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
        FLAGS.remove(Flag.MEASURE_NEEDED_BEFORE_LAYOUT);
      } else {
        long value = measureCache.valueAt(cacheId);
        setMeasuredWidth((int)(value >> 32));
        setMeasuredHeight((int)value);
        FLAGS.add(Flag.MEASURE_NEEDED_BEFORE_LAYOUT);
      }

      if (!FLAGS.contains(Flag.MEASURED_DIMENSION_SET)) {
        throw new IllegalStateException("onMeasure(int,int) did not set the measured dimension " +
                "by calling setMeasuredDimension(int,int)");
      }

      FLAGS.add(Flag.LAYOUT_REQUIRED);
    }

    oldWidthMeasureSpec = widthMeasureSpec;
    oldHeightMeasureSpec = heightMeasureSpec;
    measureCache.put(key,
            ((long) measuredWidth) << Integer.SIZE | (long) measuredHeight & 0xFFFFFFFFL);
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredWidth(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec));
    setMeasuredHeight(getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
  }

  public static int getDefaultSize(int size, int measureSpec) {
    MeasureSpec.Mode specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    switch (specMode) {
      case UNSPECIFIED:
        return size;
      case AT_MOST:
      case EXACTLY:
        return specSize;
      default:
        throw new IllegalStateException("measureSpec did not match any valid modes");
    }
  }

  protected int getSuggestedMinimumWidth() {
    return (background == null)
            ? getMinimumWidth()
            : Math.max(getMinimumWidth(), background.getMinimumWidth());
  }

  protected int getSuggestedMinimumHeight() {
    return (background == null)
            ? getMinimumHeight()
            : Math.max(getMinimumHeight(), background.getMinimumHeight());
  }

  public int getMinimumWidth() {
    return minimumWidth;
  }

  public void setMinimumWidth(int minimumWidth) {
    if (getMinimumWidth() == minimumWidth) {
      return;
    }

    this.minimumWidth = minimumWidth;
    requestLayout();
  }

  public int getMinimumHeight() {
    return minimumHeight;
  }

  public void setMinimumHeight(int minimumHeight) {
    if (getMinimumHeight() == minimumHeight) {
      return;
    }

    this.minimumHeight = minimumHeight;
    requestLayout();
  }

  public final void layout(int left, int top, int right, int bottom) {

  }

  final Set<Flag> getFlags() {
    return FLAGS;
  }

  public final int getMeasuredWidth() {
    return measuredWidth;
  }

  private void setMeasuredWidth(int measuredWidth) {
    this.measuredWidth = measuredWidth;
  }

  public final int getMeasuredHeight() {
    return measuredHeight;
  }

  private void setMeasuredHeight(int measuredHeight) {
    this.measuredHeight = measuredHeight;
  }

  final static class AttachInfo {

    @NonNull
    final Window window;

    @Nullable
    Widget widgetRequestingLayout;

    @Nullable
    Widget rootWidget;

    AttachInfo(@NonNull Window window) {
      Validate.isTrue(window != null, "window cannot be null");
      this.window = window;
    }

  }

  enum Flag {
    FORCE_LAYOUT,
    MEASURED_DIMENSION_SET,
    MEASURE_NEEDED_BEFORE_LAYOUT,
    LAYOUT_REQUIRED
  }

  public enum Visibility {
    VISIBLE,
    INVISIBLE,
    GONE
  }

  public static final class MeasureSpec {

    private static final int MODE_SHIFT = 30;

    private static final int UNSPECIFIED = 0;
    private static final int EXACTLY = 1 << MODE_SHIFT;
    private static final int AT_MOST = 2 << MODE_SHIFT;

    private static final int MODE_MASK = (UNSPECIFIED | EXACTLY | AT_MOST) << MODE_SHIFT;

    public enum Mode {
      UNSPECIFIED(MeasureSpec.UNSPECIFIED),
      EXACTLY(MeasureSpec.EXACTLY),
      AT_MOST(MeasureSpec.AT_MOST);

      private final int bit;

      Mode(int bit) {
        this.bit = bit << MODE_SHIFT;
      }

      public int getBit() {
        return bit;
      }

    }

    public static int compile(
            @IntRange(from = LayoutParams.WRAP_CONTENT, to = Integer.MAX_VALUE) int size,
            @NonNull MeasureSpec.Mode mode) {
      Validate.isTrue(size >= 0, "size must be greater than or equal to 0");
      Validate.isTrue(mode != null, "mode cannot be null");
      return (size & ~MODE_MASK) | (mode.bit & MODE_MASK);
    }

    @NonNull
    public static Mode getMode(int measureSpec) {
      switch (measureSpec & MODE_MASK) {
        case UNSPECIFIED:
          return Mode.UNSPECIFIED;
        case EXACTLY:
          return Mode.EXACTLY;
        case AT_MOST:
          return Mode.AT_MOST;
        default:
          throw new IllegalStateException("measureSpec did not match any valid modes");
      }
    }

    public static int getSize(int measureSpec) {
      return measureSpec & ~MODE_MASK;
    }

    public static String toString(int measureSpec) {
      Mode mode = getMode(measureSpec);
      int size = getSize(measureSpec);
      return "MeasureSpec: " + mode + " " + size;
    }

  }

  public static final class LayoutParams {

    public static final int FILL_PARENT = -1;
    public static final int MATCH_PARENT = FILL_PARENT;
    public static final int WRAP_CONTENT = -2;

    @LayoutParam
    public static final String layout_width = "layout_width";

    @LayoutParam
    public static final String layout_height = "layout_height";

  }

}
