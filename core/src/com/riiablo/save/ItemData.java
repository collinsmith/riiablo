package com.riiablo.save;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.CharStats;
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
  CharStats.Entry charStats;

  final Array<Item> itemData = new Array<>(Item.class);

  int cursor = INVALID_ITEM;

  int alternate = D2S.PRIMARY;
  final Array<AlternateListener> alternateListeners = new Array<>(false, 16);

  final Array<StoreListener> storeListeners = new Array<>(false, 16);
  final Array<LocationListener> locationListeners = new Array<>(false, 16);

  final EnumIntMap<BodyLoc>  equipped = new EnumIntMap<>(BodyLoc.class, INVALID_ITEM);
  final Array<EquipListener> equipListeners = new Array<>(false, 16);

  final IntIntMap equippedSets = new IntIntMap(); // Indexed using set id
  final IntIntMap setItemsOwned = new IntIntMap(); // Indexed using set item id

  final Array<UpdateListener> updateListeners = new Array<>(false, 16);

  ItemData(Attributes stats, CharStats.Entry charStats) {
    this.stats = stats;
    this.charStats = charStats;
  }

  public void clear() {
    cursor = INVALID_ITEM;
    charStats = null;
    alternate = D2S.PRIMARY;
    alternateListeners.clear();
    itemData.clear();
    equipped.clear();
    equipListeners.clear();
    equippedSets.clear();
    setItemsOwned.clear();
    updateListeners.clear();
  }

  public void load() {
    Item[] items = itemData.items;
    for (int i = 0, s = itemData.size; i < s; i++) {
      items[i].load();
    }
  }

  void preprocessItems() {
    cursor = ItemData.INVALID_ITEM;
    Item[] items = itemData.items;
    for (int i = 0, s = itemData.size; i < s; i++) {
      Item item = items[i];
      if (item.quality == Quality.SET) {
        setItemsOwned.getAndIncrement(item.qualityId, 0, 1);
        if (item.location == Location.EQUIPPED && isActive(item)) {
          updateSet(item, 1);
        }
      }
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
    }

    updateStats();
  }

  public Array<Item> getItems() {
    return itemData;
  }

  public int indexOf(Item item) {
    return itemData.indexOf(item, true);
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

  public int getAlternate() {
    return alternate;
  }

  public void setAlternate(int alternate) {
    if (this.alternate != alternate) {
      this.alternate = alternate;
      updateStats();
      Item LH = getEquipped(BodyLoc.LARM);
      Item RH = getEquipped(BodyLoc.RARM);
      notifyAlternated(alternate, LH, RH);
    }
  }

  public int alternate() {
    int alt = alternate > D2S.PRIMARY ? D2S.PRIMARY : D2S.SECONDARY;
    setAlternate(alt);
    return alt;
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

  public Array<Item> toItemArray(IntArray items) {
    Array<Item> copy = new Array<>(false, items.size, Item.class);
    int[] cache = items.items;
    for (int i = 0, s = items.size, j; i < s; i++) {
      j = cache[i];
      copy.add(itemData.get(j));
    }
    return copy;
  }

  void pickup(int i) {
    assert cursor == INVALID_ITEM;
    Item item = itemData.get(i);
    if (item.location == Location.STORED) notifyStoreRemoved(item);
    cursor = i;
    setLocation(item, Location.CURSOR);
  }

  void storeCursor(StoreLoc storeLoc, int x, int y) {
    assert cursor != ItemData.INVALID_ITEM;
    store(storeLoc, cursor, x, y);
    cursor = INVALID_ITEM;
  }

  void store(StoreLoc storeLoc, int i, int x, int y) {
    Item item = itemData.get(i);
    setLocation(item, Location.STORED);
    item.storeLoc = storeLoc;
    item.gridX = (byte) x;
    item.gridY = (byte) y;
    notifyStoreAdded(item);
  }

  void equip(BodyLoc bodyLoc, Item item) {
    assert !itemData.contains(item, true);
    equip(bodyLoc, add(item));
  }

  void equip(BodyLoc bodyLoc, int i) {
    Item item = itemData.get(i);
    setLocation(item, Location.EQUIPPED);
    item.bodyLoc = bodyLoc;
    int j = equipped.put(bodyLoc, i);
    assert j == INVALID_ITEM : "Item " + j + " should have been unequipped by this point.";
    updateStats(); // TODO: add support for appending to existing stats if this is an additional item
    updateSet(item, 1);
    notifyEquip(bodyLoc, item);
  }

  int unequip(BodyLoc bodyLoc) {
    int i = equipped.remove(bodyLoc);
    Item item = itemData.get(i);
    updateStats();
    updateSet(item, -1);
    notifyUnequip(bodyLoc, item);
    return i;
  }

  void updateStats() {
    Stat stat;
    stats.reset();
    int[] cache = equipped.values();
    for (int i = 0, s = cache.length, j; i < s; i++) {
      j = cache[i];
      if (j == ItemData.INVALID_ITEM) continue;
      Item item = itemData.get(j);
      if (isActive(item)) {
        item.update(stats, charStats, equippedSets);
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
        item.update(stats, charStats, equippedSets);
        stats.add(item.props.remaining());
      } else if (item.type.is(Type.BOOK)) { // TODO: may not be needed since not stat -- calculate elsewhere?
        item.update(stats, charStats, equippedSets);
      }
    }
    stats.update(stats, charStats);
    notifyUpdated();
  }

  private void updateSet(Item item, int add) {
    if (item != null && item.quality == Quality.SET) {
      SetItems.Entry setItem = (SetItems.Entry) item.qualityData;
      int id = Riiablo.files.Sets.index(setItem.set);
      equippedSets.getAndIncrement(id, 0, add);
    }
  }

  public IntIntMap getEquippedSets() {
    return equippedSets;
  }

  public int getOwnedSetCount(int setId) {
    return setItemsOwned.get(setId, 0);
  }

  public boolean addEquipListener(EquipListener l) {
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

  public boolean addAlternateListener(AlternateListener l) {
    alternateListeners.add(l);
    return true;
  }

  private void notifyAlternated(int alternate, Item LH, Item RH) {
    for (AlternateListener l : alternateListeners) l.onAlternated(this, alternate, LH, RH);
  }

  public interface AlternateListener {
    void onAlternated(ItemData items, int alternate, Item LH, Item RH);
  }

  public boolean addUpdateListener(UpdateListener l) {
    updateListeners.add(l);
    return true;
  }

  private void notifyUpdated() {
    for (UpdateListener l : updateListeners) l.onUpdated(this);
  }

  public interface UpdateListener {
    void onUpdated(ItemData itemData);
  }

  public boolean addStoreListener(StoreListener l) {
    storeListeners.add(l);
    return true;
  }

  private void notifyStoreAdded(Item item) {
    for (StoreListener l : storeListeners) l.onAdded(this, item.storeLoc, item);
  }

  private void notifyStoreRemoved(Item item) {
    for (StoreListener l : storeListeners) l.onRemoved(this, item.storeLoc, item);
  }

  public interface StoreListener {
    void onAdded(ItemData items, StoreLoc storeLoc, Item item);
    void onRemoved(ItemData items, StoreLoc storeLoc, Item item);
  }

  void setLocation(Item item, Location location) {
    if (item.location != location) {
      Location oldLocation = item.location;
      item.location = location;
      notifyLocationChanged(item, oldLocation);
    }
  }

  public boolean addLocationListener(LocationListener l) {
    locationListeners.add(l);
    return true;
  }

  private void notifyLocationChanged(Item item, Location oldLocation) {
    for (LocationListener l : locationListeners) l.onChanged(this, oldLocation, item.location, item);
  }

  public interface LocationListener {
    void onChanged(ItemData items, Location oldLocation, Location location, Item item);
  }
}
