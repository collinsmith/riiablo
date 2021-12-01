package com.riiablo.item;

import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
import com.riiablo.attributes.Stat;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.Misc;
import com.riiablo.codec.excel.Weapons;

public class ItemGenerator extends PassiveSystem {
  private static final String TAG = "ItemGenerator";

  private static final boolean DEBUG = true;

  private static final float SOCKETED_CHANCE = 1 / 3f;
  private static final float ETHEREAL_CHANCE = 1 / 20f;

//  public Item generate(TreasureClass tc) {
//    return null;
//  }

  public Item generate(String code) {
    ItemEntry type = ItemUtils.getBase(code);
    return generate(type);
  }

  public Item generate(ItemEntry base) {
    Gdx.app.debug(TAG, String.format("Generating %s (%s)", base.code, base.name));
    if (base instanceof Armor.Entry) {
      return generate((Armor.Entry) base);
    } else if (base instanceof Weapons.Entry) {
      return generate((Weapons.Entry) base);
    } else if (base instanceof Misc.Entry) {
      return generate((Misc.Entry) base);
    }

    throw new AssertionError();
  }

  private static void socket(Item item) {
    // TODO: include difficulty
    if (item.base.gemsockets > 0 && MathUtils.randomBoolean(SOCKETED_CHANCE)) {
      Gdx.app.debug(TAG, "Item is socketed");
      item.flags |= Item.ITEMFLAG_SOCKETED;
      int diff = Riiablo.NORMAL;
      int maxSockets = Math.min(item.base.gemsockets, item.typeEntry.MaxSock[diff]);
      int numSockets = MathUtils.random(1, maxSockets);
      Gdx.app.debug(TAG, "Setting sockets to: " + numSockets);
      item.attrs.base().put(Stat.item_numsockets, numSockets);
      item.sockets = new Array<>(numSockets);
    }
  }

  private static void ethereal(Item item) {
    if (!item.base.nodurability && MathUtils.randomBoolean(ETHEREAL_CHANCE)) {
      Gdx.app.debug(TAG, "Item is ethereal");
      item.flags |= Item.ITEMFLAG_ETHEREAL;
    }
  }

  private static void durability(Item item) {
    if (item.base.nodurability) {
      item.attrs.base().put(Stat.maxdurability, 0);
    } else {
      // TODO: assign random int up to item.base.durability
    }
  }

  public Item generate(Armor.Entry base) {
    Item item = new Item();
    item.reset();
    item.location = Location.STORED; // FIXME: should allow null?
    if (base.compactsave) item.flags |= Item.ITEMFLAG_COMPACT;

    item.setBase(base);

//    socket(item);
//    ethereal(item);
//    durability(item);

    return item;
  }

  public Item generate(Weapons.Entry base) {
    Item item = new Item();
    item.reset();
    item.location = Location.STORED; // FIXME: should allow null?
    if (base.compactsave) item.flags |= Item.ITEMFLAG_COMPACT;

    item.setBase(base);

//    socket(item);
//    ethereal(item);
//    durability(item);

//    if (base.stackable) {
//      // TODO: spawnstack ± ?
//      int quantity = base.spawnstack;
//      item.props.base.put(Stat.quantity, quantity);
//    }

    return item;
  }

  public Item generate(final Misc.Entry base) {
    Item item = new Item();
    item.reset();
    item.location = Location.STORED; // FIXME: should allow null?
    if (base.compactsave) item.flags |= Item.ITEMFLAG_COMPACT;

    if (base.code.equalsIgnoreCase("ear")) {
      item.setEar(0, 0, "null"); // TODO: creating an 'ear' requires the following params
      throw new IllegalArgumentException("No support for 'ear'");
    } else {
      item.setBase(base);
    }

//    if (base.stackable) {
//      // TODO: spawnstack ± ?
//      int quantity = base.spawnstack;
//      item.props.base.put(Stat.quantity, quantity);
//    }

    return item;
  }
}
