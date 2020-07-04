package com.riiablo.screen.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.graphics.BlendMode;
import com.riiablo.item.Stat;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;
import com.riiablo.widget.LabelButton;

public class VendorPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "VendorPanel";

  final AssetDescriptor<DC6> buysellDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysell.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion buysell;

  final AssetDescriptor<DC6> buyselltabsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buyselltabs.DC6", DC6.class);
  DC buyselltabs;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnBuy;
  Button btnSell;
  Button btnRepair;
  Button btnBlank;
  Button btnExit;

  Tab[] tabs;

  final Inventory.Entry inventory;

  public VendorPanel() {
    Riiablo.assets.load(buysellDescriptor);
    Riiablo.assets.finishLoadingAsset(buysellDescriptor);
    buysell = Riiablo.assets.get(buysellDescriptor).getTexture();
    setSize(buysell.getRegionWidth(), buysell.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    Riiablo.assets.load(buyselltabsDescriptor);
    Riiablo.assets.finishLoadingAsset(buyselltabsDescriptor);
    buyselltabs = Riiablo.assets.get(buyselltabsDescriptor);

    float tabY = getHeight() - buyselltabs.getTexture().getRegionHeight();
    float tabX = 0;
    tabs = new Tab[4];
    for (int i = 0; i < tabs.length; i++) {
      Tab tab = tabs[i] = new Tab(buyselltabs.getTexture(i), buyselltabs.getTexture(i + tabs.length));
      tab.setPosition(tabX, tabY);
      tabX += tab.getWidth() + 1;
      addActor(tab);
    }

    String[] names = {"strBSArmor", "strBSWeapons", "strBSWeapons", "strBSMisc"};
    LabelButton[] labels = new LabelButton[4];
    for (int i = 0; i < labels.length; i++) {
      final LabelButton label = labels[i] = LabelButton.i18n(names[i], Riiablo.fonts.font16);
      label.setAlignment(Align.center);
      Tab tab = tabs[i];
      label.setPosition(tab.getX(), tab.getY());
      label.setSize(tab.getWidth(), tab.getHeight());
      label.setUserObject(tab);
      label.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          for (Tab tab : tabs) tab.setMode(Tab.INACTIVE);
          Tab tab = (Tab) label.getUserObject();
          tab.setMode(Tab.ACTIVE);
        }
      });
      addActor(label);
    }

    Riiablo.assets.load(buysellbtnDescriptor);
    Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
    btnExit = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(272, 15);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    btnBuy = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(2));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(3));
      checked = down;
    }});
    btnBuy.setPosition(116, 15);
    btnBuy.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {

      }
    });
    addActor(btnBuy);

    btnSell = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(4));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(5));
      checked = down;
    }});
    btnSell.setPosition(168, 15);
    btnSell.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {

      }
    });
    addActor(btnSell);

    btnBlank = new Button(new Button.ButtonStyle() {{
      up = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(0));
    }});
    btnBlank.setDisabledBlendMode(BlendMode.NONE, Riiablo.colors.white);
    btnBlank.setDisabled(true);
    btnBlank.setPosition(220, 15);
    btnBlank.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {

      }
    });
    addActor(btnBlank);

    btnRepair = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(6));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(7));
    }});
    btnRepair.setPosition(220, 15);
    btnRepair.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {

      }
    });
    btnRepair.setVisible(false);
    addActor(btnRepair);

    Label goldbankLabel = Label.i18n("stash", Riiablo.fonts.font16);
    goldbankLabel.setSize(180, 16);
    goldbankLabel.setPosition(20, 57);
    addActor(goldbankLabel);

    Label goldbank = new Label(Integer.toString(Riiablo.charData.getStats().get(Stat.goldbank).value()), Riiablo.fonts.font16);
    goldbank.setSize(goldbankLabel.getWidth(), goldbankLabel.getHeight());
    goldbank.setPosition(goldbankLabel.getX(), goldbankLabel.getY());
    goldbank.setAlignment(Align.right);
    addActor(goldbank);

    // TODO: add support for special vendor grid /w pages
    inventory = Riiablo.files.inventory.get("Monster");
    ItemGrid grid = new ItemGrid(inventory, null);
    grid.setPosition(
        inventory.gridLeft - inventory.invLeft,
        getHeight() - inventory.gridTop - grid.getHeight());
    addActor(grid);

    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnExit.dispose();
    btnBuy.dispose();
    btnSell.dispose();
    btnRepair.dispose();
    btnBlank.dispose();
    Riiablo.assets.unload(buysellDescriptor.fileName);
    Riiablo.assets.unload(buyselltabsDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(buysell, getX(), getY());
    super.draw(batch, a);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
      Riiablo.game.setRightPanel(Riiablo.game.inventoryPanel);
    }
  }

  private static class Tab extends WidgetGroup {
    static final int ACTIVE   = 0;
    static final int INACTIVE = 1;

    final TextureRegion[] modes;
    int mode;

    public Tab(TextureRegion active, TextureRegion inactive) {
      setSize(active.getRegionWidth(), active.getRegionHeight());
      modes = new TextureRegion[2];
      modes[ACTIVE]   = active;
      modes[INACTIVE] = inactive;
      mode = INACTIVE;
    }

    public void setMode(int mode) {
      this.mode = mode;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      batch.draw(modes[mode], getX(), getY());
      super.draw(batch, parentAlpha);
    }
  }
}
