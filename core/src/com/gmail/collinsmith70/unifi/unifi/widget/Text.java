package com.gmail.collinsmith70.unifi.unifi.widget;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.gmail.collinsmith70.unifi.unifi.Widget;
import com.gmail.collinsmith70.unifi.unifi.graphics.Canvas;
import com.gmail.collinsmith70.unifi.unifi.graphics.Paint;
import com.google.common.base.Strings;

public class Text extends Widget {

  @NonNull
  private String text;

  @NonNull
  private BitmapFont font;

  @NonNull
  private Color textColor;

  @NonNull
  private GlyphLayout glyphLayout;

  public Text(@NonNull final BitmapFont font) {
    this(null, font);
  }

  public Text(@Nullable final String text, @NonNull final BitmapFont font) {
    if (font == null) {
      throw new IllegalArgumentException("font cannot be null");
    }

    this.text = Strings.nullToEmpty(text);
    this.font = font;
    this.textColor = Color.WHITE;

    this.glyphLayout = new GlyphLayout(getFont(), getText());
    setPreferredWidth(Math.round(glyphLayout.width));
    setPreferredHeight(Math.round(glyphLayout.height));
    setDebug(null);
  }

  @NonNull
  public String getText() {
    return text;
  }

  public void setText(@Nullable final String text) {
    this.text = Strings.nullToEmpty(text);
    this.glyphLayout = new GlyphLayout(font, this.text);
    setPreferredWidth(Math.round(glyphLayout.width));
    setPreferredHeight(Math.round(glyphLayout.height));
  }

  @NonNull
  public BitmapFont getFont() {
    return font;
  }

  public void setFont(@NonNull final BitmapFont font) {
    if (font == null) {
      throw new IllegalArgumentException("font cannot be null");
    }

    this.font = font;
  }

  @NonNull
  public Color getTextColor() {
    return textColor;
  }

  public void setTextColor(@NonNull final Color textColor) {
    if (textColor == null) {
      throw new IllegalArgumentException("textColor cannot be null");
    }

    this.textColor = textColor;
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int length() {
    return text.length();
  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  protected void onDraw(@NonNull final Canvas canvas) {
    Paint paint = new Paint();
    paint.setBlendingMode(Pixmap.Blending.SourceOver);
    paint.setFilterMode(Pixmap.Filter.BiLinear);
    canvas.drawText(0, 0, text, paint, getFont());
    //font.setColor(textColor);
    //font.draw(batch, glyphLayout, getX(), getY() + glyphLayout.height);
  }

  @Override
  public void dispose() {
    super.dispose();
    font.dispose();
  }

}
