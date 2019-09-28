package com.riiablo.codec.excel;

@Excel.Binned
public class BodyLocs extends Excel<BodyLocs.Entry> {
  public static final int NONE  = 0;
  public static final int HEAD  = 1;
  public static final int NECK  = 2;
  public static final int TORS  = 3;
  public static final int RARM  = 4;
  public static final int LARM  = 5;
  public static final int RRIN  = 6;
  public static final int LRIN  = 7;
  public static final int BELT  = 8;
  public static final int FEET  = 9;
  public static final int GLOV  = 10;
  public static final int RARM2 = 11;
  public static final int LARM2 = 12;
  public static final int NUM_LOCS = 11;

  @Override
  protected void init() {
    put(RARM2, get(RARM));
    put(LARM2, get(LARM));
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Body_Location;
    }

    @Column(format = "Body Location")
    public String  Body_Location;

    @Key
    @Column
    public String  Code;
  }
}
