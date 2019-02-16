package gdx.diablo.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import gdx.diablo.BlendMode;
import gdx.diablo.Diablo;

public class PaletteIndexedColorDrawable extends TextureRegionDrawable {

  public Color tint;

  public PaletteIndexedColorDrawable(Color color) {
    super(Diablo.textures.white);
    this.tint = color;
  }

  @Override
  public void setLeftWidth(float leftWidth) {
    super.setLeftWidth(leftWidth);
    setMinWidth(getLeftWidth() + getRightWidth());
  }

  @Override
  public void setRightWidth(float rightWidth) {
    super.setRightWidth(rightWidth);
    setMinWidth(getLeftWidth() + getRightWidth());
  }

  @Override
  public void setTopHeight(float topHeight) {
    super.setTopHeight(topHeight);
    setMinHeight(getTopHeight() + getBottomHeight());
  }

  @Override
  public void setBottomHeight(float bottomHeight) {
    super.setBottomHeight(bottomHeight);
    setMinHeight(getTopHeight() + getBottomHeight());
  }

  @Override
  public void draw(Batch batch, float x, float y, float width, float height) {
    if (!(batch instanceof PaletteIndexedBatch)) {
      // unsupported, will draw white for now
      super.draw(batch, x, y, width, height);
      return;
    }

    PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
    b.setBlendMode(BlendMode.SOLID, tint);
    super.draw(batch, x, y, width, height);
    b.resetBlendMode();
  }

  @Override
  public void draw(Batch batch, float x, float y, float originX, float originY, float width,
                   float height, float scaleX, float scaleY, float rotation) {
    if (!(batch instanceof PaletteIndexedBatch)) {
      // unsupported, will draw white for now
      super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
      return;
    }

    PaletteIndexedBatch b = (PaletteIndexedBatch) batch;
    b.setBlendMode(BlendMode.SOLID, tint);
    super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    b.resetBlendMode();
  }
}
