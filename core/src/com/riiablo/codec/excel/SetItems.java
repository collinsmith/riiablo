package com.riiablo.codec.excel;

public class SetItems extends Excel<SetItems.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return index;
    }

    public Sets.Entry getSet() {
      return parentSet;
    }

    Sets.Entry parentSet;

    @Key
    @Column
    public String  index;
    @Column public String  set;
    @Column public String  item;
    @Column(format = "*item")
    public String  _item;
    @Column public int      rarity;
    @Column public int      lvl;
    @Column(format = "lvl req")
    public int     lvl_req;
    @Column public String   chrtransform;
    @Column public String   invtransform;
    @Column public String   invfile;
    @Column public String   flippyfile;
    @Column public String   dropsound;
    @Column public int      dropsfxframe;
    @Column public String   usesound;
    @Column(format = "cost mult")
    public int     cost_mult;
    @Column(format = "cost add")
    public int     cost_add;
    @Column(format = "add func")
    public int     add_func;
    @Column public String   prop1;
    @Column public int      par1;
    @Column public int      min1;
    @Column public int      max1;
    @Column public String   prop2;
    @Column public int      par2;
    @Column public int      min2;
    @Column public int      max2;
    @Column public String   prop3;
    @Column public int      par3;
    @Column public int      min3;
    @Column public int      max3;
    @Column public String   prop4;
    @Column public int      par4;
    @Column public int      min4;
    @Column public int      max4;
    @Column public String   prop5;
    @Column public int      par5;
    @Column public int      min5;
    @Column public int      max5;
    @Column public String   prop6;
    @Column public int      par6;
    @Column public int      min6;
    @Column public int      max6;
    @Column public String   prop7;
    @Column public int      par7;
    @Column public int      min7;
    @Column public int      max7;
    @Column public String   prop8;
    @Column public int      par8;
    @Column public int      min8;
    @Column public int      max8;
    @Column public String   prop9;
    @Column public int      par9;
    @Column public int      min9;
    @Column public int      max9;
    @Column public String   aprop1a;
    @Column public int      apar1a;
    @Column public int      amin1a;
    @Column public int      amax1a;
    @Column public String   aprop1b;
    @Column public int      apar1b;
    @Column public int      amin1b;
    @Column public int      amax1b;
    @Column public String   aprop2a;
    @Column public int      apar2a;
    @Column public int      amin2a;
    @Column public int      amax2a;
    @Column public String   aprop2b;
    @Column public int      apar2b;
    @Column public int      amin2b;
    @Column public int      amax2b;
    @Column public String   aprop3a;
    @Column public int      apar3a;
    @Column public int      amin3a;
    @Column public int      amax3a;
    @Column public String   aprop3b;
    @Column public int      apar3b;
    @Column public int      amin3b;
    @Column public int      amax3b;
    @Column public String   aprop4a;
    @Column public int      apar4a;
    @Column public int      amin4a;
    @Column public int      amax4a;
    @Column public String   aprop4b;
    @Column public int      apar4b;
    @Column public int      amin4b;
    @Column public int      amax4b;
    @Column public String   aprop5a;
    @Column public int      apar5a;
    @Column public int      amin5a;
    @Column public int      amax5a;
    @Column public String   aprop5b;
    @Column public int      apar5b;
    @Column public int      amin5b;
    @Column public int      amax5b;
  }
}
