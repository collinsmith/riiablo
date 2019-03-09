package com.riiablo.codec.excel;

public class Affix extends Excel.Entry {
  @Override
  public String toString() {
    return name;
  }

  @Key
  @Column public String  name;
  @Column public int     version;
  @Column public int     add;
  @Column public int     multiply;
  @Column public int     divide;
}
