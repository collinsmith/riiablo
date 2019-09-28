package com.riiablo.codec.excel;

@Excel.Binned
public class Gems extends Excel<Gems.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return name;
    }

    @Key
    @Column
    public String  code;
    @Column public String  name;
    @Column public String  letter;
    @Column public int     transform;
    @Column public int     nummods;
    @Column(format = "weaponMod%dCode", startIndex = 1, endIndex = 4)
    public String  weaponModCode[];
    @Column(format = "weaponMod%dParam", startIndex = 1, endIndex = 4)
    public int     weaponModParam[];
    @Column(format = "weaponMod%dMin", startIndex = 1, endIndex = 4)
    public int     weaponModMin[];
    @Column(format = "weaponMod%dMax", startIndex = 1, endIndex = 4)
    public int     weaponModMax[];
    @Column(format = "helmMod%dCode", startIndex = 1, endIndex = 4)
    public String  helmModCode[];
    @Column(format = "helmMod%dParam", startIndex = 1, endIndex = 4)
    public int     helmModParam[];
    @Column(format = "helmMod%dMin", startIndex = 1, endIndex = 4)
    public int     helmModMin[];
    @Column(format = "helmMod%dMax", startIndex = 1, endIndex = 4)
    public int     helmModMax[];
    @Column(format = "shieldMod%dCode", startIndex = 1, endIndex = 4)
    public String  shieldModCode[];
    @Column(format = "shieldMod%dParam", startIndex = 1, endIndex = 4)
    public int     shieldModParam[];
    @Column(format = "shieldMod%dMin", startIndex = 1, endIndex = 4)
    public int     shieldModMin[];
    @Column(format = "shieldMod%dMax", startIndex = 1, endIndex = 4)
    public int     shieldModMax[];
  }
}
