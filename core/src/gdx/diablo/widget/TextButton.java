package gdx.diablo.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import gdx.diablo.Diablo;

public class TextButton extends Button {
  private final Label label;
  private TextButtonStyle style;

  public TextButton(int id, TextButtonStyle style) {
    this(Diablo.string.lookup(id), style);
  }

  public TextButton(String text, TextButtonStyle style) {
    super(style);
    label = new Label(text, style.font);
    label.getStyle().fontColor = style.fontColor;
    label.setAlignment(Align.center);
    add(label).expand().fill();
    label.setWrap(true);
    label.setColor(Color.BLACK);
  }

  @Override
  public void setStyle(com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle buttonStyle) {
    if (!(buttonStyle instanceof TextButtonStyle)) throw new IllegalArgumentException("style must be a TextButtonStyle. ");
    style = (TextButtonStyle) buttonStyle;
    super.setStyle(buttonStyle);
    if (label != null) {
      Label.LabelStyle labelStyle = label.getStyle();
      labelStyle.font = style.font;
      labelStyle.fontColor = style.fontColor;
      label.setStyle(labelStyle);
    }
  }

  @Override
  public TextButtonStyle getStyle() {
    return style;
  }

  public String getText() {
    return label.getText().toString();
  }

  public void setText(int id) {
    label.setText(id);
  }

  public void setText(String text) {
    label.setText(text);
  }

  public static class TextButtonStyle extends ButtonStyle {
    public BitmapFont font;
    public Color fontColor;
    public TextButtonStyle() {
      this(null, null, null, null);
    }

    public TextButtonStyle(Drawable up, Drawable down, BitmapFont font) {
      this(up, down, null, font);
    }

    public TextButtonStyle (Drawable up, Drawable down, Drawable checked, BitmapFont font) {
      super(up, down, checked);
      unpressedOffsetY = -2;
      pressedOffsetX = -2;
      pressedOffsetY = unpressedOffsetY + pressedOffsetX;
      checkedOffsetX = unpressedOffsetX;
      checkedOffsetY = unpressedOffsetY;
      this.font = font;
      fontColor = Color.BLACK;
    }

    public TextButtonStyle(TextButtonStyle style) {
      super(style);
      this.font = style.font;
      fontColor = Color.BLACK;
    }
  }

}
