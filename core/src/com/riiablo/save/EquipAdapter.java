package com.riiablo.save;

import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;

public class EquipAdapter implements ItemData.EquipListener {
  @Override public void onChanged(ItemData items, BodyLoc bodyLoc, Item oldItem, Item item) {}
  @Override public void onAlternated(ItemData items, int alternate, Item LH, Item RH) {}
}
