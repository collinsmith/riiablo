package gdx.diablo.codec.excel;

public class UniqueItems extends Excel<UniqueItems.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return index;
    }

    @Key
    @Column
    public String  index;
    @Column public int     version;
    @Column public boolean enabled;
    @Column public boolean ladder;
    @Column public int     rarity;
    @Column public int     nolimit;
    @Column public int     lvl;
    @Column(format = "lvl req")
    public int     lvl_req;
    @Column public String  code;
    @Column(format = "*type")
    public String  type;
    @Column(format = "*uber")
    public boolean uber;
    @Column public boolean carry1;
    @Column(format = "cost mult")
    public int     cost_mult;
    @Column(format = "cost add")
    public int     cost_add;
    @Column public String  chrtransform;
    @Column public String  invtransform;
    @Column public String  flippyfile;
    @Column public String  invfile;
    @Column public String  dropsound;
    @Column public String  dropsfxframe;
    @Column public String  usesound;
    @Column public String  prop1;
    @Column public int     par1;
    @Column public int     min1;
    @Column public int     max1;
    @Column public String  prop2;
    @Column public int     par2;
    @Column public int     min2;
    @Column public int     max2;
    @Column public String  prop3;
    @Column public int     par3;
    @Column public int     min3;
    @Column public int     max3;
    @Column public String  prop4;
    @Column public int     par4;
    @Column public int     min4;
    @Column public int     max4;
    @Column public String  prop5;
    @Column public int     par5;
    @Column public int     min5;
    @Column public int     max5;
    @Column public String  prop6;
    @Column public int     par6;
    @Column public int     min6;
    @Column public int     max6;
    @Column public String  prop7;
    @Column public int     par7;
    @Column public int     min7;
    @Column public int     max7;
    @Column public String  prop8;
    @Column public int     par8;
    @Column public int     min8;
    @Column public int     max8;
    @Column public String  prop9;
    @Column public int     par9;
    @Column public int     min9;
    @Column public int     max9;
    @Column public String  prop10;
    @Column public int     par10;
    @Column public int     min10;
    @Column public int     max10;
    @Column public String  prop11;
    @Column public int     par11;
    @Column public int     min11;
    @Column public int     max11;
    @Column public String  prop12;
    @Column public int     par12;
    @Column public int     min12;
    @Column public int     max12;
  }
}
