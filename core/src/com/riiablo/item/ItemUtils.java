package com.riiablo.item;

import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.Riiablo;
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
}
