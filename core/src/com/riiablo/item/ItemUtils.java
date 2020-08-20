package com.riiablo.item;

import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.excel.ItemEntry;
import com.riiablo.log.Log;

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

  public static int getBaseIndex(String code) {
    ItemEntry entry;
    if ((entry = Riiablo.files.armor  .get(code)) != null) return Riiablo.files.armor  .index(entry.code);
    if ((entry = Riiablo.files.weapons.get(code)) != null) return Riiablo.files.weapons.index(entry.code);
    if ((entry = Riiablo.files.misc   .get(code)) != null) return Riiablo.files.misc   .index(entry.code);
    throw new GdxRuntimeException("Unable to locate entry for code: " + code);
  }

  // TODO: basic optimization to have a pre-prepared immutable prop list for each gem type
  static PropertyList[] getGemProps(Gems.Entry gem) {
    PropertyList[] props = new PropertyList[Item.NUM_GEMPROPS];
    try {
      Log.put("propList", "GEMPROPS_WEAPON");
      props[Item.GEMPROPS_WEAPON] = PropertyList.obtain().add(gem.weaponModCode, gem.weaponModParam, gem.weaponModMin, gem.weaponModMax);
      Log.put("propList", "GEMPROPS_ARMOR");
      props[Item.GEMPROPS_ARMOR] = PropertyList.obtain().add(gem.helmModCode, gem.helmModParam, gem.helmModMin, gem.helmModMax);
      Log.put("propList", "GEMPROPS_SHIELD");
      props[Item.GEMPROPS_SHIELD] = PropertyList.obtain().add(gem.shieldModCode, gem.shieldModParam, gem.shieldModMin, gem.shieldModMax);
    } finally {
      Log.remove("propList");
    }
    return props;
  }
}
