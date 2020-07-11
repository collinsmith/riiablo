package com.riiablo.item;

import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.Weapons;

public class ItemGenerator extends PassiveSystem {
//  public Item generate(TreasureClass tc) {
//    return null;
//  }

  public Item generate(String code) {
    return generate(Item.findBase(code));
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
    item.reset();
    item.location = Location.STORED; // FIXME: should allow null?
    if (base.compactsave) item.flags |= Item.COMPACT;

    item.base = base;
    item.code = base.code;

    item.typeEntry = Riiablo.files.ItemTypes.get(item.base.type);
    item.type2Entry = Riiablo.files.ItemTypes.get(item.base.type2);
    item.type = Type.get(item.typeEntry, item.type2Entry);

    if (base.code.equalsIgnoreCase("ear")) {
      item.flags |= Item.EAR;
      // TODO: creating an 'ear' requires the below as params
//    item.qualityId     = 0;  // class
//    item.qualityData   = 0;  // level
//    item.inscription   = ""; // name
      throw new IllegalArgumentException("No support for 'ear'");
    }

    if (base.stackable) {
      // TODO: spawnstack ± ?
      int quantity = base.spawnstack;
      item.props.base.put(Stat.quantity, quantity);
    }

    return item;
  }
}
