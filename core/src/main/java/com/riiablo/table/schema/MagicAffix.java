package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;

@SuppressWarnings("unused")
public class MagicAffix extends Affix {
  public boolean spawnable;
  public boolean rare;
  public int level;
  public int maxlevel;
  public int levelreq;
  public String classspecific;

  @Format(format = "class")
  public String _class;

  public int classlevelreq;
  public int frequency;
  public int group;
  public boolean transform;
  public String transformcolor;
}
