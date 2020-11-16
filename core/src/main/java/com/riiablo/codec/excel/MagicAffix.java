package com.riiablo.codec.excel;

public class MagicAffix extends Affix {
  @Column public boolean spawnable;
  @Column public boolean rare;
  @Column public int     level;
  @Column public int     maxlevel;
  @Column public int     levelreq;
  @Column public String  classspecific;
  @Column(format = "class")
  public String  _class;
  @Column public int     classlevelreq;
  @Column public int     frequency;
  @Column public int     group;
  @Column public boolean transform;
  @Column public String  transformcolor;
}
