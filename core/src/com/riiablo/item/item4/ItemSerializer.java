package com.riiablo.item.item4;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.util.BitStream;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Location;
import com.riiablo.item.PropertyList;
import com.riiablo.item.Quality;
import com.riiablo.item.Stat;
import com.riiablo.item.StoreLoc;
import com.riiablo.item.Type;

public class ItemSerializer {
  private static final String TAG = "ItemSerializer";

  private static final boolean DEBUG = true;

  public Item read(BitStream bitStream) {
    Item item = new Item();
    item.reset();
    item.flags    = bitStream.read32BitsOrLess(Integer.SIZE);
    item.version  = bitStream.readUnsigned8OrLess(Byte.SIZE);
    bitStream.skip(2); // Unknown use -- safe to skip
    item.location = Location.valueOf(bitStream.readUnsigned7OrLess(3));
    item.bodyLoc  = BodyLoc.valueOf(bitStream.readUnsigned7OrLess(4));
    item.gridX    = bitStream.readUnsigned7OrLess(4);
    item.gridY    = bitStream.readUnsigned7OrLess(4);
    item.storeLoc = StoreLoc.valueOf(bitStream.readUnsigned7OrLess(3));

    if ((item.flags & Item.ITEMFLAG_BODYPART) == Item.ITEMFLAG_BODYPART) {
      int    charClass = bitStream.readUnsigned7OrLess(3);
      int    charLevel = bitStream.readUnsigned7OrLess(7);
      String charName  = bitStream.readString2(Riiablo.MAX_NAME_LENGTH + 1, 7);
      item.setEar(charClass, charLevel, charName);
    } else {
      item.setBase(bitStream.readString(4).trim());
      item.socketsFilled = bitStream.readUnsigned7OrLess(3);
    }

    if ((item.flags & Item.ITEMFLAG_COMPACT) == Item.ITEMFLAG_COMPACT) {
      readCompact(item);
    } else {
      read(bitStream, item);
    }

    return item;
  }

  private static void readCompact(Item item) {
    if (item.type.is(Type.GEM) || item.type.is(Type.RUNE)) {
      Gems.Entry gem = Riiablo.files.Gems.get(item.code);
      item.stats = ItemUtils.getGemProps(gem);
    } else {
      item.stats = Item.EMPTY_PROPERTY_ARRAY;
    }
  }

  private static void read(BitStream bitStream, Item item) {
    item.id        = bitStream.read32BitsOrLess(Integer.SIZE);
    item.ilvl      = bitStream.readUnsigned7OrLess(7);
    item.quality   = Quality.valueOf(bitStream.readUnsigned7OrLess(4));
    item.pictureId = bitStream.readBoolean() ? bitStream.readUnsigned7OrLess(3)   : Item.NO_PICTURE_ID;
    item.classOnly = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(11) : Item.NO_CLASS_ONLY;
    readQualityData(bitStream, item);

    int listFlags = Item.MAGIC_PROPS_FLAG;
    if (readRunewordData(bitStream, item)) listFlags |= Item.RUNE_PROPS_FLAG;

    item.inscription = (item.flags & Item.ITEMFLAG_INSCRIBED) == Item.ITEMFLAG_INSCRIBED
        ? bitStream.readString2(Riiablo.MAX_NAME_LENGTH + 1, 7) : null;

    bitStream.skip(1); // TODO: Unknown, this usually is 0, but is 1 on a Tome of Identify.  (It's still 0 on a Tome of Townportal.)

    readArmorClass(bitStream, item);
    readDurability(bitStream, item);
    readSockets(bitStream, item);
    readBook(bitStream, item);
    readQuantity(bitStream, item);

    if (item.type.is(Type.BOOK)) listFlags = 0;
    listFlags |= (readSetFlags(bitStream, item) << Item.SET_PROPS);
    PropertyList[] props = item.stats = new PropertyList[Item.NUM_PROPS];
    for (int i = 0; i < Item.NUM_PROPS; i++) {
      if (((listFlags >> i) & 1) == 1) {
        props[i] = PropertyList.obtain().read(bitStream);
      }
    }
  }

