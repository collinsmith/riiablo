package com.riiablo.screen.panel;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.excel.Inventory;
import com.riiablo.item.Item;

public class VendorGrid extends ItemGrid {
  private static final String TAG = "VendorGrid";

  protected static final Comparator<Item> ITEM_CODE_COMPARATOR = new Comparator<Item>() {
    @Override
    public int compare(Item i1, Item i2) {
      return Integer.compare(i1.getBaseIndex(), i2.getBaseIndex());
    }
  };

  public VendorGrid(Inventory.Entry inv, GridListener gridListener) {
    super(inv, gridListener);
    showFill = false;
  }

  @Override
  protected boolean accept(Item item) {
    return false;
  }

  public void sort(Array<Item> items) {
    items.sort(ITEM_CODE_COMPARATOR);
    Array<Item> placed = new Array<>(false, items.size, Item.class);
    int x = 0, y = 0;
    for (Item item : items) {
      if (item.base.multibuy) {
        placeMultibuyItem(x, y, item, placed);
      } else {
        placeItem(x, y, item, placed);
//        x = item.gridX;
//        y = item.gridY;
      }
      Gdx.app.debug(TAG, "Putting " + item.code + " at " + x + "," + y + "; multibuy: " + item.base.multibuy);
    }
  }

  // y isn't used -- just for posterity in case it will be needed
  private void placeItem(int x, int y, Item item, Array<Item> placed) {
    boolean contains = false;
    final Item[] items = placed.items;
TopLevelSort:
    for (; x < width; x++) {
      for (y = 0; y < height; y++) {
        for (int i = 0, s = placed.size; i < s; i++) {
          Item placedItem = items[i];
          if (contains = placedItem.contains(x, y)) {
            y += (placedItem.base.invheight - 1);
            break;
          }
        }

        if (!contains) break TopLevelSort;
      }
    }

    item.gridX = (byte) x;
    item.gridY = (byte) y;
    placed.add(item);
  }

  private void placeMultibuyItem(int x, int y, Item item, Array<Item> placed) {
    boolean contains = false;
    final Item[] items = placed.items;
TopLevelSort:
    for (x = width - 1; x >= 0; x--) {
      for (y = 0; y < height; y++) {
        for (int i = 0, s = placed.size; i < s; i++) {
          Item placedItem = items[i];
          if (contains = placedItem.contains(x, y)) {
            y += (placedItem.base.invheight - 1);
            break;
          }
        }

        if (!contains) break TopLevelSort;
      }
    }

    item.gridX = (byte) x;
    item.gridY = (byte) y;
    placed.add(item);
  }
}
