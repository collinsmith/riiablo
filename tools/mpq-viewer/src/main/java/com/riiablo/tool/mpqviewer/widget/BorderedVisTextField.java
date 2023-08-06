package com.riiablo.tool.mpqviewer.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextField;

public class BorderedVisTextField extends VisTextField {
  static final VisTextFieldStyle style;
  static {
    style = VisUI.getSkin().get("light", VisTextFieldStyle.class);
    final Drawable backgroundParent = style.background;
    style.background = new BaseDrawable(backgroundParent) {
      {
        setLeftWidth(4);
        setRightWidth(4);
      }

      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        backgroundParent.draw(batch, x, y, width, height);
      }
    };
    final Drawable backgroundOverParent = style.backgroundOver;
    style.backgroundOver = new BaseDrawable(backgroundOverParent) {
      {
        setLeftWidth(4);
        setRightWidth(4);
      }

      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        backgroundOverParent.draw(batch, x, y, width, height);
      }
    };
  }

  protected ClickListener clickListener;
  protected boolean hasFocus;

  public BorderedVisTextField() {
    super();
    setStyle(style);
  }

  @Override
  protected void initialize() {
    super.initialize();
    addListener(clickListener = new ClickListener());
  }

  @Override
  public void focusGained() {
    super.focusGained();
    hasFocus = true;
  }

  @Override
  public void focusLost() {
    super.focusLost();
    hasFocus = false;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    final VisTextFieldStyle style = getStyle();
    if (clickListener.isOver() || hasFocus) {
      style.focusBorder.draw(batch, getX(), getY(), getWidth(), getHeight());
    }
  }
}
