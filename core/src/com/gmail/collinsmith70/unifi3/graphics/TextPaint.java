package com.gmail.collinsmith70.unifi3.graphics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class TextPaint extends Paint {

  @Nullable
  private BitmapFont font;

  public TextPaint() {
    _setFont(null);
  }

  private void _setFont(@Nullable BitmapFont font) {
    this.font = font;
  }

  @Nullable
  public final BitmapFont getFont() {
    return font;
  }

  @NonNull
  public TextPaint setFont(@Nullable BitmapFont font) {
    _setFont(font);
    return this;
  }

}
