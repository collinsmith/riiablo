package gdx.diablo.codec.excel;

public class WeaponClass extends Excel<WeaponClass.Entry> {
  public Entry get(ItemEntry entry) {
    return entry instanceof Weapons.Entry ? get(((Weapons.Entry) entry).wclass) : null;//get("NONE");
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return WeaponClass;
    }

    @Column(format = "Weapon Class")
    public String WeaponClass;

    @Column
    @Key
    public String Code;
  }
}
