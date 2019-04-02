package com.riiablo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.codec.D2S;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.Quality;
import com.riiablo.item.StoreLoc;

import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;

public class CharData {
  private D2S d2s;
  private Item cursor;
  private final EnumMap<StoreLoc, Array<Item>> store = new EnumMap<>(StoreLoc.class);
  private final EnumMap<BodyLoc, Item> equipped = new EnumMap<>(BodyLoc.class);
  private final Array<Item> belt = new Array<>(16);
  private final Array<EquippedListener> EQUIPPED_LISTENERS = new Array<>();

  private final IntIntMap equippedSets = new IntIntMap(); // Indexed using set id
  private final IntIntMap setItemsOwned = new IntIntMap(); // Indexed using set item id

  public CharData() {
    for (StoreLoc storeLoc : StoreLoc.values()) store.put(storeLoc, new Array<Item>());
  }

  public D2S getD2S() {
    return d2s;
  }

  public CharData setD2S(D2S d2s) {
    if (this.d2s != d2s) {
      this.d2s = d2s;
    }

    return this;
  }

  public CharData createD2S(String name, CharacterClass charClass) {
    return this;
  }

  public void loadItems() {
    for (Array<Item> array : store.values()) array.clear();
    equipped.clear();
    belt.clear();
    for (Item item : d2s.items.items) {
      addItem(item);
      //item.load();
    }
  }

  private void addItem(Item item) {
    switch (item.location) {
      case BELT:
        belt.add(item);
        break;
      case CURSOR:
        cursor = item;
        break;
      case EQUIPPED:
        setEquipped(item.bodyLoc, item);
        break;
      case STORED:
        store.get(item.storeLoc).add(item);
        break;
    }
    if (item.quality == Quality.SET) {
      setItemsOwned.getAndIncrement(item.qualityId, 0, 1);
    }
  }

  private void removeItem(Item item) {
    // TODO: e.g., item dropped
    if (item.quality == Quality.SET) {
      setItemsOwned.getAndIncrement(item.qualityId, 0, -1);
    }
  }

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

  public int getSkill(int alternate, int button) {
    return d2s.header.actions[alternate][button];
  }

  public int getHotKey(int button, int skill) {
    return ArrayUtils.indexOf(d2s.header.hotkeys, button == Input.Buttons.LEFT
        ? skill | D2S.HOTKEY_LEFT_MASK
        : skill);
  }

  public Item getCursor() {
    return cursor;
  }

  public Item setCursor(Item item) {
    Item oldItem = cursor;
    this.cursor = item;
    return oldItem;
  }

  public Array<Item> getStore(StoreLoc storeLoc) {
    return store.get(storeLoc);
  }

  public Item getEquipped(BodyLoc bodyLoc) {
    return equipped.get(bodyLoc);
  }

  public Item setEquipped(BodyLoc bodyLoc, Item item) {
    Item oldItem = equipped.put(bodyLoc, item);
    updateSets(oldItem, item);
    notifyEquippedChanged(bodyLoc, oldItem, item);
    return oldItem;
  }

  public Array<Item> getBelt() {
    return belt;
  }

  public int getAlternate() {
    return d2s.header.alternate;
  }

  public void setAlternate(int alternate) {
    if (d2s.header.alternate != alternate) {
      d2s.header.alternate = alternate;
      Item LH = getEquipped(alternate > 0 ? BodyLoc.LARM2 : BodyLoc.LARM);
      Item RH = getEquipped(alternate > 0 ? BodyLoc.RARM2 : BodyLoc.RARM);
      notifyEquippedAlternated(LH, RH);
    }
  }

  private void notifyEquippedChanged(BodyLoc bodyLoc, Item oldItem, Item item) {
    for (EquippedListener l : EQUIPPED_LISTENERS) l.onChanged(this, bodyLoc, oldItem, item);
  }

  private void notifyEquippedAlternated(Item LH, Item RH) {
    for (EquippedListener l : EQUIPPED_LISTENERS) l.onAlternated(this, LH, RH);
  }

  public interface EquippedListener {
    void onChanged(CharData client, BodyLoc bodyLoc, Item oldItem, Item item);
    void onAlternated(CharData client, Item LH, Item RH);
  }

  public static class EquippedAdapter implements EquippedListener {
    @Override public void onChanged(CharData client, BodyLoc bodyLoc, Item oldItem, Item item) {}
    @Override public void onAlternated(CharData client, Item LH, Item RH) {}
  }
}