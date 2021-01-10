package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class SetItems {
  @Override
  public String toString() {
    return index;
  }

  // TODO:
  // public Sets.Entry getSet() {
  //   return parentSet;
  // }
  //
  // Sets.Entry parentSet;

  @PrimaryKey
  public String index;
  public String set;
  public String item;

  @Format(format = "*item")
  public String _item;

  public int rarity;
  public int lvl;

  @Format(format = "lvl req")
  public int lvl_req;

  public String chrtransform;
  public String invtransform;
  public String invfile;
  public String flippyfile;
  public String dropsound;
  public int dropsfxframe;
  public String usesound;

  @Format(format = "cost mult")
  public int cost_mult;

  @Format(format = "cost add")
  public int cost_add;

  @Format(format = "add func")
  public int add_func;

  public String prop1;
  public int par1;
  public int min1;
  public int max1;
  public String prop2;
  public int par2;
  public int min2;
  public int max2;
  public String prop3;
  public int par3;
  public int min3;
  public int max3;
  public String prop4;
  public int par4;
  public int min4;
  public int max4;
  public String prop5;
  public int par5;
  public int min5;
  public int max5;
  public String prop6;
  public int par6;
  public int min6;
  public int max6;
  public String prop7;
  public int par7;
  public int min7;
  public int max7;
  public String prop8;
  public int par8;
  public int min8;
  public int max8;
  public String prop9;
  public int par9;
  public int min9;
  public int max9;
  public String aprop1a;
  public int apar1a;
  public int amin1a;
  public int amax1a;
  public String aprop1b;
  public int apar1b;
  public int amin1b;
  public int amax1b;
  public String aprop2a;
  public int apar2a;
  public int amin2a;
  public int amax2a;
  public String aprop2b;
  public int apar2b;
  public int amin2b;
  public int amax2b;
  public String aprop3a;
  public int apar3a;
  public int amin3a;
  public int amax3a;
  public String aprop3b;
  public int apar3b;
  public int amin3b;
  public int amax3b;
  public String aprop4a;
  public int apar4a;
  public int amin4a;
  public int amax4a;
  public String aprop4b;
  public int apar4b;
  public int amin4b;
  public int amax4b;
  public String aprop5a;
  public int apar5a;
  public int amin5a;
  public int amax5a;
  public String aprop5b;
  public int apar5b;
  public int amin5b;
  public int amax5b;
}
