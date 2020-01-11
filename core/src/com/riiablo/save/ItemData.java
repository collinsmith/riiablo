package com.riiablo.save;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.Location;
import com.riiablo.item.Quality;
import com.riiablo.item.StoreLoc;
import com.riiablo.util.EnumIntMap;

public class ItemData {
  public static final int INVALID_ITEM = -1;

  final Array<Item> itemData = new Array<>(Item.class);

  int cursor;

  final EnumIntMap<BodyLoc>  equipped = new EnumIntMap<>(BodyLoc.class, INVALID_ITEM);
  final Array<EquipListener> equipListeners = new Array<>(false, 16);

  final IntIntMap equippedSets = new IntIntMap(); // Indexed using set id
  final IntIntMap setItemsOwned = new IntIntMap(); // Indexed using set item id

  public void clear() {
    cursor = INVALID_ITEM;
    itemData.clear();
    equipped.clear();
    equipListeners.clear();
    equippedSets.clear();
    setItemsOwned.clear();
  }

  void preprocessItems() {
    cursor = ItemData.INVALID_ITEM;
    Item[] items = itemData.items;
    for (int i = 0, s = itemData.size; i < s; i++) {
      Item item = items[i];
      switch (item.location) {
        case EQUIPPED:
          equipped.put(item.bodyLoc, i);
          break;
        case BELT:
          item.gridY = (byte) -(item.gridX >>> 2);
          item.gridX &= 0x3;
          break;
        case CURSOR:
          assert cursor == INVALID_ITEM : "Only one item should be marked as cursor";
          cursor = i;
          break;
        case STORED:
        case UNK3:
        case UNK5:
        case SOCKET:
        default:
      }
      if (item.quality == Quality.SET) setItemsOwned.getAndIncrement(item.qualityId, 0, 1);
    }
  }

  public Item getItem(int i) {
    return itemData.get(i);
  }

  public Item getCursor() {
    return cursor == INVALID_ITEM ? null : getItem(cursor);
  }

  public Item getSlot(BodyLoc bodyLoc) {
    int i = equipped.get(bodyLoc);
    return i == ItemData.INVALID_ITEM ? null : getItem(i);
  }

  public Item getEquipped(BodyLoc bodyLoc, int alternate) {
    return getSlot(BodyLoc.getAlternate(bodyLoc, alternate));
  }

  public int add(Item item) {
    int i = itemData.size;
    itemData.add(item);
    if (item.quality == Quality.SET) setItemsOwned.getAndIncrement(item.qualityId, 0, 1);
    return i;
  }

  public Item remove(int i) {
    Item item = getItem(i);
    itemData.removeIndex(i);
    int[] vals = equipped.values();
    for (int j = 0, s = vals.length; j < s; j++) if (vals[j] > i) vals[j]--;
    if (item.quality == Quality.SET) setItemsOwned.getAndIncrement(item.qualityId, 0, -1);
    return item;
  }

  public void addAll(Array<? extends Item> items) {
    itemData.addAll(items);
  }

  public IntArray getLocation(Location location) {
    return getLocation(location, StoreLoc.NONE);
  }

  public IntArray getLocation(Location location, StoreLoc storeLoc) {
    Item[] items = itemData.items;
    IntArray copy = new IntArray(items.length);
    for (int i = 0, s = itemData.size; i < s; i++) {
      Item item = items[i];
      if (item.location != location) continue;
      if (location == Location.STORED && item.storeLoc != storeLoc) continue;
      copy.add(i);
    }

    return copy;
  }

  public IntArray getStore(StoreLoc storeLoc) {
    return getLocation(Location.STORED, storeLoc);
  }

//  public Item equip(BodyLoc bodyLoc, Item item) {
//    Item oldItem = equipped.put(bodyLoc, item);
////    if (item != null) item.update(this);
//    updateSets(oldItem, item);
//    updateStats();
//    notifyEquipmentChanged(bodyLoc, oldItem, item);
//    return oldItem;
//  }

  private void updateSets(Item oldItem, Item item) {
    if (oldItem != null && oldItem.quality == Quality.SET) {
      SetItems.Entry setItem = (SetItems.Entry) oldItem.qualityData;
      int id = Riiablo.files.Sets.index(setItem.set);
      equippedSets.getAndIncrement(id, 0, -1);
    }
    if (item != null && item.quality == Quality.SET) {
      SetItems.Entry setItem = (SetItems.Entry) item.qualityData;
      int id = Riiablo.files.Sets.index(setItem.set);
      equippedSets.getAndIncrement(id, 0, 1);
    }
  }

  public boolean addEquipmentListener(EquipListener l) {
    equipListeners.add(l);
    return true;
  }

  void notifyEquipmentChanged(BodyLoc bodyLoc, Item oldItem, Item item) {
    for (EquipListener l : equipListeners) l.onChanged(this, bodyLoc, oldItem, item);
  }

  void notifyEquipmentAlternated(int alternate, Item LH, Item RH) {
    for (EquipListener l : equipListeners) l.onAlternated(this, alternate, LH, RH);
  }

  public interface EquipListener {
    void onChanged(ItemData items, BodyLoc bodyLoc, Item oldItem, Item item);
    void onAlternated(ItemData items, int alternate, Item LH, Item RH);
  }
}
