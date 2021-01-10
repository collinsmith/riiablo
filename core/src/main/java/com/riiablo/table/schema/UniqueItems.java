package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class UniqueItems {
  @Override
  public String toString() {
    return index;
  }

  @PrimaryKey
  public String index;

  public int version;
  public boolean enabled;
  public boolean ladder;
  public int rarity;
  public int nolimit;
  public int lvl;

  @Format(format = "lvl req")
  public int lvl_req;

  public String code;

  @Format(format = "*type")
  public String type;

  @Format(format = "*uber")
  public boolean uber;

  public boolean carry1;

  @Format(format = "cost mult")
  public int cost_mult;

  @Format(format = "cost add")
  public int cost_add;

  public String chrtransform;
  public String invtransform;
  public String flippyfile;
  public String invfile;
  public String dropsound;
  public int dropsfxframe;
  public String usesound;
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
  public String prop10;
  public int par10;
  public int min10;
  public int max10;
  public String prop11;
  public int par11;
  public int min11;
  public int max11;
  public String prop12;
  public int par12;
  public int min12;
  public int max12;
}
