package com.riiablo.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.riiablo.Riiablo;
import com.riiablo.item.Attributes;
import com.riiablo.item.Stat;

public class StatLabel extends Label {
  Attributes attrs;
  int stat;
  int value;
  Colorizer colorizer;

  public StatLabel(Attributes attrs, int stat) {
    super(Riiablo.fonts.font16);
    this.attrs = attrs;
    this.stat = stat;
    colorizer = Colorizer.DEFAULT;
    updateSize = false;
  }

  @Override
  public void act(float delta) {
    updateValue();
    super.act(delta);
  }

  private void updateValue() {
    int curValue = attrs.get(stat).value();
    if (value != curValue) {
      value = curValue;
      setAlignment(Align.center);
      setText(Integer.toString(value));
      setColor(colorizer.getColor(attrs.get(stat)));
    }
  }

  @Override
  public void setText(CharSequence newText) {
    super.setText(newText);
    if (newText.length() > 6) {
      getStyle().font = Riiablo.fonts.ReallyTheLastSucker;
    } else if (newText.length() > 3) {
      getStyle().font = Riiablo.fonts.font8;
    } else {
      getStyle().font = Riiablo.fonts.font16;
    }
  }

  public enum Colorizer {
    DEFAULT {
      @Override
      Color getColor(Stat stat) {
        return stat.isModified()
            ? Riiablo.colors.blue
            : Riiablo.colors.white;
      }
    },
    RESISTANCE {
      @Override
      Color getColor(Stat stat) {
        if (stat.value() < 0) {
          return Riiablo.colors.red;
        }

        return stat.isModified()
            ? Riiablo.colors.blue
            : Riiablo.colors.white;
      }
    };

    abstract Color getColor(Stat stat);
  }
}
