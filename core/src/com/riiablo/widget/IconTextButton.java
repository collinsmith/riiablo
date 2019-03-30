package com.riiablo.widget;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.riiablo.codec.FontTBL;

public class IconTextButton extends Table {
  final Button button;
  final LabelButton label;

  public IconTextButton(Button.ButtonStyle style, String text, FontTBL.BitmapFont font) {
    add(button = new Button(style)).fill(false).pad(2).padLeft(1).padTop(3);
    add(label = new LabelButton(text, font)).grow().spaceLeft(25).row();
    setTouchable(Touchable.enabled);
    addListener(new InputListener() {
      @Override
      public boolean handle(Event e) {
        button.getClickListener().handle(e);
        label.clickListener.handle(e);
        return true;
      }
    });
  }

}
