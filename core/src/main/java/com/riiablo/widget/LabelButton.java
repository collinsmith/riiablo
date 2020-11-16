package com.riiablo.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.riiablo.Riiablo;
import com.riiablo.graphics.PaletteIndexedBatch;

public class LabelButton extends Label {
  ClickListener clickListener;

  public LabelButton(int id, BitmapFont font) {
    super(id, font);
    init();
  }

  public LabelButton(int id, BitmapFont font, Color color) {
    super(id, font, color);
    init();
  }

  public LabelButton(String text, BitmapFont font) {
    super(text, font);
    init();
  }

  public LabelButton(String text, BitmapFont font, Color color) {
    super(text, font, color);
    init();
  }

  public LabelButton(BitmapFont font) {
    super(font);
    init();
  }

  public static LabelButton i18n(String id, BitmapFont font) {
    return new LabelButton(Riiablo.string.lookup(id), font);
  }

  public static LabelButton i18n(String id, BitmapFont font, Color color) {
    return new LabelButton(Riiablo.string.lookup(id), font, color);
  }

  private void init() {
    addListener(clickListener = new ClickListener());
  }

  @Override
  public void draw(PaletteIndexedBatch batch, float a) {
    setColor(clickListener.isOver() ? Riiablo.colors.blue : Riiablo.colors.white);
    super.draw(batch, a);
  }
}
