package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.BodyLocsTable;

@Schema
@Table(BodyLocsTable.class)
@SuppressWarnings("unused")
public class BodyLocs {
  public static final int NONE = 0;
  public static final int HEAD = 1;
  public static final int NECK = 2;
  public static final int TORS = 3;
  public static final int RARM = 4;
  public static final int LARM = 5;
  public static final int RRIN = 6;
  public static final int LRIN = 7;
  public static final int BELT = 8;
  public static final int FEET = 9;
  public static final int GLOV = 10;
  public static final int RARM2 = 11;
  public static final int LARM2 = 12;
  public static final int NUM_LOCS = 11;

  @Override
  public String toString() {
    return Body_Location;
  }

  @PrimaryKey
  public String Code;

  @Format(format = "Body Location")
  public String Body_Location;
}
