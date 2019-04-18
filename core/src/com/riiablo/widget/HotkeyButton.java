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
  Label hotkey;
  Label charges;
  int skillId;

  public HotkeyButton(final DC dc, final int index, int skillId) {
    super(new ButtonStyle() {{
      up       = new TextureRegionDrawable(dc.getTexture(index));
      down     = new TextureRegionDrawable(dc.getTexture(index + 1));
      disabled = up;
      pressedOffsetX = pressedOffsetY = -2;
    }});

    this.skillId = skillId;
    add(hotkey = new Label("", Riiablo.fonts.font16, Riiablo.colors.gold)).align(Align.topRight);
    row();
    add().grow();
    row();
    add(charges = new Label("", Riiablo.fonts.font16, Riiablo.colors.blue)).align(Align.bottomLeft);
    pad(2);
    pack();

    setDisabledBlendMode(BlendMode.DARKEN, Riiablo.colors.darkenRed);
  }

  public void map(MappedKey mapping) {
    this.mapping = mapping;
    hotkey.setText(Input.Keys.toString(mapping.getPrimaryAssignment()));
  }

  public MappedKey getMapping() {
    return mapping;
  }

  public int getSkill() {
    return skillId;
  }

  public void copy(HotkeyButton other) {
    setStyle(other.getStyle());
    setBlendMode(other.blendMode, other.color);
    setDisabledBlendMode(other.disabledBlendMode, other.disabledColor);
    setHighlightedBlendMode(other.highlightedBlendMode, other.highlightedColor);
    hotkey.setText(other.hotkey.getText());
    skillId = other.skillId;
  }
}
