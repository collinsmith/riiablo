package com.riiablo.item.item4;

import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.codec.excel.MagicAffix;
import com.riiablo.codec.excel.SetItems;
import com.riiablo.codec.excel.UniqueItems;
import com.riiablo.item.PropertyList;

public class ItemUtils {
  private ItemUtils() {}

  @SuppressWarnings("unchecked")
  public static <T extends ItemEntry> T getBase(String code) {
    ItemEntry entry;
    if ((entry = Riiablo.files.armor  .get(code)) != null) return (T) entry;
    if ((entry = Riiablo.files.weapons.get(code)) != null) return (T) entry;
    if ((entry = Riiablo.files.misc   .get(code)) != null) return (T) entry;
    throw new GdxRuntimeException("Unable to locate entry for code: " + code);
  }

  // TODO: basic optimization to have a pre-prepared immutable prop list for each gem type
  static PropertyList[] getGemProps(Gems.Entry gem) {
    PropertyList[] props = new PropertyList[Item.NUM_GEMPROPS];
    props[Item.GEMPROPS_WEAPON] = PropertyList.obtain().add(gem.weaponModCode, gem.weaponModParam, gem.weaponModMin, gem.weaponModMax);
    props[Item.GEMPROPS_ARMOR ] = PropertyList.obtain().add(gem.helmModCode, gem.helmModParam, gem.helmModMin, gem.helmModMax);
    props[Item.GEMPROPS_SHIELD] = PropertyList.obtain().add(gem.shieldModCode, gem.shieldModParam, gem.shieldModMin, gem.shieldModMax);
    return props;
  }

  static String getInvFileName(Item item) {
    if (item.isIdentified()) {
      switch (item.quality) {
        case SET:
          SetItems.Entry setItem = (SetItems.Entry) item.qualityData;
          if (!setItem.invfile.isEmpty()) return setItem.invfile;
          break;
        case UNIQUE:
          UniqueItems.Entry uniqueItem = (UniqueItems.Entry) item.qualityData;
          if (!uniqueItem.invfile.isEmpty()) return uniqueItem.invfile;
          break;
        default:
          // do nothing
      }
    }

    return item.pictureId >= 0 ? item.typeEntry.InvGfx[item.pictureId] : item.base.invfile;
  }

  static String getInvColor(Item item) {
    if (item.base.InvTrans == 0 || !item.isIdentified()) return null;
    switch (item.quality) {
      case MAGIC: {
        MagicAffix affix;
        int prefix = item.qualityId & Item.MAGIC_AFFIX_MASK;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
          return affix.transformcolor;
        int suffix = item.qualityId >>> Item.MAGIC_AFFIX_SIZE;
        if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
          return affix.transformcolor;
        return null;
      }

      case RARE:
      case CRAFTED: {
        MagicAffix affix;
        RareQualityData rareQualityData = (RareQualityData) item.qualityData;
        for (int i = 0; i < RareQualityData.NUM_AFFIXES; i++) {
          int prefix = rareQualityData.prefixes[i];
          if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
            return affix.transformcolor;
          int suffix = rareQualityData.suffixes[i];
          if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
            return affix.transformcolor;
        }
        return null;
      }

      case SET:
        return ((SetItems.Entry) item.qualityData).invtransform;

      case UNIQUE:
        return ((UniqueItems.Entry) item.qualityData).invtransform;

      default:
        return null;
    }
  }

  static String getCharColor(Item item) {
    if (item.base.Transform == 0 || !item.isIdentified()) return null;
    switch (item.quality) {
      case MAGIC: {
        MagicAffix affix;
        int prefix = item.qualityId & Item.MAGIC_AFFIX_MASK;
        if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
          return affix.transformcolor;
        int suffix = item.qualityId >>> Item.MAGIC_AFFIX_SIZE;
        if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
          return affix.transformcolor;
        return null;
      }

      case RARE:
      case CRAFTED: {
        MagicAffix affix;
        RareQualityData rareQualityData = (RareQualityData) item.qualityData;
        for (int i = 0; i < RareQualityData.NUM_AFFIXES; i++) {
          int prefix = rareQualityData.prefixes[i];
          if ((affix = Riiablo.files.MagicPrefix.get(prefix)) != null && affix.transform)
            return affix.transformcolor;
          int suffix = rareQualityData.suffixes[i];
          if ((affix = Riiablo.files.MagicSuffix.get(suffix)) != null && affix.transform)
            return affix.transformcolor;
        }
        return null;
      }

      case SET:
        return ((SetItems.Entry) item.qualityData).chrtransform;

      case UNIQUE:
        return ((UniqueItems.Entry) item.qualityData).chrtransform;

      default:
        return null;
    }
  }
}
