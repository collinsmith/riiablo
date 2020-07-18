package com.riiablo.item.item4;

import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.item.TreasureClass;

public class ItemGenerator {
  public Item generate(TreasureClass tc) {
    return null;
  }

  public Item generate(ItemEntry base) {
    if (base instanceof Armor.Entry) {
      return generate((Armor.Entry) base);
    } else if (base instanceof Weapons.Entry) {
      return generate((Weapons.Entry) base);
    } else if (base instanceof Misc.Entry) {
      return generate((Misc.Entry) base);
    }

    throw new AssertionError();
  }

  public Item generate(Armor.Entry base) {
    throw new UnsupportedOperationException();
  }

  public Item generate(Weapons.Entry base) {
    throw new UnsupportedOperationException();
  }

  public Item generate(final Misc.Entry base) {
    Item item = new Item();
    item.version = Item.VERSION_110e;
    item.flags = 0;
    if (base.compactsave) item.flags |= Item.ITEMFLAG_COMPACT;

    item.setBase(base.code);
    if (base.code.equalsIgnoreCase("ear")) {
      item.flags |= Item.ITEMFLAG_BODYPART;
      item.socketsFilled = 0;
      item.qualityId     = 0; // class
      item.qualityData   = 0; // level
      item.inscription   = ""; // name
    }

    item.socketsFilled = 0;


    return item;
  }
}
