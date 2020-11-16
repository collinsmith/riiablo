package com.riiablo.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.riiablo.codec.DC;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;

public class DCWrapper extends Image {
  private final TextureRegionDrawable drawable;
  private int blendMode;

  public DCWrapper() {
    super();
    setDrawable(drawable = new TextureRegionDrawable());
    blendMode = BlendMode.ID;
  }

  public void setBlendMode(int blendMode) {
    if (this.blendMode != blendMode) {
      this.blendMode = blendMode;
    }
  }

  public void setDrawable(DC drawable) {
    setDrawable(drawable.getTexture());
  }

  public void setDrawable(TextureRegion drawable) {
    this.drawable.setRegion(drawable);
    layout();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    draw((PaletteIndexedBatch) batch, parentAlpha);
  }

  public void draw(PaletteIndexedBatch batch, float parentAlpha) {
    batch.setBlendMode(blendMode, getColor());
    super.draw(batch, parentAlpha);
    batch.resetBlendMode();
  }
}
