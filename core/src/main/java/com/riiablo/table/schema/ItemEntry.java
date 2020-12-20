package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;

public class ItemEntry {
  @Override
  public String toString() {
    return name;
  }

  @PrimaryKey
  public String code;
  public String name;
  public String namestr;
  public boolean compactsave;
  public int version;
  public String alternateGfx;
  public String type;
  public String type2;
  public int component;
  public String flippyfile;
  public String invfile;
  public String uniqueinvfile;
  public String setinvfile;
  public int Transform;
  public int InvTrans;
  public int invwidth;
  public int invheight;
  public String dropsound;
  public int dropsfxframe;
  public boolean stackable;
  public int minstack;
  public int maxstack;
  public int spawnstack;
  public boolean useable;
  public String usesound;
  public int quest;
  public boolean nodurability;
  public int level;
  public int levelreq;
  public int mindam;
  public int maxdam;
  public int speed;
  public int gemsockets;
  public int gemapplytype;
  public boolean PermStoreItem;
  public boolean multibuy;

  @Format(
      format = "Charsi%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int charsi[];

  @Format(
      format = "Gheed%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int gheed[];

  @Format(
      format = "Akara%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int akara[];

  @Format(
      format = "Fara%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int fara[];

  @Format(
      format = "Lysander%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int lysander[];

  @Format(
      format = "Drognan%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int drognan[];

  /**
   * FIXME: Hratli mislabeled within tables as Hralti in many places -- may require custom code per sheet
   *        NOTE: This is the only discrepancy between these columns
   * weapons: HraltiMin HraltiMax HraltiMagicMin HraltiMagicMax HratliMagicLvl
   * armor:   HraltiMin HraltiMax HraltiMagicMin HraltiMagicMax HratliMagicLvl
   * misc:    HraltiMin HraltiMax HraltiMagicMin HraltiMagicMax HraltiMagicLvl
   *
   */
  @Format(
      format = "Hralti%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int hratli[];

  @Format(
      format = "Alkor%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int alkor[];

  @Format(
      format = "Ormus%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int ormus[];

  @Format(format = "Elzix%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int elzix[];

  @Format(format = "Asheara%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int asheara[];

  @Format(
      format = "Halbu%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int halbu[];

  @Format(
      format = "Jamella%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int jamella[];

  @Format(
      format = "Malah%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int malah[];

  @Format(
      format = "Larzuk%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int larzuk[];

  @Format(
      format = "Drehya%s",
      values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"},
      endIndex = 5)
  public int drehya[];
}
