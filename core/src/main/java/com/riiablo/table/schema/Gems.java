package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Gems {
  @Override
  public String toString() {
    return name;
  }

  @PrimaryKey
  public String code;
  public String name;
  public String letter;
  public int transform;
  public int nummods;

  @Format(
      format = "weaponMod%dCode",
      startIndex = 1,
      endIndex = 4)
  public String weaponModCode[];

  @Format(
      format = "weaponMod%dParam",
      startIndex = 1,
      endIndex = 4)
  public int weaponModParam[];

  @Format(
      format = "weaponMod%dMin",
      startIndex = 1,
      endIndex = 4)
  public int weaponModMin[];

  @Format(
      format = "weaponMod%dMax",
      startIndex = 1,
      endIndex = 4)
  public int weaponModMax[];

  @Format(
      format = "helmMod%dCode",
      startIndex = 1,
      endIndex = 4)
  public String helmModCode[];

  @Format(
      format = "helmMod%dParam",
      startIndex = 1,
      endIndex = 4)
  public int helmModParam[];

  @Format(
      format = "helmMod%dMin",
      startIndex = 1,
      endIndex = 4)
  public int helmModMin[];

  @Format(
      format = "helmMod%dMax",
      startIndex = 1,
      endIndex = 4)
  public int helmModMax[];

  @Format(
      format = "shieldMod%dCode",
      startIndex = 1,
      endIndex = 4)
  public String shieldModCode[];

  @Format(
      format = "shieldMod%dParam",
      startIndex = 1,
      endIndex = 4)
  public int shieldModParam[];

  @Format(
      format = "shieldMod%dMin",
      startIndex = 1,
      endIndex = 4)
  public int shieldModMin[];

  @Format(
      format = "shieldMod%dMax",
      startIndex = 1,
      endIndex = 4)
  public int shieldModMax[];
}
