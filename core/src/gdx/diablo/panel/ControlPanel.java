package gdx.diablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;
import gdx.diablo.codec.DC6;
import gdx.diablo.screen.GameScreen;
import gdx.diablo.widget.Button;

public class ControlPanel extends WidgetGroup implements Disposable {

  final AssetDescriptor<DC6> ctrlpnlDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\ctrlpnl7.DC6", DC6.class);
  HealthWidget healthWidget;
  ManaWidget manaWidget;
  ControlWidget controlWidget;

  final AssetDescriptor<DC6> hlthmanaDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\hlthmana.DC6", DC6.class);
  DC6 hlthmana;

  final AssetDescriptor<DC6> overlapDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\overlap.DC6", DC6.class);
  DC6 overlap;

  GameScreen gameScreen;

  public ControlPanel(GameScreen gameScreen) {
    this.gameScreen = gameScreen;
    Diablo.assets.load(hlthmanaDescriptor);
    Diablo.assets.finishLoadingAsset(hlthmanaDescriptor);
    hlthmana = Diablo.assets.get(hlthmanaDescriptor);

    Diablo.assets.load(overlapDescriptor);
    Diablo.assets.finishLoadingAsset(overlapDescriptor);
    overlap = Diablo.assets.get(overlapDescriptor);

    Diablo.assets.load(ctrlpnlDescriptor);
    Diablo.assets.finishLoadingAsset(ctrlpnlDescriptor);
    DC6 ctrlpnl = Diablo.assets.get(ctrlpnlDescriptor);

    int width = 0;
    int height = Integer.MIN_VALUE;
    final int numFrames = ctrlpnl.getNumFramesPerDir();
    for (int i = 1; i < numFrames - 2; i++) {
      Pixmap frame = ctrlpnl.getPixmap(0, i);
      width += frame.getWidth();
      height = Math.max(height, frame.getHeight());
    }

    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    pixmap.setBlending(Pixmap.Blending.None);
    int x = 0;
    for (int i = 1; i < numFrames - 2; i++) {
      Pixmap frame = ctrlpnl.getPixmap(0, i);
      pixmap.drawPixmap(frame, x, pixmap.getHeight() - frame.getHeight());
      x += frame.getWidth();
    }

    healthWidget = new HealthWidget(ctrlpnl.getTexture(0));
    manaWidget = new ManaWidget(ctrlpnl.getTexture(numFrames - 2));
    controlWidget = new ControlWidget(new Texture(new PixmapTextureData(pixmap, null, false, false, false)));

    healthWidget.setX(0);
    healthWidget.setY(1);
    controlWidget.setX(healthWidget.getWidth());
    manaWidget.setX(healthWidget.getWidth() + controlWidget.getWidth());
    manaWidget.setY(1);

    addActor(healthWidget);
    addActor(controlWidget);
    addActor(manaWidget);

    setWidth(healthWidget.getWidth() + controlWidget.getWidth() + manaWidget.getWidth());
    setHeight(height);
  }

  @Override
  public void draw(Batch batch, float a) {
    //batch.draw(ctrlpnl, getX(), getY());
    super.draw(batch, a);
  }

  @Override
  public void dispose() {
    Diablo.assets.unload(ctrlpnlDescriptor.fileName);
    Diablo.assets.unload(overlapDescriptor.fileName);
    Diablo.assets.unload(hlthmanaDescriptor.fileName);
    controlWidget.dispose();
  }

  private class HealthWidget extends Widget {
    TextureRegion background;
    TextureRegion health;
    TextureRegion overlay;

    HealthWidget(TextureRegion background) {
      this.background = background;
      setSize(background.getRegionWidth(), background.getRegionHeight());

      health = hlthmana.getTexture(0);
      overlay = overlap.getTexture(0);
    }

    @Override
    public void draw(Batch batch, float a) {
      batch.draw(background, getX(), getY());
      batch.draw(health, 30, 14);
      batch.draw(overlay, 28, 6);
      super.draw(batch, a);
    }
  }
  private class ManaWidget extends Widget {
    TextureRegion background;
    TextureRegion mana;
    TextureRegion overlay;

