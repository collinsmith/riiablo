package gdx.diablo.codec.excel;

import gdx.diablo.Diablo;

public class ItemTypes extends Excel<ItemTypes.Entry> {
  public boolean is(Entry entry, String type) {
    if (entry.Code.equals(type)) {
      return true;
    }

    for (String Equiv : entry.Equiv) {
      if (Equiv.isEmpty()) {
        break;
      } else if (is(get(Equiv), type)) {
        return true;
      }
    }

    return false;
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return ItemType;
    }

    public boolean is(String type) {
      return Diablo.files.ItemTypes.is(this, type);
    }

    @Column public String  ItemType;

    @Key
    @Column
    public String  Code;
    @Column(startIndex = 1, endIndex = 3)
    public String  Equiv[];
    @Column public boolean Repair;
    @Column public boolean Body;
    @Column(startIndex = 1, endIndex = 3)
    public String  BodyLoc[];
    @Column public String  Shoots;
    @Column public String  Quiver;
    @Column public boolean Throwable;
    @Column public boolean Reload;
    @Column public boolean ReEquip;
    @Column public boolean AutoStack;
    @Column public boolean Magic;
    @Column public boolean Rare;
    @Column public boolean Normal;
    @Column public boolean Charm;
    @Column public boolean Gem;
    @Column public boolean Beltable;
    @Column public int     MaxSock1;
    @Column public int     MaxSock25;
    @Column public int     MaxSock40;
    @Column public int     TreasureClass;
    @Column public int     Rarity;
    @Column public String  StaffMods;
    @Column public int     CostFormula;
    @Column public String  Class;
    @Column public int     VarInvGfx;
    @Column(startIndex = 1, endIndex = 7)
    public String  InvGfx[];
    @Column public String  StorePage;
  }
}
