package gdx.diablo.codec.excel;

public class LvlTypes extends Excel<LvlTypes.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column public String  Name;
    @Column
    @Key
    public int     Id;
    @Column(format = "File %d", startIndex = 1, endIndex = 33)
    public String  File[];
    @Column public boolean Beta;
    @Column public int     Act;
    @Column public boolean Expansion;
  }
}