    ManaWidget(TextureRegion background) {
      this.background = background;
      setSize(background.getRegionWidth(), background.getRegionHeight());

      mana = hlthmana.getTexture(1);
      overlay = overlap.getTexture(1);
    }

    @Override
    public void draw(Batch batch, float a) {
      batch.draw(background, getX(), getY());
      batch.draw(mana, 435, 14);
      batch.draw(overlay, 435, 10);
      super.draw(batch, a);
    }
  }
  private class ControlWidget extends WidgetGroup implements Disposable {
    final AssetDescriptor<DC6> menubuttonDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\menubutton.DC6", DC6.class);
    Button btnMenu;
    Button.ButtonStyle menuHidden, menuShown;

    final AssetDescriptor<DC6> minipanelDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\minipanel.dc6", DC6.class);
    MinipanelWidget minipanelWidget;

    Texture background;
    ControlWidget(Texture background) {
      this.background = background;
      setSize(background.getWidth(), background.getHeight() - 7);

      Diablo.assets.load(menubuttonDescriptor);
      Diablo.assets.finishLoadingAsset(menubuttonDescriptor);
      menuHidden = new Button.ButtonStyle() {{
        up   = new TextureRegionDrawable(Diablo.assets.get(menubuttonDescriptor).getTexture(0));
        down = new TextureRegionDrawable(Diablo.assets.get(menubuttonDescriptor).getTexture(1));
      }};
      menuShown = new Button.ButtonStyle() {{
        up   = new TextureRegionDrawable(Diablo.assets.get(menubuttonDescriptor).getTexture(2));
        down = new TextureRegionDrawable(Diablo.assets.get(menubuttonDescriptor).getTexture(3));
      }};
      btnMenu = new Button(menuHidden);
      btnMenu.setPosition((getWidth() / 2) - (btnMenu.getWidth() / 2), 15);
      btnMenu.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          btnMenu.setStyle(btnMenu.getStyle() == menuHidden ? menuShown : menuHidden);
          minipanelWidget.setVisible(!minipanelWidget.isVisible());
        }
      });
      addActor(btnMenu);

      Diablo.assets.load(minipanelDescriptor);
      Diablo.assets.finishLoadingAsset(minipanelDescriptor);
      minipanelWidget = new MinipanelWidget(Diablo.assets.get(minipanelDescriptor).getTexture(0));
      minipanelWidget.setPosition((getWidth() / 2) - (minipanelWidget.getWidth() / 2), getHeight());
      addActor(minipanelWidget);
    }

    @Override
    public void dispose() {
      btnMenu.dispose();
      Diablo.assets.unload(minipanelDescriptor.fileName);
      minipanelWidget.dispose();
      background.dispose();
      Diablo.assets.unload(menubuttonDescriptor.fileName);
    }

    @Override
    public void draw(Batch batch, float a) {
      batch.draw(background, getX(), getY());
      super.draw(batch, a);
    }

    private class MinipanelWidget extends WidgetGroup implements Disposable {
      final AssetDescriptor<DC6> minipanelbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\minipanelbtn.DC6", DC6.class);

      Button btnCharacter;
      Button btnInventory;
      Button btnSkillTree;
      Button btnParty;
      Button btnMap;
      Button btnMessages;
      Button btnQuests;
      Button btnEscapeMenu;

      TextureRegion background;
      MinipanelWidget(TextureRegion background) {
        this.background = background;
        setSize(background.getRegionWidth(), background.getRegionHeight());
        setVisible(false);

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
        Table table = new Table();
        table.setFillParent(true);
        table.align(Align.topLeft);
        table.pad(3);
        table.add(btnCharacter).space(1);
        table.add(btnInventory).space(1);
        table.add(btnSkillTree).space(1);
        table.add(btnParty).space(1);
        table.add(btnMap).space(1);
        table.add(btnMessages).space(1);
        table.add(btnQuests).space(1);
        table.add(btnEscapeMenu).space(1);
        addActor(table);
      }

      @Override
      public void dispose() {
        btnCharacter.dispose();
        btnInventory.dispose();
        Diablo.assets.unload(minipanelbtnDescriptor.fileName);
      }

      @Override
      public void draw(Batch batch, float a) {
        batch.draw(background, getX(), getY());
        super.draw(batch, a);
      }
    }
  }
}
