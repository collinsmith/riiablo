package gdx.diablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;
import gdx.diablo.codec.DC6;
import gdx.diablo.screen.GameScreen;
import gdx.diablo.widget.Button;

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
    Diablo.assets.load(minipanelbtnDescriptor);
    Diablo.assets.finishLoadingAsset(minipanelbtnDescriptor);
    ClickListener clickListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Actor actor = event.getListenerActor();
        if (actor == btnCharacter) {
          gameScreen.characterPanel.setVisible(!gameScreen.characterPanel.isVisible());
        } else if (actor == btnInventory) {
          gameScreen.inventoryPanel.setVisible(!gameScreen.inventoryPanel.isVisible());
        } else if (actor == btnSkillTree) {

        } else if (actor == btnParty) {

        } else if (actor == btnMap) {

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
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(0));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(1));
    }});
    btnCharacter.addListener(clickListener);
    btnInventory = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(2));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(3));
    }});
    btnInventory.addListener(clickListener);
    btnSkillTree = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(4));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(5));
    }});
    btnSkillTree.addListener(clickListener);
    btnParty = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(6));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(7));
    }});
    btnParty.addListener(clickListener);
    btnMap = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(8));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(9));
    }});
    btnMap.addListener(clickListener);
    btnMessages = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(11));
    }});
    btnMessages.addListener(clickListener);
    btnQuests = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(12));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(13));
    }});
    btnQuests.addListener(clickListener);
    btnEscapeMenu = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(14));
      down = new TextureRegionDrawable(Diablo.assets.get(minipanelbtnDescriptor).getTexture(15));
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
    pack();
    setTouchable(Touchable.enabled);
  }

  @Override
  public void dispose() {
    btnCharacter.dispose();
    btnInventory.dispose();
    Diablo.assets.unload(minipanelbtnDescriptor.fileName);
  }
}
