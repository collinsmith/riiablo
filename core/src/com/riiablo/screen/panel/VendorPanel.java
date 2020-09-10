package com.riiablo.screen.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.Riiablo;
import com.riiablo.attributes.Stat;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.graphics.BlendMode;
import com.riiablo.item.Item;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;
import com.riiablo.widget.LabelButton;

public class VendorPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "VendorPanel";

  public static final int BUY        = 1 << 0;
  public static final int SELL       = 1 << 1;
  public static final int REPAIR     = 1 << 2;
  public static final int REPAIR_ALL = 1 << 4;
  public static final int EXIT       = 1 << 5;

  public static final int TAB_ARMOR    = 0;
  public static final int TAB_WEAPONS  = 1;
  public static final int TAB_WEAPONS2 = 2;
  public static final int TAB_MISC     = 3;

  public static final int BUYSELL  = BUY | SELL;
  public static final int REPAIRER = REPAIR | REPAIR_ALL;
  public static final int TRADER   = BUYSELL | EXIT;
  public static final int SMITHY   = BUYSELL | REPAIRER;
  public static final int GAMBLER  = TRADER;

  static final int BLANK_MASKS[] = {
      BUY,
      SELL,
      REPAIR,
      EXIT | REPAIR_ALL
  };

  final AssetDescriptor<DC6> buysellDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysell.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion buysell;

  final AssetDescriptor<DC6> buyselltabsDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buyselltabs.DC6", DC6.class);
  DC buyselltabs;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnBuy;
  Button btnSell;
  Button btnRepair;
  Button btnRepairAll;
  Button btnExit;
  Button btnBlank[];
  ButtonGroup<Button> buttonGroup;

  Tab[] tabs;

  final Inventory.Entry inventory;
  VendorGrid activeGrid = null;

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
      tab.label = label;
      label.setPosition(tab.getX(), tab.getY());
      label.setSize(tab.getWidth(), tab.getHeight());
      label.setUserObject(tab);
      label.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          setTab((Tab) label.getUserObject());
        }
      });
      addActor(label);
    }

    final float[] X = {116, 168, 220, 272};
    Button.ButtonStyle blankButtonStyle = new Button.ButtonStyle() {{
      up = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(0));
    }};
    btnBlank = new Button[4];
    for (int i = 0; i < btnBlank.length; i++) {
      Button button = btnBlank[i] = new Button(blankButtonStyle);
      button.setDisabledBlendMode(BlendMode.NONE, Riiablo.colors.white);
      button.setDisabled(true);
      button.setPosition(X[i], 15);
      button.setVisible(false);
      addActor(button);
    }

    btnBuy = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(2));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(3));
      checked = down;
    }});
    btnBuy.setPosition(btnBlank[0].getX(), btnBlank[0].getY());
    btnBuy.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (btnBuy.isChecked()) {
          Riiablo.cursor.setCursor(Riiablo.cursor.buysell, 3);
        } else {
          Riiablo.cursor.resetCursor();
        }
      }
    });
    btnBuy.setVisible(false);
    addActor(btnBuy);

    btnSell = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(4));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(5));
      checked = down;
    }});
    btnSell.setPosition(btnBlank[1].getX(), btnBlank[1].getY());
    btnSell.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (btnSell.isChecked()) {
          Riiablo.cursor.setCursor(Riiablo.cursor.buysell, 4);
        } else {
          Riiablo.cursor.resetCursor();
        }
      }
    });
    btnSell.setVisible(false);
    addActor(btnSell);

    btnRepair = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(6));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(7));
      checked = down;
    }});
    btnRepair.setPosition(btnBlank[2].getX(), btnBlank[2].getY());
    btnRepair.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (btnRepair.isChecked()) {
          Riiablo.cursor.setCursor(Riiablo.cursor.buysell, 1);
        } else {
          Riiablo.cursor.resetCursor();
        }
      }
    });
    btnRepair.setVisible(false);
    addActor(btnRepair);

    btnRepairAll = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(18));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(19));
    }});
    btnRepairAll.setPosition(btnBlank[3].getX(), btnBlank[3].getY());
    btnRepairAll.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {

      }
    });
    btnRepairAll.setVisible(false);
    addActor(btnRepairAll);

    Riiablo.assets.load(buysellbtnDescriptor);
    Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
    btnExit = new Button(new Button.ButtonStyle() {{
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(btnBlank[3].getX(), btnBlank[3].getY());
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    btnExit.setVisible(false);
    addActor(btnExit);

    buttonGroup = new ButtonGroup<>();
    buttonGroup.setMinCheckCount(0);
    buttonGroup.add(btnBuy, btnSell, btnRepair);

    Label goldbankLabel = Label.i18n("stash", Riiablo.fonts.font16);
    goldbankLabel.setSize(180, 16);
    goldbankLabel.setPosition(20, 57);
    addActor(goldbankLabel);

    Label goldbank = new Label(Integer.toString(Riiablo.charData.getStats().get(Stat.goldbank).asInt()), Riiablo.fonts.font16);
    goldbank.setSize(goldbankLabel.getWidth(), goldbankLabel.getHeight());
    goldbank.setPosition(goldbankLabel.getX(), goldbankLabel.getY());
    goldbank.setAlignment(Align.right);
    addActor(goldbank);

    inventory = Riiablo.files.inventory.get("Monster");
    for (int i = 0; i < tabs.length; i++) {
      VendorGrid grid = tabs[i].grid = new VendorGrid(inventory, null);
      grid.setPosition(
          inventory.gridLeft - inventory.invLeft,
          getHeight() - inventory.gridTop - grid.getHeight());
      grid.setVisible(false);
      addActor(grid);
    }

    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnBuy.dispose();
    btnSell.dispose();
    btnRepair.dispose();
    btnRepairAll.dispose();
    btnExit.dispose();
    for (int i = 0; i < btnBlank.length; i++) btnBlank[i].dispose();
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
    } else {
      if (buttonGroup != null && buttonGroup.getCheckedIndex() >= 0) {
        Riiablo.cursor.resetCursor();
      }
    }
  }

  public void config(int flags, Array<Item> items) {
    buttonGroup.uncheckAll();
    btnBuy.setVisible((flags & BUY) == BUY);
    btnSell.setVisible((flags & SELL) == SELL);
    btnRepair.setVisible((flags & REPAIR) == REPAIR);
    btnRepairAll.setVisible((flags & REPAIR_ALL) == REPAIR_ALL);
    btnExit.setVisible((flags & EXIT) == EXIT);
    for (int i = 0; i < btnBlank.length; i++) {
      btnBlank[i].setVisible((flags & BLANK_MASKS[i]) == 0);
    }

    // TODO: supply cleaner API grid.drain(items, "misc") or similar
    Array<Item> tmp = new Array<>(true, items.size, Item.class);
    try {
      int count;
      collect(items, tmp, "armo");
      count = tabs[TAB_ARMOR].grid.drain(tmp);
      tabs[TAB_ARMOR].setVisible(count > 0);
      Gdx.app.debug(TAG, "Dropping " + tmp);

      collect(items, tmp, "weap");
      count = tabs[TAB_WEAPONS].grid.drain(tmp);
      tabs[TAB_WEAPONS].setVisible(count > 0);

      count = tabs[TAB_WEAPONS2].grid.drain(tmp);
      tabs[TAB_WEAPONS2].setVisible(count > 0);
      Gdx.app.debug(TAG, "Dropping " + tmp);

      collect(items, tmp, "misc");
      count = tabs[TAB_MISC].grid.drain(tmp);
      tabs[TAB_MISC].setVisible(count > 0);
      Gdx.app.debug(TAG, "Dropping " + tmp);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    setTab(TAB_MISC);
  }

  private static Array<Item> collect(Array<Item> items, Array<Item> to, String page) {
    to.clear();
    for (Item item : items) {
      if (item.typeEntry.StorePage.equalsIgnoreCase(page)) {
        to.add(item);
      }
    }
    return to;
  }

  void setTab(Tab tab) {
    for (Tab t : tabs) t.setMode(Tab.INACTIVE);
    tab.setMode(Tab.ACTIVE);
    if (activeGrid != null) activeGrid.setVisible(false);
    activeGrid = tab.grid;
    activeGrid.setVisible(true);
  }

  public void setTab(int i) {
    setTab(tabs[i]);
  }

  private class Tab extends WidgetGroup {
    static final int ACTIVE   = 0;
    static final int INACTIVE = 1;

    final TextureRegion[] modes;
    int mode;

    Label label;
    VendorGrid grid;

    public Tab(TextureRegion active, TextureRegion inactive) {
      setSize(active.getRegionWidth(), active.getRegionHeight());
      modes = new TextureRegion[2];
      modes[ACTIVE]   = active;
      modes[INACTIVE] = inactive;
      mode = INACTIVE;
    }

    @Override
    public void setVisible(boolean visible) {
      super.setVisible(visible);
      label.setVisible(visible);
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
