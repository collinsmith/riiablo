package com.riiablo.save;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.item.Attributes;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.Location;
import com.riiablo.item.Quality;
import com.riiablo.item.Stat;
import com.riiablo.item.StoreLoc;
import com.riiablo.item.Type;
import com.riiablo.util.EnumIntMap;

public class ItemData {
  public static final int INVALID_ITEM = -1;

  final Attributes stats;

  final Array<Item> itemData = new Array<>(Item.class);

  int cursor = INVALID_ITEM;
  int alternate = D2S.PRIMARY;

  final EnumIntMap<BodyLoc>  equipped = new EnumIntMap<>(BodyLoc.class, INVALID_ITEM);
  final Array<EquipListener> equipListeners = new Array<>(false, 16);

  final IntIntMap equippedSets = new IntIntMap(); // Indexed using set id
  final IntIntMap setItemsOwned = new IntIntMap(); // Indexed using set item id

  ItemData(Attributes stats) {
    this.stats = stats;
  }

  public void clear() {
    cursor = INVALID_ITEM;
    alternate = D2S.PRIMARY;
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

  public Item getEquipped(BodyLoc bodyLoc) {
    return getEquipped(bodyLoc, alternate);
  }

  public Item getEquipped(BodyLoc bodyLoc, int alternate) {
    return getSlot(BodyLoc.getAlternate(bodyLoc, alternate));
  }

  public boolean isActive(Item item) {
    if (item == null) return false;
    return item.bodyLoc == BodyLoc.getAlternate(item.bodyLoc, alternate);
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

  void equip(BodyLoc bodyLoc, Item item) {
    assert !itemData.contains(item, true);
    equip(bodyLoc, add(item));
  }

  void equip(BodyLoc bodyLoc, int i) {
    Item item = itemData.get(i);
    item.location = Location.EQUIPPED;
    item.bodyLoc = bodyLoc;
    int j = equipped.put(bodyLoc, i);
    assert j == INVALID_ITEM : "Item " + j + " should have been unequipped by this point.";
    update(bodyLoc);
    updateSet(item, 1);
    notifyEquip(bodyLoc, item);
  }

  int unequip(BodyLoc bodyLoc) {
    int i = equipped.remove(bodyLoc);
    Item item = itemData.get(i);
//    update(bodyLoc);
    updateSet(item, -1);
    notifyUnequip(bodyLoc, item);
    return i;
  }

  void update(BodyLoc bodyLoc) {
    int i = equipped.get(bodyLoc);
    update(i);
  }

  void update(int i) {
    Item item = itemData.get(i);
//    if (item != null) item.update(this);
  }

  private void updateStats() {
    Stat stat;
    stats.reset();
    int[] cache = equipped.values();
    for (int i = 0, s = cache.length, j; i < s; i++) {
      j = cache[i];
      if (j == ItemData.INVALID_ITEM) continue;
      Item item = itemData.get(j);
      if (isActive(item)) {
        stats.add(item.props.remaining());
        if ((stat = item.props.get(Stat.armorclass)) != null) {
          stats.aggregate().addCopy(stat);
        }
      }
    }

    IntArray inventoryItems = getStore(StoreLoc.INVENTORY);
    cache = inventoryItems.items;
    for (int i = 0, s = cache.length, j; i < s; i++) {
      j = cache[i];
      if (j == ItemData.INVALID_ITEM) continue;
      Item item = itemData.get(j);
      if (item.type.is(Type.CHAR)) {
        stats.add(item.props.remaining());
      }
    }
//    stats.update(this); // TODO: uncomment
  }

  private void updateSet(Item item, int add) {
    if (item != null && item.quality == Quality.SET) {
      SetItems.Entry setItem = (SetItems.Entry) item.qualityData;
      int id = Riiablo.files.Sets.index(setItem.set);
      equippedSets.getAndIncrement(id, 0, add);
    }
  }

  public boolean addEquipmentListener(EquipListener l) {
    equipListeners.add(l);
    return true;
  }

  private void notifyEquip(BodyLoc bodyLoc, Item item) {
    for (EquipListener l : equipListeners) l.onEquip(this, bodyLoc, item);
  }

  private void notifyUnequip(BodyLoc bodyLoc, Item item) {
    for (EquipListener l : equipListeners) l.onUnequip(this, bodyLoc, item);
  }

  public interface EquipListener {
    void onEquip(ItemData items, BodyLoc bodyLoc, Item item);
    void onUnequip(ItemData items, BodyLoc bodyLoc, Item item);
  }
}
