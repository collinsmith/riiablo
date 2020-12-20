package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Weapons extends ItemEntry {
  public String wclass;

  @Format(format = "2handedwclass")
  public String _2handedwclass;

  @Format(format = "1or2handed")
  public boolean _1or2handed;

  @Format(format = "2handed")
  public boolean _2handed;

  @Format(format = "2handmindam")
  public int _2handmindam;

  @Format(format = "2handmaxdam")
  public int _2handmaxdam;

  public int minmisdam;
  public int maxmisdam;
  public int reqstr;
  public int reqdex;
  public int durability;
}
