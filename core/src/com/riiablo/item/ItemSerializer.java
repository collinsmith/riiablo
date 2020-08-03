package com.riiablo.item;

import java.util.Arrays;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.util.BitStream;
import com.riiablo.log.Log;
import com.riiablo.log.LogManager;
import com.riiablo.save.InvalidFormat;
import com.riiablo.util.DebugUtils;

public class ItemSerializer {
  private static final Logger log = LogManager.getLogger(ItemSerializer.class);

  private static final byte[] SIGNATURE = {0x4A, 0x4D};

  private static boolean readSignature(BitStream bitStream) {
    log.trace("Validating item signature");
    byte[] signature = bitStream.readFully(SIGNATURE.length);
    boolean matched = Arrays.equals(signature, SIGNATURE);
    if (!matched) {
      throw new InvalidFormat(
          String.format("Item signature doesn't match expected signature: %s, expected %s",
          DebugUtils.toByteArray(signature),
          DebugUtils.toByteArray(SIGNATURE)));
    }
    return matched;
  }

  public Item readItem(BitStream bitStream) {
    Item item = readSingleItem(bitStream);
    if (item.socketsFilled > 0) log.trace("Reading {} sockets...", item.socketsFilled);
    for (int i = 0; i < item.socketsFilled; i++) {
      try {
        Log.put("socket", String.valueOf(i));
        bitStream.alignToByte();
        item.sockets.add(readSingleItem(bitStream));
      } finally {
        Log.remove("socket");
      }
    }
    return item;
  }

  public Item readSingleItem(BitStream bitStream) {
    log.trace("Reading item...");
    readSignature(bitStream);
    Item item = new Item();
    item.reset();
    item.flags = (int) bitStream.readRaw(32);
    Log.tracef(log, "flags: 0x%08X [%s]", item.flags, item.getFlagsString());
    item.version = bitStream.readU8(Byte.SIZE);
    log.trace("version: {}", item.version);
    bitStream.skip(2); // Unknown use -- safe to skip
    item.location = Location.valueOf(bitStream.readU7(3));
    item.bodyLoc = BodyLoc.valueOf(bitStream.readU7(4));
    item.gridX = bitStream.readU7(4);
    item.gridY = bitStream.readU7(4);
    item.storeLoc = StoreLoc.valueOf(bitStream.readU7(3));

    if ((item.flags & Item.ITEMFLAG_BODYPART) == Item.ITEMFLAG_BODYPART) {
      int charClass = bitStream.readU7(3);
      int charLevel = bitStream.readU7(7);
      String charName = bitStream.readString2(Riiablo.MAX_NAME_LENGTH + 1, 7);
      item.setEar(charClass, charLevel, charName);
    } else {
      item.setBase(bitStream.readString(4).trim());
      item.socketsFilled = bitStream.readU7(3);
    }

    log.trace("code: {}", item.code);
    if ((item.flags & Item.ITEMFLAG_COMPACT) == Item.ITEMFLAG_COMPACT) {
      readCompact(item);
    } else {
      readStandard(bitStream, item);
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

  private static void readStandard(BitStream bitStream, Item item) {
    item.data = bitStream.getBufferView(); // TODO: remove when serialization implemented
    item.id = (int) bitStream.readRaw(32);
    Log.tracef(log, "id: 0x%08X", item.id);
    item.ilvl = bitStream.readU7(7);
    item.quality = Quality.valueOf(bitStream.readU7(4));
    item.pictureId = bitStream.readBoolean() ? bitStream.readU7(3) : Item.NO_PICTURE_ID;
    item.classOnly = bitStream.readBoolean() ? bitStream.readU15(11) : Item.NO_CLASS_ONLY;
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
    log.trace("quality: {}", item.quality);
    switch (item.quality) {
      case LOW:
      case HIGH:
        item.qualityId = bitStream.readU31(3);
        log.trace("qualityId: {}", item.qualityId);
        return true;

      case NORMAL:
        item.qualityId = 0;
        return true;

      case SET:
        item.qualityId = bitStream.readU31(Item.SET_ID_SIZE);
        log.trace("qualityId: {}", item.qualityId);
        item.qualityData = Riiablo.files.SetItems.get(item.qualityId);
        log.trace("qualityData: {}", item.qualityData);
        if (item.qualityId == (1 << Item.SET_ID_SIZE) - 1) {
          log.error("Unknown set item id: {}", item.qualityId);
          // This is unexpected -- all set items should reference a set id
          // TODO: throw item format exception
        }
        return true;

      case UNIQUE:
        item.qualityId = bitStream.readU31(Item.UNIQUE_ID_SIZE);
        log.trace("qualityId: {}", item.qualityId);
        item.qualityData = Riiablo.files.UniqueItems.get(item.qualityId);
        log.trace("qualityData: {}", item.qualityData);
        if (item.qualityId == (1 << Item.UNIQUE_ID_SIZE) - 1) {
          log.warn("Unknown unique item id: {}", item.qualityId);
          // This is expected for hdm and possibly others
          // TODO: ensure item can be gracefully handled, else throw item format exception
        }
        return true;

      case MAGIC:
        item.qualityId = bitStream.readU31(2 * Item.MAGIC_AFFIX_SIZE); // 11 for prefix, 11 for suffix
        log.trace("qualityId: {}", item.qualityId);
        return true;

      case RARE:
      case CRAFTED:
        item.qualityId = bitStream.readU31(2 * Item.RARE_AFFIX_SIZE); // 8 for prefix, 8 for suffix
        log.trace("qualityId: {}", item.qualityId);
        item.qualityData = new RareQualityData(bitStream);
        log.trace("qualityData: {}", item.qualityData);
        return true;

      default:
        item.qualityId = 0;
        return false;
    }
  }

  private static int readSetFlags(BitStream bitStream, Item item) {
    return item.quality == Quality.SET ? bitStream.readU7(5) : 0;
  }

  private static boolean readRunewordData(BitStream bitStream, Item item) {
    boolean hasRunewordData = (item.flags & Item.ITEMFLAG_RUNEWORD) == Item.ITEMFLAG_RUNEWORD;
    item.runewordData = hasRunewordData ? (short) bitStream.readRaw(16) : 0;
    return hasRunewordData;
  }

  private static boolean readArmorClass(BitStream bitStream, Item item) {
    boolean hasAC = item.type.is(Type.ARMO);
    if (hasAC) item.props.base().read(Stat.armorclass, bitStream);
    return hasAC;
  }

  private static boolean readDurability(BitStream bitStream, Item item) {
    boolean hasDurability = item.type.is(Type.ARMO) || item.type.is(Type.WEAP);
    if (hasDurability) {
      int maxdurability = item.props.base().read(Stat.maxdurability, bitStream);
      if (maxdurability > 0) item.props.base().read(Stat.durability, bitStream);
    }
    return hasDurability;
  }

  private static boolean readSockets(BitStream bitStream, Item item) {
    boolean hasSockets = (item.flags & Item.ITEMFLAG_SOCKETED) == Item.ITEMFLAG_SOCKETED
        && (item.type.is(Type.ARMO) || item.type.is(Type.WEAP));
    if (hasSockets) {
      int item_numsockets = item.props.base().read(Stat.item_numsockets, bitStream);
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
      int quantity = bitStream.readU15(9);
      item.props.base().put(Stat.quantity, quantity);
    }
    return hasQuantity;
  }
}
