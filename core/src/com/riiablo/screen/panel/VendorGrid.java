package com.riiablo.screen.panel;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
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

  public static Array<Item> sort(Array<Item> items) {
    items.sort(ITEM_CODE_COMPARATOR);
    return items;
  }

  public int drain(Array<Item> items) {
    sort(items);
    Gdx.app.debug(TAG, "Draining " + items);
    GridPoint2 coords = new GridPoint2(0, 0);
    Array<Item> placedItems = new Array<>(true, items.size, Item.class);
    for (Array.ArrayIterator<Item> it = new Array.ArrayIterator<>(items); it.hasNext(); ) {
      Item item = it.next();
      coords.set(0, 0); // TODO: handle non-zero when switching below state
      boolean placed = item.base.multibuy
          ? placeMultibuyItem(coords, item, placedItems)
          : placeItem(coords, item, placedItems);
      if (placed) {
        item.gridX = (byte) coords.x;
        item.gridY = (byte) coords.y;
        it.remove();
        addItem(item);
        placedItems.add(item);
        Gdx.app.debug(TAG, "Placing " + item.code + " @ " + coords);
      } else {
        Gdx.app.debug(TAG, "Draining " + item.code);
      }
    }

    return placedItems.size;
  }

  // y isn't used -- just for posterity in case it will be needed
  private boolean placeItem(GridPoint2 coords, Item item, Array<Item> placed) {
    boolean contains = false;
    final Item[] items = placed.items;
    int x = coords.x;
    int y = coords.y;
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

        if (!contains && y + item.base.invheight - 1 < height) break TopLevelSort;
      }
    }

    coords.set(x, y);
    if (contains) return false;
    return x + item.base.invwidth  - 1 < width
        && y + item.base.invheight - 1 < height;
  }

  private boolean placeMultibuyItem(GridPoint2 coords, Item item, Array<Item> placed) {
    boolean contains = false;
    final Item[] items = placed.items;
    int x = coords.x;
    int y = coords.y;
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

        if (!contains && y + item.base.invheight - 1 < height) break TopLevelSort;
      }
    }

    coords.set(x, y);
    if (contains) return false;
    return x + item.base.invwidth  - 1 < width
        && y + item.base.invheight - 1 < height;
  }
}
