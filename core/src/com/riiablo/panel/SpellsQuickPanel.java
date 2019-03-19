package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.CharacterClass;
import com.riiablo.Keys;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.entity.Player;
import com.riiablo.graphics.BlendMode;
import com.riiablo.key.MappedKey;
import com.riiablo.key.MappedKeyStateAdapter;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.HotkeyButton;

import org.apache.commons.lang3.ArrayUtils;

public class SpellsQuickPanel extends Table implements Disposable {
  final AssetDescriptor<DC6> SkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\Skillicon.DC6", DC6.class);
  DC6 Skillicon;

  final AssetDescriptor<DC6> CharSkilliconDescriptor;
  DC6 CharSkillicon;

  GameScreen gameScreen;
  ObjectMap<MappedKey, HotkeyButton> keyMappings;
  MappedKeyStateAdapter mappedKeyListener;

  public SpellsQuickPanel(final GameScreen gameScreen, final boolean leftSkills) {
    this.gameScreen = gameScreen;

    Riiablo.assets.load(SkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(SkilliconDescriptor);
    Skillicon = Riiablo.assets.get(SkilliconDescriptor);

    Player player = gameScreen.player;
    CharacterClass charClass = player.charClass;
    CharSkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\" + charClass.spellIcons + ".DC6", DC6.class);
    Riiablo.assets.load(CharSkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(CharSkilliconDescriptor);
    CharSkillicon = Riiablo.assets.get(CharSkilliconDescriptor);

    keyMappings = new ObjectMap<>(31);
    Table top = new Table() {{
      add(new HotkeyButton(Skillicon, 14));
      add(new HotkeyButton(Skillicon, 18));
      pack();
    }};
    Table[] tables = new Table[5];
    for (int i = charClass.firstSpell; i < charClass.lastSpell; i++) {
      if (player.skills.getLevel(i) <= 0) continue;

      final Skills.Entry skill = Riiablo.files.skills.get(i);
      if (leftSkills && !skill.leftskill) continue;
      if (skill.passive) continue;

      final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
      Table table = tables[desc.ListRow];
      if (table == null) table = tables[desc.ListRow] = new Table();
      final HotkeyButton button = new HotkeyButton(CharSkillicon, desc.IconCel);
      if (skill.aura) {
        button.setBlendMode(BlendMode.DARKEN, Riiablo.colors.darkenGold);
      }

      int index = ArrayUtils.indexOf(player.skillBar, i);
      if (index != ArrayUtils.INDEX_NOT_FOUND) {
        MappedKey mapping = Keys.Skill[index];
        button.map(mapping);
        keyMappings.put(mapping, button);
      }

      button.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ControlPanel controlPanel = gameScreen.controlPanel;
          if (leftSkills) {
            controlPanel.getLeftSkill().copy(button);
          } else {
            controlPanel.getRightSkill().copy(button);
          }
          SpellsQuickPanel.this.setVisible(false);
        }
      });
      table.add(button);
    }
    Table bottom = new Table() {{
      add(new HotkeyButton(Skillicon, 4));
      add(new HotkeyButton(Skillicon, 6));
      add(new HotkeyButton(Skillicon, 2));
      pack();
    }};
    add(top).align(leftSkills ? Align.left : Align.right).row();
    for (int i = tables.length - 1; i >= 0; i--) {
      if (tables[i] != null) {
        add(tables[i]).align(leftSkills ? Align.left : Align.right).row();
      }
    }
    add(bottom).align(leftSkills ? Align.left : Align.right).row();
    pack();
    //setDebug(true, true);

    mappedKeyListener = new MappedKeyStateAdapter() {
      @Override
      public void onPressed(MappedKey key, int keycode) {
        HotkeyButton button = keyMappings.get(key);
        if (button == null) return;
        // TODO: Assign
        ControlPanel controlPanel = gameScreen.controlPanel;
        if (leftSkills) {
          controlPanel.getLeftSkill().copy(button);
        } else {
          controlPanel.getRightSkill().copy(button);
        }
      }
    };
    for (MappedKey Skill : Keys.Skill) Skill.addStateListener(mappedKeyListener);
  }

  @Override
  public void dispose() {
    for (MappedKey Skill : Keys.Skill) Skill.removeStateListener(mappedKeyListener);
    Riiablo.assets.unload(SkilliconDescriptor.fileName);
    Riiablo.assets.unload(CharSkilliconDescriptor.fileName);
  }
}
