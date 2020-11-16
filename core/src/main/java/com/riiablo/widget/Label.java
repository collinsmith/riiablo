package com.riiablo.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Riiablo;
import com.riiablo.codec.FontTBL;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.graphics.PaletteIndexedColorDrawable;

public class Label extends com.badlogic.gdx.scenes.scene2d.ui.Label {
  public static final Drawable MODAL = new PaletteIndexedColorDrawable(Riiablo.colors.modal75) {{
    final float padding = 2;
    setLeftWidth(padding);
    setTopHeight(padding);
    setRightWidth(padding);
    setBottomHeight(padding);
  }};

  boolean updateSize = true; // FIXME: find a less hacky solution

  public Label(int id, BitmapFont font) {
    this(id == -1 ? "" : Riiablo.string.lookup(id), font);
  }

  public Label(int id, BitmapFont font, Color color) {
    this(id, font);
    setColor(color);
  }

  public Label(String text, BitmapFont font, int align) {
    super(text, new LabelStyle(font, null));
    setAlignment(align);
  }

  public Label(String text, BitmapFont font) {
    super(text, new LabelStyle(font, null));
  }

  public Label(String text, BitmapFont font, Color color) {
    this(text, font);
    setColor(color);
  }

  public Label(BitmapFont font) {
    this(null, font);
  }

  public Label(LabelStyle style) {
    super(null, style);
  }

  public Label(Label src) {
    super(src.getText(), src.getStyle());
    setColor(src.getColor());
  }

  public static Label i18n(String id, BitmapFont font) {
    return new Label(Riiablo.string.lookup(id), font);
  }

  public static Label i18n(String id, BitmapFont font, Color color) {
    return new Label(Riiablo.string.lookup(id), font, color);
  }

  @Override
  public void draw(Batch batch, float a) {
    if (batch instanceof PaletteIndexedBatch) {
      draw((PaletteIndexedBatch) batch, a);
    } else {
      throw new GdxRuntimeException("Not supported");
    }
  }

  public void draw(PaletteIndexedBatch batch, float a) {
    validate();

    LabelStyle style = getStyle();
    if (style != null) {
      Drawable background = style.background;
      if (background != null) {
        background.draw(batch,
            getX() - background.getLeftWidth(), getY() - background.getBottomHeight(),
            getWidth() + background.getMinWidth(), getHeight() + background.getMinHeight());
      }
    }

    batch.setBlendMode(((FontTBL.BitmapFont) getStyle().font).getBlendMode());
    BitmapFontCache cache = getBitmapFontCache();
    cache.setPosition(getX(), getY());
    cache.tint(getColor());
    cache.draw(batch);
    batch.resetBlendMode();
  }

  @Override
  public boolean setText(int id) {
    setText(id == -1 ? "" : Riiablo.string.lookup(id));
    return true;
  }

  @Override
  public void setText(CharSequence newText) {
    super.setText(newText);
    if (updateSize) setSize(getPrefWidth(), getPrefHeight());
  }
}
