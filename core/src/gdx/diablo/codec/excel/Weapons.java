package gdx.diablo.codec.excel;

public class Weapons extends Excel<Weapons.Entry> {
  public static class Entry extends ItemEntry {
    @Column public String  wclass;
    @Column(format = "2handedwclass")
    public String  _2handedwclass;
    @Column(format = "1or2handed")
    public boolean _1or2handed;
    @Column(format = "2handed")
    public boolean _2handed;
  }
}
