package com.riiablo.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;

import com.riiablo.Riiablo;
import com.riiablo.attributes.Attributes;
import com.riiablo.attributes.StatRef;

public class StatLabel extends Label {
  Attributes attrs;
  short stat;
  int value;
  Colorizer colorizer;

  public StatLabel(Attributes attrs, short stat) {
    this(attrs, stat, Colorizer.DEFAULT);
  }

  public StatLabel(Attributes attrs, short stat, Colorizer colorizer) {
    super(Riiablo.fonts.font16);
    this.attrs = attrs;
    this.stat = stat;
    this.colorizer = colorizer;
    updateSize = false;
  }

  @Override
  public void act(float delta) {
    updateValue();
    super.act(delta);
  }

  private void updateValue() {
    StatRef s = attrs.get(stat);
    int curValue = s.asInt();
    if (value != curValue) {
      value = curValue;
      setAlignment(Align.center);
      setText(Integer.toString(value));
      setColor(colorizer.getColor(attrs.get(stat)));
    }
  }

  @Override
  public void setText(CharSequence newText) {
    BitmapFont font = getFont(newText.length());
    if (font != getStyle().font) {
      getStyle().font = font;
      setStyle(getStyle()); // hacky, but only way to correct update style with changes
    }

    super.setText(newText);
  }

  private static BitmapFont getFont(int len) {
    if (len > 6) {
      return Riiablo.fonts.ReallyTheLastSucker;
    } else if (len > 3) {
      return Riiablo.fonts.font8;
    } else {
      return Riiablo.fonts.font16;
    }
  }

  public enum Colorizer {
    DEFAULT {
      @Override
      Color getColor(StatRef stat) {
        return stat.modified()
            ? Riiablo.colors.blue
            : Riiablo.colors.white;
      }
    },
    RESISTANCE {
      @Override
      Color getColor(StatRef stat) {
        int value = stat.asInt();
        if (value < 0) {
          return Riiablo.colors.red;
        } else if (value < 75) {
          return Riiablo.colors.white;
        //} else if (value == MAX) {
        //  return Riiablo.colors.gold;
        //} else if (75 < value < MAX) {
        //  return Riiablo.colors.blue;
        } else {
          return Riiablo.colors.white;
        }
      }
    };

    abstract Color getColor(StatRef stat);
  }
}
