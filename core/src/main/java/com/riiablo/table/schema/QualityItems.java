package com.riiablo.table.schema;

import java.util.Arrays;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.Schema;

@Schema(indexed = true)
@SuppressWarnings("unused")
public class QualityItems {
  @Override
  public String toString() {
    switch (nummods) {
      case 1:  return effect[0];
      default: return Arrays.toString(effect);
    }
  }

  public int nummods;
  public String mod1code;
  public int mod1param;
  public int mod1min;
  public int mod1max;
  public String mod2code;
  public int mod2param;
  public int mod2min;
  public int mod2max;
  public int ToHitMin;
  public int ToHitMax;

  @Format(format = "Dam%Min")
  public int DamMin;

  @Format(format = "Dam%Max")
  public int DamMax;

  @Format(format = "AC%Min")
  public int ACMin;

  @Format(format = "AC%Max")
  public int ACMax;

  @Format(format = "Dur%Min")
  public int DurMin;

  @Format(format = "Dur%Max")
  public int DurMax;

  @Format(
      startIndex = 1,
      endIndex = 3)
  public String effect[];

  public boolean armor;
  public boolean weapon;
  public boolean shield;
  public boolean thrown;
  public boolean scepter;
  public boolean wand;
  public boolean staff;
  public boolean bow;
  public boolean boots;
  public boolean gloves;
  public boolean belt;
  public int level;
  public int multiply;
  public int add;
}
