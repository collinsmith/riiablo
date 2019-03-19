package com.riiablo.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;

public class Button extends com.badlogic.gdx.scenes.scene2d.ui.Button implements Disposable {

  private static final AssetDescriptor<Sound> buttonDescriptor = new AssetDescriptor<>("data\\global\\sfx\\cursor\\button.wav", Sound.class);

  int disabledBlendMode = BlendMode.DARKEN;
  Color disabledColor = Riiablo.colors.darken;

  int highlightedBlendMode = BlendMode.BRIGHTEN;
  Color highlightedColor = Riiablo.colors.highlight;

  int blendMode = BlendMode.ID;
  Color color = Riiablo.colors.white;

  @Override
  public void setStyle(com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle style) {
    super.setStyle(style);
  }

  public Button(ButtonStyle style) {
    super(style);
    addListener(new ClickListener() {
      Sound button = null;

      {
        Riiablo.assets.load(buttonDescriptor);
      }

      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int b) {
        if (event.getButton() != Input.Buttons.LEFT || isDisabled()) {
          return super.touchDown(event, x, y, pointer, b);
        } else if (button == null) {
          Riiablo.assets.finishLoadingAsset(buttonDescriptor);
          button = Riiablo.assets.get(buttonDescriptor);
        }

        button.play();
        Riiablo.input.vibrate(10);
        return super.touchDown(event, x, y, pointer, b);
      }
    });
  }

  public void setBlendMode(int blendMode, Color color) {
    this.blendMode = blendMode;
    this.color = color;
  }

  public void setDisabledBlendMode(int blendMode, Color color) {
    disabledBlendMode = blendMode;
    disabledColor = color;
  }

  public void setHighlightedBlendMode(int blendMode, Color color) {
    highlightedBlendMode = blendMode;
    highlightedColor = color;
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(buttonDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    if (batch instanceof PaletteIndexedBatch) {
      draw((PaletteIndexedBatch) batch, parentAlpha);
    } else {
      super.draw(batch, parentAlpha);
    }
  }

  // FIXME: super.draw(Batch,float) sets color and applies to batch, circumventing my own color -- workaround is to set actor's color
  public void draw(PaletteIndexedBatch batch, float parentAlpha) {
    final boolean disabled = isDisabled();
    final boolean over = isOver();
    if (disabled) {
      setColor(disabledColor);
      batch.setBlendMode(disabledBlendMode);
    } else if (over) {
      setColor(highlightedColor);
      batch.setBlendMode(highlightedBlendMode);
    } else {
      setColor(color);
      batch.setBlendMode(blendMode);
    }
    super.draw(batch, parentAlpha);
    batch.resetBlendMode();
  }

  public static class ButtonStyle extends com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle {
    public ButtonStyle() {}
    public ButtonStyle(Drawable up, Drawable down) {
      this(up, down, null);
    }
    public ButtonStyle(Drawable up, Drawable down, Drawable checked) {
      super(up, down, checked);
    }
    public ButtonStyle(com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle style) {
      super(style);
    }
  }
}