  private static boolean readQualityData(BitStream bitStream, Item item) {
    switch (item.quality) {
      case LOW:
      case HIGH:
        item.qualityId = bitStream.readUnsigned31OrLess(3);
        return true;

      case NORMAL:
        item.qualityId = 0;
        return true;

      case SET:
        item.qualityId = bitStream.readUnsigned31OrLess(Item.SET_ID_SIZE);
        item.qualityData = Riiablo.files.SetItems.get(item.qualityId);
        if (item.qualityId == (1 << Item.SET_ID_SIZE) - 1) {
          Gdx.app.error(TAG, String.format("Unknown set id: 0x%03x", item.qualityId));
        }
        return true;

      case UNIQUE:
        item.qualityId = bitStream.readUnsigned31OrLess(Item.UNIQUE_ID_SIZE);
        item.qualityData = Riiablo.files.UniqueItems.get(item.qualityId);
        if (item.qualityId == (1 << Item.UNIQUE_ID_SIZE) - 1) {
          Gdx.app.error(TAG, String.format("Unknown unique id: 0x%03x", item.qualityId));
        }
        return true;

      case MAGIC:
        item.qualityId = bitStream.readUnsigned31OrLess(2 * Item.MAGIC_AFFIX_SIZE); // 11 for prefix, 11 for suffix
        return true;

      case RARE:
      case CRAFTED:
        item.qualityId = bitStream.readUnsigned31OrLess(2 * Item.RARE_AFFIX_SIZE); // 8 for prefix, 8 for suffix
        item.qualityData = new RareQualityData(bitStream);
        return true;

      default:
        item.qualityId = 0;
        return false;
    }
  }

  private static int readSetFlags(BitStream bitStream, Item item) {
    return item.quality == Quality.SET ? bitStream.readUnsigned7OrLess(5) : 0;
  }

  private static boolean readRunewordData(BitStream bitStream, Item item) {
    boolean hasRunewordData = (item.flags & Item.ITEMFLAG_RUNEWORD) == Item.ITEMFLAG_RUNEWORD;
    item.runewordData = hasRunewordData ? bitStream.read16BitsOrLess(Short.SIZE) : 0;
    return hasRunewordData;
  }

  private static boolean readArmorClass(BitStream bitStream, Item item) {
    boolean hasAC = item.type.is(Type.ARMO);
    if (hasAC) item.attrs.base().read(Stat.armorclass, bitStream);
    return hasAC;
  }

  private static boolean readDurability(BitStream bitStream, Item item) {
    boolean hasDurability = item.type.is(Type.ARMO) || item.type.is(Type.WEAP);
    if (hasDurability) {
      int maxdurability = item.attrs.base().read(Stat.maxdurability, bitStream);
      if (maxdurability > 0) item.attrs.base().read(Stat.durability, bitStream);
    }
    return hasDurability;
  }

  private static boolean readSockets(BitStream bitStream, Item item) {
    boolean hasSockets = (item.flags & Item.ITEMFLAG_SOCKETED) == Item.ITEMFLAG_SOCKETED
        && (item.type.is(Type.ARMO) || item.type.is(Type.WEAP));
    if (hasSockets) {
      int item_numsockets = item.attrs.base().read(Stat.item_numsockets, bitStream);
      item.sockets = new Array<>(item_numsockets);
    }
    return hasSockets;
  }

  private static boolean readBook(BitStream bitStream, Item item) {
    boolean isBook = item.type.is(Type.BOOK);
    if (isBook) bitStream.skip(5); // TODO: Appears to be 0 for tbk and 1 for ibk
    return isBook;
  }

  private static boolean readQuantity(BitStream bitStream, Item item) {
    boolean hasQuantity = item.base.stackable;
    if (hasQuantity) {
      int quantity = bitStream.readUnsigned15OrLess(9);
      item.attrs.base().put(Stat.quantity, quantity);
    }
    return hasQuantity;
  }
}
