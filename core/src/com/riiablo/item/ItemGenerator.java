package com.riiablo.item;

import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
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
    return generate(Item.findBase(code));
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

  public static void configureBaseProps(Item item, Attributes attrs) {
    Gdx.app.debug(TAG, "Injecting base stats...");
    PropertyList baseProps = attrs.base;
    baseProps.put(Stat.item_levelreq, item.base.levelreq);
    if (item.base instanceof Weapons.Entry) {
      Weapons.Entry weapon = item.getBase();
      baseProps.put(Stat.mindamage, weapon.mindam);
      baseProps.put(Stat.maxdamage, weapon.maxdam);
      baseProps.put(Stat.secondary_mindamage, weapon._2handmindam);
      baseProps.put(Stat.secondary_maxdamage, weapon._2handmaxdam);
      baseProps.put(Stat.item_throw_mindamage, weapon.minmisdam);
      baseProps.put(Stat.item_throw_maxdamage, weapon.maxmisdam);
      baseProps.put(Stat.reqstr, weapon.reqstr);
      baseProps.put(Stat.reqdex, weapon.reqdex);
    } else if (item.base instanceof Armor.Entry) {
      Armor.Entry armor = item.getBase();
      baseProps.put(Stat.reqstr, armor.reqstr);
      baseProps.put(Stat.reqdex, 0);
      baseProps.put(Stat.toblock, armor.block); // FIXME: apply Riiablo.charData.getCharacterClass().entry().BlockFactor for view stats
      baseProps.put(Stat.mindamage, armor.mindam);
      baseProps.put(Stat.maxdamage, armor.maxdam);
    } else {
      Misc.Entry misc = item.getBase();
    }
  }

  private static void socket(Item item) {
    // TODO: include difficulty
    if (item.base.gemsockets > 0 && MathUtils.randomBoolean(SOCKETED_CHANCE)) {
      Gdx.app.debug(TAG, "Item is socketed");
      item.flags |= Item.SOCKETED;
      int diff = Riiablo.NORMAL;
      int maxSockets = Math.min(item.base.gemsockets, item.typeEntry.MaxSock[diff]);
      int numSockets = MathUtils.random(1, maxSockets);
      Gdx.app.debug(TAG, "Setting sockets to: " + numSockets);
      item.props.base.put(Stat.item_numsockets, numSockets);
      item.sockets = new Array<>(numSockets);
    }
  }

  private static void ethereal(Item item) {
    if (!item.base.nodurability && MathUtils.randomBoolean(ETHEREAL_CHANCE)) {
      Gdx.app.debug(TAG, "Item is ethereal");
      item.flags |= Item.ETHEREAL;
    }
  }

  private static void durability(Item item) {
    if (item.base.nodurability) {
      item.props.base.put(Stat.maxdurability, 0);
    } else {
      // TODO: assign random int up to item.base.durability
    }
  }

  public Item generate(Armor.Entry base) {
    Item item = new Item();
    item.reset();
    item.location = Location.STORED; // FIXME: should allow null?
    if (base.compactsave) item.flags |= Item.COMPACT;

    item.base = base;
    item.code = base.code;

    item.typeEntry = Riiablo.files.ItemTypes.get(item.base.type);
    item.type2Entry = Riiablo.files.ItemTypes.get(item.base.type2);
    item.type = Type.get(item.typeEntry, item.type2Entry);

//    configureBaseProps(item, item.props);
//    socket(item);
//    ethereal(item);
//    durability(item);

    return item;
  }

  public Item generate(Weapons.Entry base) {
    Item item = new Item();
    item.reset();
    item.location = Location.STORED; // FIXME: should allow null?
    if (base.compactsave) item.flags |= Item.COMPACT;

    item.base = base;
    item.code = base.code;

    item.typeEntry = Riiablo.files.ItemTypes.get(item.base.type);
    item.type2Entry = Riiablo.files.ItemTypes.get(item.base.type2);
    item.type = Type.get(item.typeEntry, item.type2Entry);

//    configureBaseProps(item, item.props);
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

//    if (base.stackable) {
//      // TODO: spawnstack ± ?
//      int quantity = base.spawnstack;
//      item.props.base.put(Stat.quantity, quantity);
//    }

    return item;
  }
}
