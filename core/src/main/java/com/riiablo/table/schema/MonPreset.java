package com.riiablo.table.schema;

import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.MonPresetTable;

@Schema(
    indexed = true,
    preload = true
)
@Table(MonPresetTable.class)
@SuppressWarnings("unused")
public class MonPreset {
  @Override
  public String toString() {
    return Place;
  }

  public int Act;
  public String Place;
}
