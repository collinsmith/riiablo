package com.riiablo.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.graphics.BlendMode;
import com.riiablo.key.MappedKey;

public class HotkeyButton extends Button {
  MappedKey mapping;
  Label label;

  public HotkeyButton(final DC dc, final int index) {
    super(new ButtonStyle() {{
      up       = new TextureRegionDrawable(dc.getTexture(index));
      down     = new TextureRegionDrawable(dc.getTexture(index + 1));
      disabled = up;
      pressedOffsetX = pressedOffsetY = -2;
    }});

    add(label = new Label("", Riiablo.fonts.font16, Riiablo.colors.gold));
    align(Align.topRight);
    pad(2);
    pack();

    setDisabledBlendMode(BlendMode.DARKEN, Riiablo.colors.darkenR);
  }

  public void map(MappedKey mapping) {
    this.mapping = mapping;
    label.setText(Input.Keys.toString(mapping.getPrimaryAssignment()));
  }

  public MappedKey getMapping() {
    return mapping;
  }

  public void copy(HotkeyButton other) {
    setStyle(other.getStyle());
    label.setText(other.label.getText());
  }
}
