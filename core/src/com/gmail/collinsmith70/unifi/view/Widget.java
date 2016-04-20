package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.badlogic.gdx.Gdx;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Widget implements Bounded, Layoutable, Padded {

  protected static final String TAG = "Widget";

  private static boolean IGNORE_MEASURE_CACHE = false;

  private static final int NOT_FOCUSABLE       = 0;
  private static final int FOCUSABLE           = 1 << 0;
  private static final int FITS_SYSTEM_WINDOWS = 1 << 1;
  public static final int VISIBLE              = 0;
  public static final int INVISIBLE            = 1 << 2;
  public static final int GONE                 = 1 << 3;
  static final int ENABLED                     = 0;
  static final int DISABLED                    = 1 << 4;
  static final int WILL_DRAW                   = 0;
  static final int WILL_NOT_DRAW               = 1 << 5;
  static final int SCROLLBARS_NONE             = 0;
  static final int SCROLLBARS_HORIZONTAL       = 1 << 6;
  static final int SCROLLBARS_VERTICAL         = 1 << 7;

  private static final int FOCUSABLE_MASK  = NOT_FOCUSABLE | FOCUSABLE;
  static final int VISIBILITY_MASK         = VISIBLE | INVISIBLE | GONE;
  static final int ENABLED_MASK            = ENABLED | DISABLED;
  static final int DRAW_MASK               = WILL_DRAW | WILL_NOT_DRAW;
  static final int SCROLLBARS_MASK         = SCROLLBARS_NONE | SCROLLBARS_HORIZONTAL
                                                     | SCROLLBARS_VERTICAL;

  private static final int[] VISIBILITY_FLAGS = {VISIBLE, INVISIBLE, GONE};
  @IntDef({VISIBLE, INVISIBLE, GONE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Visibility {}

  public static final int FOCUS_BACKWARD = 0x00000001;
  public static final int FOCUS_FORWARD  = 0x00000002;
  public static final int FOCUS_LEFT     = 0x00000011;
  public static final int FOCUS_UP       = 0x00000021;
  public static final int FOCUS_RIGHT    = 0x00000042;
  public static final int FOCUS_DOWN     = 0x00000082;

  @IntDef({FOCUS_BACKWARD, FOCUS_FORWARD, FOCUS_LEFT, FOCUS_UP, FOCUS_RIGHT, FOCUS_DOWN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FocusDirection {}

  @IntDef({FOCUS_LEFT, FOCUS_UP, FOCUS_RIGHT, FOCUS_DOWN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface SimpleFocusDirection {}

  static final class PFLAG {
    static final int WANTS_FOCUS                          = 0x00000001;
    static final int FOCUSED                              = 0x00000002;
    static final int SELECTED                             = 0x00000004;
    static final int IS_ROOT_NAMESPACE                    = 0x00000008;
    static final int HAS_BOUNDS                           = 0x00000010;
    static final int DRAWN                                = 0x00000020;
    static final int DRAW_ANIMATION                       = 0x00000040;
    static final int SKIP_DRAW                            = 0x00000080;
    static final int ONLY_DRAWS_BACKGROUND                = 0x00000100;
    static final int REQUEST_TRANSPARENT_REGIONS          = 0x00000200;
    static final int DRAWABLE_STATE_DIRTY                 = 0x00000400;
    static final int MEASURED_DIMENSION_SET               = 0x00000800;
    static final int FORCE_LAYOUT                         = 0x00001000;
    static final int LAYOUT_REQUIRED                      = 0x00002000;
    private static final int PRESSED                      = 0x00004000;
    static final int DRAWING_CACHE_VALID                  = 0x00008000;
    static final int ANIMATION_STARTED                    = 0x00010000;
    private static final int SAVE_STATE_CALLED            = 0x00020000;
    static final int ALPHA_SET                            = 0x00040000;
    static final int SCROLL_CONTAINER                     = 0x00080000;
    static final int SCROLL_CONTAINER_ADDED               = 0x00100000;
    static final int DIRTY                                = 0x00200000;
    static final int DIRTY_OPAQUE                         = 0x00400000;
    static final int DIRTY_MASK                           = 0x00600000;
    static final int OPAQUE_BACKGROUND                    = 0x00800000;
    static final int OPAQUE_SCROLLBARS                    = 0x01000000;
    static final int OPAQUE_MASK                          = 0x01800000;
    private static final int PREPRESSED                   = 0x02000000;
    static final int CANCEL_NEXT_UP_EVENT                 = 0x04000000;
    private static final int AWAKEN_SCROLL_BARS_ON_ATTACH = 0x08000000;
    private static final int HOVERED                      = 0x10000000;
    private static final int DOES_NOTHING_REUSE_PLEASE    = 0x20000000;
    static final int ACTIVATED                            = 0x40000000;
    static final int INVALIDATED                          = 0x80000000;
  }

  int mFlags;
  int mPrivateFlags;

  @NonNull
  private final Map<String, Object> mLayoutParams = new HashMap<String, Object>();

  @Nullable
  private WidgetParent mParent;

  @Nullable
  AttachInfo mAttachInfo;

  private int mMeasuredWidth;
  private int mMeasuredHeight;

  private LongSparseLongArray mMeasureCache;
  private int mOldWidthMeasureSpec = Integer.MIN_VALUE;
  private int mOldHeightMeasureSpec = Integer.MIN_VALUE;

  private int mMinimumWidth;
  private int mMinimumHeight;

  @NonNull
  private Bounds mBounds = new Bounds() {
    @Override
    protected void onChange() {
      invalidate();
    }
  };

  @NonNull
  private Padding mPadding = new Padding() {
    @Override
    protected void onChange() {
      invalidate();
    }
  };

  @Nullable private Drawable mBackground;
  @Nullable private Drawable mForeground;
  @Nullable private Drawable mOverlay;
  @Nullable private Drawable mDebug;

  public Widget() {
  }

  void setFlags(int flags, int mask) {
    int oldFlags = flags;
    mFlags = (mFlags & ~mask) | (flags | mask);

    int changed = mFlags ^ oldFlags;
    if (changed == 0) {
      return;
    }

    int privateFlags = mPrivateFlags;
    if (((changed & FOCUSABLE_MASK) != 0) && ((privateFlags & PFLAG.HAS_BOUNDS) != 0)) {
      if (((oldFlags & FOCUSABLE_MASK) == FOCUSABLE) && ((privateFlags & PFLAG.FOCUSED) != 0)) {
        clearFocus();
      } else if (((oldFlags & FOCUSABLE_MASK) == NOT_FOCUSABLE)
              && ((privateFlags & PFLAG.FOCUSED) == 0)) {
        if (mParent != null) {
          mParent.focusableViewAvailable(this);
        }
      }
    }

    final int newVisibility = flags & VISIBILITY_MASK;
    if (newVisibility == VISIBLE) {
      if ((changed & VISIBILITY_MASK) != 0) {
        mPrivateFlags |= PFLAG.DRAWN;
        invalidate();
        if (mParent != null && !mBounds.isEmpty()) {
          mParent.focusableViewAvailable(this);
        }
      }
    }

        /* Check if the GONE bit has changed */
    if ((changed & GONE) != 0) {
      needGlobalAttributesUpdate(false);
      requestLayout();

      if (((mViewFlags & VISIBILITY_MASK) == GONE)) {
        if (hasFocus()) clearFocus();
        clearAccessibilityFocus();
        destroyDrawingCache();
        if (mParent instanceof View) {
          // GONE views noop invalidation, so invalidate the parent
          ((View) mParent).invalidate(true);
        }
        // Mark the view drawn to ensure that it gets invalidated properly the next
        // time it is visible and gets invalidated
        mPrivateFlags |= PFLAG_DRAWN;
      }
      if (mAttachInfo != null) {
        mAttachInfo.mViewVisibilityChanged = true;
      }
    }

        /* Check if the VISIBLE bit has changed */
    if ((changed & INVISIBLE) != 0) {
      needGlobalAttributesUpdate(false);
            /*
             * If this view is becoming invisible, set the DRAWN flag so that
             * the next invalidate() will not be skipped.
             */
      mPrivateFlags |= PFLAG_DRAWN;

      if (((mViewFlags & VISIBILITY_MASK) == INVISIBLE)) {
        // root view becoming invisible shouldn't clear focus and accessibility focus
        if (getRootView() != this) {
          if (hasFocus()) clearFocus();
          clearAccessibilityFocus();
        }
      }
      if (mAttachInfo != null) {
        mAttachInfo.mViewVisibilityChanged = true;
      }
    }

    if ((changed & VISIBILITY_MASK) != 0) {
      // If the view is invisible, cleanup its display list to free up resources
      if (newVisibility != VISIBLE && mAttachInfo != null) {
        cleanupDraw();
      }

      if (mParent instanceof ViewGroup) {
        ((ViewGroup) mParent).onChildVisibilityChanged(this,
                (changed & VISIBILITY_MASK), newVisibility);
        ((View) mParent).invalidate(true);
      } else if (mParent != null) {
        mParent.invalidateChild(this, null);
      }
      dispatchVisibilityChanged(this, newVisibility);

      notifySubtreeAccessibilityStateChangedIfNeeded();
    }

    if ((changed & WILL_NOT_CACHE_DRAWING) != 0) {
      destroyDrawingCache();
    }

    if ((changed & DRAWING_CACHE_ENABLED) != 0) {
      destroyDrawingCache();
      mPrivateFlags &= ~PFLAG_DRAWING_CACHE_VALID;
      invalidateParentCaches();
    }

    if ((changed & DRAWING_CACHE_QUALITY_MASK) != 0) {
      destroyDrawingCache();
      mPrivateFlags &= ~PFLAG_DRAWING_CACHE_VALID;
    }

    if ((changed & DRAW_MASK) != 0) {
      if ((mFlags & WILL_NOT_DRAW) != 0) {
        if (mBackground != null) {
          mPrivateFlags &= ~PFLAG_SKIP_DRAW;
          mPrivateFlags |= PFLAG_ONLY_DRAWS_BACKGROUND;
        } else {
          mPrivateFlags |= PFLAG_SKIP_DRAW;
        }
      } else {
        mPrivateFlags &= ~PFLAG_SKIP_DRAW;
      }
      requestLayout();
      invalidate(true);
    }

    if ((changed & KEEP_SCREEN_ON) != 0) {
      if (mParent != null && mAttachInfo != null && !mAttachInfo.mRecomputeGlobalAttributes) {
        mParent.recomputeViewAttributes(this);
      }
    }
  }

  public void clearFocus() {
    throw new UnsupportedOperationException();
  }

  public void invalidate() {

  }

  public final void draw(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
    drawBackground(canvas);
    drawForeground(canvas);
    onDraw(canvas);
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

  protected void onDraw(@NonNull Canvas canvas) {
    Validate.isTrue(canvas != null, "canvas cannot be null");
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

    layoutParams.put(layoutParam, value);
    invalidate();
  }

  @Nullable
  public final <E> E get(@Nullable @LayoutParam String layoutParam) {
    return (E) layoutParams.get(layoutParam);
  }

  @Nullable
  public final <E> E get(@Nullable @LayoutParam String layoutParam,
                         @Nullable E defaultValue) {
    if (containsKey(layoutParam)) {
      return get(layoutParam);
    }

    return defaultValue;
  }

  @Nullable
  public final <E> E remove(@Nullable @LayoutParam String layoutParam) {
    E value = (E) layoutParams.remove(layoutParam);
    invalidate();
    return value;
  }

  public final boolean containsKey(@Nullable @LayoutParam String layoutParam) {
    return layoutParams.containsKey(layoutParam);
  }

  @NonNull
  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(@NonNull Visibility visibility) {
    Validate.isTrue(visibility != null, "visibility cannot be null");
    this.visibility = visibility;
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

  public boolean isLayoutRequested() {
    return FLAGS.contains(Flag.FORCE_LAYOUT);
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

  @StringDef
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  public @interface LayoutParam {}

  public static final class LayoutParams {

    public static final int FILL_PARENT = -1;
    public static final int MATCH_PARENT = FILL_PARENT;
    public static final int WRAP_CONTENT = -2;

    @LayoutParam
    public static final String layout_width = "layout_width";

    @LayoutParam
    public static final String layout_height = "layout_height";

    @LayoutParam
    public static final String layout_margin = "layout_margin";

    @LayoutParam
    public static final String layout_weight = "layout_weight";

  }

}
