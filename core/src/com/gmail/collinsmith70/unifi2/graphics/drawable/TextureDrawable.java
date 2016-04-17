package com.gmail.collinsmith70.unifi2.graphics.drawable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi2.graphics.Canvas;

public class TextureDrawable implements Drawable, Disposable {

  @Nullable
  private Texture texture;

  @Nullable
  private TextureData textureData;

  public TextureDrawable() {

  }

  public TextureDrawable(@NonNull final Texture texture) {
    this.texture = texture;
    this.textureData = texture.getTextureData();
  }

  @Nullable
  public Texture getTexture() {
    return texture;
  }

  @Override
  public void draw(@NonNull final Canvas canvas) {
    if (!textureData.isPrepared()) {
      textureData.prepare();
    }

    Pixmap pixmap = textureData.consumePixmap();
    canvas.drawPixmap(0, 0, pixmap);
    if (textureData.disposePixmap()) {
      pixmap.dispose();
    }
  }

  @Override
  public void dispose() {
    texture.dispose();
    texture = null;
  }

}
