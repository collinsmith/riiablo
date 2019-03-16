package com.riiablo.panel;

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
import com.riiablo.entity.Entity;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;

public class MobilePanel extends Table implements Disposable {
  final AssetDescriptor<DC6> minipanelbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\minipanelbtn.DC6", DC6.class);

  GameScreen gameScreen;

  Button btnCharacter;
  Button btnInventory;
  Button btnSkillTree;
  Button btnParty;
  Button btnMap;
  Button btnMessages;
  Button btnQuests;
  Button btnEscapeMenu;

  public MobilePanel(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;
    Riiablo.assets.load(minipanelbtnDescriptor);
    Riiablo.assets.finishLoadingAsset(minipanelbtnDescriptor);
    ClickListener clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnCharacter) {
          gameScreen.characterPanel.setVisible(!gameScreen.characterPanel.isVisible());
        } else if (actor == btnInventory) {
          gameScreen.inventoryPanel.setVisible(!gameScreen.inventoryPanel.isVisible());
        } else if (actor == btnSkillTree) {
          gameScreen.spellsPanel.setVisible(!gameScreen.spellsPanel.isVisible());
        } else if (actor == btnParty) {

        } else if (actor == btnMap) {
          if (!gameScreen.labels.isEmpty()) {
            for (Actor label : gameScreen.labels) {
              Object obj = label.getUserObject();
              if (obj instanceof Entity) {
                Entity entity = (Entity) obj;
                if (entity.isSelectable()) entity.interact(gameScreen);
                break;
              }
            }
          }
        } else if (actor == btnMessages) {
          gameScreen.input.setVisible(!gameScreen.input.isVisible());
          gameScreen.input.getStage().setKeyboardFocus(gameScreen.input);
        } else if (actor == btnQuests) {

        } else if (actor == btnEscapeMenu) {
          gameScreen.escapePanel.setVisible(!gameScreen.escapePanel.isVisible());
        }
      }
    };
    btnCharacter = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(0));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(1));
    }});
    btnCharacter.addListener(clickListener);
    btnInventory = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(2));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(3));
    }});
    btnInventory.addListener(clickListener);
    btnSkillTree = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(4));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(5));
    }});
    btnSkillTree.addListener(clickListener);
    btnParty = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(6));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(7));
    }});
    btnParty.addListener(clickListener);
    btnMap = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(8));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(9));
    }});
    btnMap.addListener(clickListener);
    btnMessages = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(11));
    }});
    btnMessages.addListener(clickListener);
    btnQuests = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(12));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(13));
    }});
    btnQuests.addListener(clickListener);
    btnEscapeMenu = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(14));
      down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(15));
    }});
    btnEscapeMenu.addListener(clickListener);
    final float size = 50;
    add(btnCharacter).size(size);
    add(btnInventory).size(size);
    add(btnSkillTree).size(size);
    //add(btnParty).size(size);
    //add(btnMap).size(size);
    add(btnMessages).size(size);
    //add(btnQuests).size(size);
    add(btnEscapeMenu).size(size);
    add(btnMap).size(size);
    pack();
    setTouchable(Touchable.enabled);
  }

  @Override
  public void dispose() {
    btnCharacter.dispose();
    btnInventory.dispose();
    Riiablo.assets.unload(minipanelbtnDescriptor.fileName);
  }
}
