package com.riiablo.codec.excel;

@Excel.Binned
public class ItemEntry extends Excel.Entry {
  @Override
  public String toString() {
    return name;
  }

  @Key
  @Column
  public String  code;

  @Column public String  name;
  @Column public String  namestr;
  @Column public boolean compactsave;
  @Column public int     version;
  @Column public String  alternateGfx;
  @Column public String  type;
  @Column public String  type2;
  @Column public int     component;
  @Column public String  flippyfile;
  @Column public String  invfile;
  @Column public String  uniqueinvfile;
  @Column public String  setinvfile;
  @Column public int     Transform;
  @Column public int     InvTrans;
  @Column public int     invwidth;
  @Column public int     invheight;
  @Column public String  dropsound;
  @Column public int     dropsfxframe;
  @Column public boolean stackable;
  @Column public int     minstack;
  @Column public int     maxstack;
  @Column public int     spawnstack;
  @Column public boolean useable;
  @Column public String  usesound;
  @Column public int     quest;
  @Column public boolean nodurability;
  @Column public int     level;
  @Column public int     levelreq;
  @Column public int     mindam;
  @Column public int     maxdam;
  @Column public int     speed;
  @Column public int     gemapplytype;
  @Column public boolean PermStoreItem;
  @Column public boolean multibuy;

  @Column(format = "Charsi%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int charsi[];
  @Column(format = "Gheed%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int gheed[];
  @Column(format = "Akara%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int akara[];

  @Column(format = "Fara%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int fara[];
  @Column(format = "Lysander%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int lysander[];
  @Column(format = "Drognan%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int drognan[];

  /**
   * FIXME: Hratli mislabeled within tables as Hralti in many places -- may require custom code per sheet
   *        NOTE: This is the only discrepancy between these columns
   * weapons: HraltiMin HraltiMax HraltiMagicMin HraltiMagicMax HratliMagicLvl
   * armor:   HraltiMin HraltiMax HraltiMagicMin HraltiMagicMax HratliMagicLvl
   * misc:    HraltiMin HraltiMax HraltiMagicMin HraltiMagicMax HraltiMagicLvl
   *
   */
  @Column(format = "Hralti%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int hratli[];
  @Column(format = "Alkor%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int alkor[];
  @Column(format = "Ormus%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int ormus[];
  @Column(format = "Elzix%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int elzix[];
  @Column(format = "Asheara%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int asheara[];

  @Column(format = "Halbu%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int halbu[];
  @Column(format = "Jamella%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int jamella[];

  @Column(format = "Malah%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int malah[];
  @Column(format = "Larzuk%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int larzuk[];
  @Column(format = "Drehya%s", values = {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}, endIndex = 5) public int drehya[];
}
