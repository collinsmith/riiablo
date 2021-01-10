package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class ItemTypes {
  // TODO:
  // public boolean is(Entry entry, String type) {
  //   if (entry.Code.equals(type)) {
  //     return true;
  //   }
  //
  //   for (String Equiv : entry.Equiv) {
  //     if (Equiv.isEmpty()) {
  //       break;
  //     } else if (is(get(Equiv), type)) {
  //       return true;
  //     }
  //   }
  //
  //   return false;
  // }
  //
  // public boolean is(String type) {
  //   return Riiablo.files.ItemTypes.is(this, type);
  // }

  @Override
  public String toString() {
    return ItemType;
  }

  public String  ItemType;

  @PrimaryKey
  public String Code;

  @Format(
      startIndex = 1,
      endIndex = 3)
  public String Equiv[];

  public boolean Repair;
  public boolean Body;

  @Format(
      startIndex = 1,
      endIndex = 3)
  public String BodyLoc[];

  public String Shoots;
  public String Quiver;
  public boolean Throwable;
  public boolean Reload;
  public boolean ReEquip;
  public boolean AutoStack;
  public boolean Magic;
  public boolean Rare;
  public boolean Normal;
  public boolean Charm;
  public boolean Gem;
  public boolean Beltable;

  @Format(
      format = "MaxSock%s",
      values = {"1", "25", "40"},
      endIndex = 3)
  public int MaxSock[];

  public int TreasureClass;
  public int Rarity;
  public String StaffMods;
  public int CostFormula;
  public String Class;
  public int VarInvGfx;

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String InvGfx[];

  public String StorePage;
}
