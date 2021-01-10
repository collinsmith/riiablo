package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Speech {
  @Override
  public String toString() {
    return sound;
  }

  @PrimaryKey
  public String  sound;

  public String  soundstr;
}
