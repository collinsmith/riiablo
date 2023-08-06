package com.riiablo.tool.mpqviewer.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.VisImageButton;

public class BorderedVisImageButton extends VisImageButton {
  protected ClickListener clickListener;
  protected boolean hasFocus;
  protected Drawable focusBorder;

  public BorderedVisImageButton(VisImageButtonStyle style, Drawable focusBorder, String tooltip) {
    super(style.imageUp, tooltip);
    this.focusBorder = focusBorder;
    setStyle(style);
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
    if (clickListener.isOver()) {
      focusBorder.draw(batch, getX(), getY(), getWidth(), getHeight());
    }
  }
}
