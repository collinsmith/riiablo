package com.gmail.collinsmith70.unifi.unifi.graphics.drawable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.unifi.graphics.Canvas;

public class TextureDrawable implements Drawable, Disposable {

  @Nullable
  private Texture texture;

  @Nullable Pixmap pixmap;

  public TextureDrawable() {

  }

  public TextureDrawable(@NonNull final Texture texture) {
    this.texture = texture;
    TextureData textureData = texture.getTextureData();
    if (textureData.isPrepared()) {
      this.pixmap = textureData.consumePixmap();
    } else {
      throw new IllegalArgumentException("TextureDrawable does not support texture " + texture);
    }
  }

  @Override
  public void draw(@NonNull final Canvas canvas) {
    canvas.drawPixmap(0, 0, pixmap);
  }

  @Override
  public void dispose() {

  }

}
