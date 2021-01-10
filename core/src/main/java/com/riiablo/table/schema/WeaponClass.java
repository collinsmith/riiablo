package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class WeaponClass {
  // TODO:
  // public WeaponClass get(ItemEntry entry) {
  //   return entry instanceof Weapons ? get(((Weapons) entry).wclass) : null;//get("NONE");
  // }

  @Override
  public String toString() {
    return WeaponClass;
  }

  @Format(format = "Weapon Class")
  public String WeaponClass;

  @PrimaryKey
  public String Code;
}
