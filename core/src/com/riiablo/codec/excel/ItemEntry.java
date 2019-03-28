package com.riiablo.codec.excel;

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
}
