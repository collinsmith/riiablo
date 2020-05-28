package com.riiablo.screen.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Inventory;
import com.riiablo.item.Item;
import com.riiablo.item.StoreLoc;
import com.riiablo.loader.DC6Loader;
import com.riiablo.save.ItemData;
import com.riiablo.widget.Button;

public class CubePanel extends WidgetGroup implements Disposable, ItemGrid.GridListener {
  private static final String TAG = "CubePanel";

  final AssetDescriptor<DC6> supertransmogrifierDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\supertransmogrifier.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion supertransmogrifier;

  final AssetDescriptor<DC6> miniconvertDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\miniconvert.DC6", DC6.class);
  Button btnTransmog;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  final Inventory.Entry inventory;

  public CubePanel() {
    Riiablo.assets.load(supertransmogrifierDescriptor);
    Riiablo.assets.finishLoadingAsset(supertransmogrifierDescriptor);
    supertransmogrifier = Riiablo.assets.get(supertransmogrifierDescriptor).getTexture();
    setSize(supertransmogrifier.getRegionWidth(), supertransmogrifier.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    btnExit = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(buysellbtnDescriptor);
      Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(275, 15);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    btnTransmog = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(miniconvertDescriptor);
      Riiablo.assets.finishLoadingAsset(miniconvertDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(miniconvertDescriptor).getTexture(0));
      down = new TextureRegionDrawable(Riiablo.assets.get(miniconvertDescriptor).getTexture(1));
    }});
    btnTransmog.setPosition(144, 139);
    btnTransmog.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        //...
      }
    });
    addActor(btnTransmog);

    inventory = Riiablo.files.inventory.get("Transmogrify Box Page 1");
    final ItemData itemData = Riiablo.charData.getItems();

    ItemGrid grid = new ItemGrid(inventory, this);
    IntArray cubeItems = itemData.getStore(StoreLoc.CUBE);
    Array<Item> items = itemData.toItemArray(cubeItems);
    grid.populate(items);
    grid.setPosition(
        inventory.gridLeft - inventory.invLeft,
        getHeight() - inventory.gridTop - grid.getHeight());
    addActor(grid);

    //setDebug(true, true);
  }

  @Override
  public void dispose() {
    btnExit.dispose();
    Riiablo.assets.unload(supertransmogrifierDescriptor.fileName);
    Riiablo.assets.unload(miniconvertDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(supertransmogrifier, getX(), getY());
    super.draw(batch, a);
  }

  @Override
  public void onDrop(int x, int y) {
    Riiablo.charData.cursorToStore(StoreLoc.CUBE, x, y);
  }

  @Override
  public void onPickup(int i) {
    Riiablo.charData.storeToCursor(i);
  }

  @Override
  public void onSwap(int i, int x, int y) {
    Riiablo.charData.swapStoreItem(i, StoreLoc.CUBE, x, y);
  }
}
