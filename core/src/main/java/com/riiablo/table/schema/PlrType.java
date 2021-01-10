package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class PlrType {
  public static final int AM = 0;
  public static final int SO = 1;
  public static final int NE = 2;
  public static final int PA = 3;
  public static final int BA = 4;
  public static final int DZ = 5;
  public static final int AS = 6;

  @Override
  public String toString() {
    return Name;
  }

  public String Name;

  @PrimaryKey
  public String Token;
}
