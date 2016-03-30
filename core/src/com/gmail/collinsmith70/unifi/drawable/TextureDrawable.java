package com.gmail.collinsmith70.unifi.drawable;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class TextureDrawable extends Drawable {

  @NonNull
  private Texture texture;

  public TextureDrawable(@NonNull Texture texture) {
    setTexture(texture);
    setWidth(getTexture().getWidth());
    setHeight(getTexture().getHeight());
  }

  private void setTexture(@NonNull Texture texture) {
    if (texture == null) {
      throw new IllegalArgumentException("texture cannot be null");
    }

    this.texture = texture;
  }

  @NonNull
  public Texture getTexture() {
    return texture;
  }

  @Override
  public void onDraw(Batch batch) {
    DrawableParent parent = getParent();
    if (parent != null) {
      batch.draw(getTexture(),
              getX() + parent.getX(), getY() + parent.getY(),
              getWidth(), getHeight());
    } else {
      batch.draw(getTexture(),
              getX(), getY(),
              getWidth(), getHeight());
    }
  }

}
