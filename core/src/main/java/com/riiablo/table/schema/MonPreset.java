package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.MonPresetTable;

@Schema(
    preload = true
)
@Table(MonPresetTable.class)
public class MonPreset {
  @Override
  public String toString() {
    return Place;
  }

  @PrimaryKey
  public int Act;

  public String Place;
}
