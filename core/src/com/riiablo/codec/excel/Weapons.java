package com.riiablo.codec.excel;

@Excel.Binned
public class Weapons extends Excel<Weapons.Entry> {
  public static class Entry extends ItemEntry {
    @Column public String  wclass;
    @Column(format = "2handedwclass")
    public String  _2handedwclass;
    @Column(format = "1or2handed")
    public boolean _1or2handed;
    @Column(format = "2handed")
    public boolean _2handed;
    @Column(format = "2handmindam")
    public int     _2handmindam;
    @Column(format = "2handmaxdam")
    public int     _2handmaxdam;
    @Column public int     minmisdam;
    @Column public int     maxmisdam;
    @Column public int     reqstr;
    @Column public int     reqdex;
    @Column public int     durability;
  }
}
