package com.riiablo.item;

import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Gems;
import com.riiablo.codec.excel.ItemEntry;

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
    props[Item.GEMPROPS_WEAPON] = PropertyList.obtain().add(gem.weaponModCode, gem.weaponModParam, gem.weaponModMin, gem.weaponModMax);
    props[Item.GEMPROPS_ARMOR ] = PropertyList.obtain().add(gem.helmModCode, gem.helmModParam, gem.helmModMin, gem.helmModMax);
    props[Item.GEMPROPS_SHIELD] = PropertyList.obtain().add(gem.shieldModCode, gem.shieldModParam, gem.shieldModMin, gem.shieldModMax);
    return props;
  }
}
