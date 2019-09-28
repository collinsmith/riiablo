package com.riiablo.codec.excel;

@Excel.Binned
public class PlrType extends Excel<PlrType.Entry> {
  public static final int AM = 0;
  public static final int SO = 1;
  public static final int NE = 2;
  public static final int PA = 3;
  public static final int BA = 4;
  public static final int DZ = 5;
  public static final int AS = 6;

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column
    public String Name;

    @Column
    @Key
    public String Token;
  }
}
