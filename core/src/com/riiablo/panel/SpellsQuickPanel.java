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
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.entity.Player;
import com.riiablo.graphics.BlendMode;
import com.riiablo.key.MappedKey;
import com.riiablo.key.MappedKeyStateAdapter;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.HotkeyButton;

import org.apache.commons.lang3.ArrayUtils;

public class SpellsQuickPanel extends Table implements Disposable {
  private static final String SPELLS_PATH = "data\\global\\ui\\SPELLS\\";

  final AssetDescriptor<DC6> SkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\Skillicon.DC6", DC6.class);
  DC6 Skillicon;

  final AssetDescriptor<DC6> CharSkilliconDescriptor[];
  DC6 CharSkillicon[];

  private static int getClassId(String charClass) {
    switch (charClass.charAt(0)) {
      case 'a': return charClass.charAt(1) == 'm' ? CharacterClass.AMAZON.id : CharacterClass.ASSASSIN.id;
      case 'b': return CharacterClass.BARBARIAN.id;
      case 'd': return CharacterClass.DRUID.id;
      case 'n': return CharacterClass.NECROMANCER.id;
      case 'p': return CharacterClass.PALADIN.id;
      case 's': return CharacterClass.SORCERESS.id;
      default:  return -1;
    }
  }

  private DC getSkillicon(String charClass, int i) {
    int classId = getClassId(charClass);
    DC icons = classId == -1 ? Skillicon : CharSkillicon[classId];
    return i < icons.getNumPages() ? icons : null;
  }

  GameScreen gameScreen;
  ObjectMap<MappedKey, HotkeyButton> keyMappings;
  MappedKeyStateAdapter mappedKeyListener;

  public SpellsQuickPanel(final GameScreen gameScreen, final boolean leftSkills) {
    this.gameScreen = gameScreen;

    Riiablo.assets.load(SkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(SkilliconDescriptor);
    Skillicon = Riiablo.assets.get(SkilliconDescriptor);

    CharSkilliconDescriptor = new AssetDescriptor[7];
    CharSkillicon = new DC6[CharSkilliconDescriptor.length];
    for (int i = 0; i < CharSkilliconDescriptor.length; i++) {
      CharSkilliconDescriptor[i] = new AssetDescriptor<>(SPELLS_PATH + CharacterClass.get(i).spellIcons + ".DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
      Riiablo.assets.load(CharSkilliconDescriptor[i]);
      Riiablo.assets.finishLoadingAsset(CharSkilliconDescriptor[i]);
      CharSkillicon[i] = Riiablo.assets.get(CharSkilliconDescriptor[i]);
    }

    Player player = gameScreen.player;
    CharacterClass charClass = player.charClass;
    keyMappings = new ObjectMap<>(31);
    Table top = new Table() {{
      add(new HotkeyButton(Skillicon, 14));
      add(new HotkeyButton(Skillicon, 18));
      pack();
    }};
    Table[] tables = new Table[5];
    // TODO: Include non-class spells gained from items
    for (int i = charClass.firstSpell; i < charClass.lastSpell; i++) {
      if (player.skills.getLevel(i) <= 0) continue;

      final Skills.Entry skill = Riiablo.files.skills.get(i);
      if (leftSkills && !skill.leftskill) continue;
      if (skill.passive) continue;

      final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
      Table table = tables[desc.ListRow];
      if (table == null) table = tables[desc.ListRow] = new Table();
      int iconCel = desc.IconCel;
      DC icons = getSkillicon(skill.charclass, iconCel);
      if (icons == null) {
        icons = Skillicon;
        iconCel = 20;
      }
      final HotkeyButton button = new HotkeyButton(icons, iconCel);
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
    for (AssetDescriptor assetDescriptor : CharSkilliconDescriptor) Riiablo.assets.unload(assetDescriptor.fileName);
  }
}
