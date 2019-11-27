package com.riiablo.screen.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.widget.Button;

public class MobilePanel extends Table implements Disposable, EscapeController {
  final AssetDescriptor<DC6> minipanelbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\minipanelbtn.DC6", DC6.class);

  Button btnCharacter;
  Button btnInventory;
  Button btnSkillTree;
  Button btnParty;
  Button btnMap;
  Button btnMessages;
  Button btnQuests;
  Button btnEscapeMenu;
  Button btnSwapWeapons;

  public MobilePanel() {
    Riiablo.assets.load(minipanelbtnDescriptor);
    Riiablo.assets.finishLoadingAsset(minipanelbtnDescriptor);
    final DC6 minipanelbtn = Riiablo.assets.get(minipanelbtnDescriptor);
    ClickListener clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnCharacter) {
          Actor panel = Riiablo.game.characterPanel;
          Riiablo.game.setLeftPanel(panel.isVisible() ? null : panel);
        } else if (actor == btnSwapWeapons) {
          Riiablo.charData.alternate();
        } else if (actor == btnInventory) {
          Actor panel = Riiablo.game.inventoryPanel;
          Riiablo.game.setRightPanel(panel.isVisible() ? null : panel);
        } else if (actor == btnSkillTree) {
          Actor panel = Riiablo.game.spellsPanel;
          Riiablo.game.setRightPanel(panel.isVisible() ? null : panel);
        } else if (actor == btnParty) {

        } else if (actor == btnMap) {

        } else if (actor == btnMessages) {
          Riiablo.game.input.setVisible(!Riiablo.game.input.isVisible());
          Riiablo.game.input.getStage().setKeyboardFocus(Riiablo.game.input);
        } else if (actor == btnQuests) {
          Actor panel = Riiablo.game.questsPanel;
          Riiablo.game.setLeftPanel(panel.isVisible() ? null : panel);
        } else if (actor == btnEscapeMenu) {
          Riiablo.game.escapePanel.setVisible(!Riiablo.game.escapePanel.isVisible());
        }
      }
    };
    btnCharacter = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(0));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(1));
    }});
    btnCharacter.addListener(clickListener);
    btnInventory = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(2));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(3));
    }});
    btnInventory.addListener(clickListener);
    btnSkillTree = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(4));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(5));
    }});
    btnSkillTree.addListener(clickListener);
    btnParty = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(6));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(7));
    }});
    btnParty.addListener(clickListener);
    btnMap = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(8));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(9));
    }});
    btnMap.addListener(clickListener);
    btnMessages = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(10));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(11));
    }});
    btnMessages.addListener(clickListener);
    btnQuests = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(12));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(13));
    }});
    btnQuests.addListener(clickListener);
    btnEscapeMenu = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(14));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(15));
    }});
    btnEscapeMenu.addListener(clickListener);
    btnSwapWeapons = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(minipanelbtn.getTexture(16));
      down = new TextureRegionDrawable(minipanelbtn.getTexture(17));
    }});
    btnSwapWeapons.addListener(clickListener);
    final float size = 50;
    add(btnCharacter).size(size);
    add(btnParty).size(size);
    add(btnQuests).size(size);
    add(btnSwapWeapons).size(size);
    add().expandX();
    add(btnInventory).size(size);
    add(btnSkillTree).size(size);
    //add(btnMap).size(size);
    add(btnMessages).size(size);
    add(btnEscapeMenu).size(size);
    pack();
    setTouchable(Touchable.childrenOnly);
    //setDebug(true, true);
  }

  @Override
  public Actor getEscapeButton() {
    return btnEscapeMenu;
  }

  @Override
  public void dispose() {
    btnCharacter.dispose();
    btnInventory.dispose();
    Riiablo.assets.unload(minipanelbtnDescriptor.fileName);
  }
}
