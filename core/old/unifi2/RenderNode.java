package com.gmail.collinsmith70.unifi2;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi2.math.ImmutablePoint2D;
import com.gmail.collinsmith70.unifi2.math.Point2D;

@Deprecated
public class RenderNode implements Disposable {

  @NonNull
  private final String name;

  @NonNull
  private final Widget owningWidget;

  @NonNull
  private final Pixmap data;

  private boolean isValid;

  @NonNull
  private Point2D translation;

  public RenderNode(@NonNull String name, @NonNull Widget owningWidget) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    } else if (owningWidget == null) {
      throw new IllegalArgumentException("owningWidget cannot be null");
    }

    this.name = name;
    this.owningWidget = owningWidget;
    this.data = new Pixmap(owningWidget.getWidth(), owningWidget.getHeight(),
            Pixmap.Format.RGBA8888);
  }

  @NonNull
  Pixmap getData() {
    return data;
  }

  public boolean isValid() {
    return isValid;
  }

  public int getTranslationX() {
    return translation.getX();
  }

  public int getTranslationY() {
    return translation.getY();
  }

  public void setTranslationX(int x) {
    translation.setX(x);
  }

  public void setTranslationY(int y) {
    translation.setY(y);
  }

  @NonNull
  public ImmutablePoint2D getTranslation() {
    return new ImmutablePoint2D(translation);
  }

  public void setTranslation(int x, int y) {
    translation.set(x, y);
  }

  @Override
  public void dispose() {
    data.dispose();
  }
}
