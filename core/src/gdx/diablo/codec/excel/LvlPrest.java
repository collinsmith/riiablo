package gdx.diablo.codec.excel;

public class LvlPrest extends Excel<LvlPrest.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column public String  Name;
    @Column
    @Key
    public int     Def;
    @Column public int     LevelId;
    @Column public boolean Populate;
    @Column public boolean Logicals;
    @Column public boolean Outdoors;
    @Column public boolean Animate;
    @Column public boolean KillEdge;
    @Column public boolean FillBlanks;
    @Column public int     SizeX;
    @Column public int     SizeY;
    @Column public boolean AutoMap;
    @Column public boolean Scan;
    @Column public int     Pops;
    @Column public int     PopPad;
    @Column public int     Files;
    @Column(startIndex = 1, endIndex = 7)
    public String  File[];
    @Column public int     Dt1Mask;
    @Column public boolean Beta;
    @Column public boolean Expansion;
  }
}
