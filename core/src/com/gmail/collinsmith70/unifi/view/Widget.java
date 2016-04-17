package com.gmail.collinsmith70.unifi.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.gmail.collinsmith70.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.graphics.Drawable;
import com.gmail.collinsmith70.unifi.util.Bounded;
import com.gmail.collinsmith70.unifi.util.Bounds;
import com.gmail.collinsmith70.unifi.util.Padded;
import com.gmail.collinsmith70.unifi.util.Padding;

import org.apache.commons.lang3.Validate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class Widget implements Bounded, Drawable, Padded {

  @StringDef
  @Documented
  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
  public @interface LayoutParam {}

  @NonNull
  private final Map<String, Object> LAYOUT_PARAMS;

  @Nullable
  private WidgetParent parent;

  @Nullable
  private AttachInfo attachInfo;

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

  @Nullable
  final AttachInfo getAttachInfo() {
    return attachInfo;
  }

  final void setAttachInfo(@Nullable AttachInfo attachInfo) {
    this.attachInfo = attachInfo;
  }

  @Nullable
  final WidgetParent getParent() {
    return parent;
  }

  final void setParent(@Nullable WidgetParent parent) {
    this.parent = parent;
  }

  final boolean hasParent() {
    return getParent() != null;
  }

  @Nullable
  public final Window getWindow() {
    if (attachInfo == null) {
      return null;
    }

    return attachInfo.window;
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

  final static class AttachInfo {

    @NonNull
    final Window window;

    AttachInfo(@NonNull Window window) {
      Validate.isTrue(window != null, "window cannot be null");
      this.window = window;
    }

  }

  public enum Visibility {
    VISIBLE,
    INVISIBLE,
    GONE
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
